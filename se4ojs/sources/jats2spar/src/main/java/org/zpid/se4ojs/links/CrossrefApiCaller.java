/**
 *
 */
package org.zpid.se4ojs.links;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.saxon.value.StringValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zpid.se4ojs.app.Config;

/**
 * Class that performs CrossRef Api Calls.
 * @author barth
 *
 */
public class CrossrefApiCaller {

	private static final String DOI_PREFIX_URI = "http://dx.doi.org/";

	private static final String PDF_LINK_INDICATOR = "name=\"citation_pdf_url\" content=\"";
	private static final String HTML_LINK_INDICATOR = "name=\"citation_fulltext_html_url\" content=\"";
	private static final String SUBJECT_INDICATOR = "<meta name=\"DC.Subject\" xml:lang=\"en\" content=".toLowerCase();


	private static final int PDF_LINK_INDICATOR_LENGTH = PDF_LINK_INDICATOR.length();
	private static final int HTML_LINK_INDICATOR_LENGTH = HTML_LINK_INDICATOR.length();
	private static final int SUBJECT_INDICATOR_LENGTH = SUBJECT_INDICATOR.length();

	private static final String END_TAG = "/>";
	private static final String END_ATTR = ">";

	private Logger log = LogManager.getLogger(CrossrefApiCaller.class);

	private static Map<String, String> doiResults = new HashMap<>();


	/**
	 * Returns the URL to the PDF and Html manifestations of the article with the given doi,
	 * or an array with empty string entries for a format that could not be retrieved.
	 *
	 * @param doi the article doi
	 * @return the URL to the PDF
	 */
	public String[] getExternalLinksByDoi(String doi) {
		String[] links = new String[] { StringUtils.EMPTY, StringUtils.EMPTY };
		String record = doiResults.get(doi);
		if (record != null) {
			extract(record, doi);
		} else {
			try {
				URLConnection urlConnection = openUrlConnection(new URL(
						DOI_PREFIX_URI + doi));
				urlConnection.setRequestProperty("Content-Type",
						"application/vnd.crossref.unixsd+xml");
				urlConnection.connect();
				try (InputStream inputStream = urlConnection.getInputStream()) {
					if (inputStream != null) {
						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(inputStream))) {
							links = fetchRecord(reader, doi);
						} catch (IOException e) {
							log.warn("Error trying to obtain crossref record for doi: "
									+ doi + "\n" + e.getLocalizedMessage());
							e.printStackTrace();
						}
					}
				}
			} catch (IOException e) {
				log.warn("Unable to resolve doi: " + doi + " by crossref API"
						+ "\n" + e.getLocalizedMessage());
			}
		}
		return links;
	}

	/**
	 * Obtains the subject keywords from the crossref record.
	 * @FIXME Currently no API call is made, we tacitly assume that method {@link CrossrefApiCaller#getExternalLinksByDoi(String)}
	 * has been called previously and has stored the crossref metadata record if it had been available.
	 *
	 * @param doi the doi of the article
	 * @return the list of subject terms
	 */
	public List<StringValue> getSubjectsByDoi(String doi) {
		List<StringValue> subjects = new ArrayList<>();
		String record = doiResults.get(doi);
		if (record != null) {
			String match = record;
			while (match.length() > 0) {
				if (match.contains(SUBJECT_INDICATOR)) {
					int beginIndex = match.indexOf(SUBJECT_INDICATOR)
							+ SUBJECT_INDICATOR_LENGTH;
					if (beginIndex > -1) {
						int endIndex = getEndIndex(match, beginIndex);
						if (endIndex > -1) {
							String subject = match
									.substring(beginIndex, endIndex)
									.replace("/>", "").replace(">", "")
									.replace("\"", "");
							log.debug("subject: " + subject);
							subjects.add(StringValue.makeStringValue(subject));
							match = match.substring(endIndex);
						}
					}
				} else {
					match = "";
				}
			}
		}
		return subjects;
	}

	private String[] fetchRecord(BufferedReader reader, String doi) throws IOException {
		String record = StringUtils.EMPTY;
		String line = StringUtils.EMPTY;

		while ((line = reader.readLine()) != null) {
			record = record + line;
			log.trace(line);
		}
		String normalizedRecord = record.toLowerCase();
		doiResults.put(doi, normalizedRecord);
		return extract(normalizedRecord, doi);

	}

	protected String[] extract(String record, String doi) {
		String[] links = new String[]{StringUtils.EMPTY, StringUtils.EMPTY};
		links[0] = extractPdfLink(record);
		if (links[0].equals(StringUtils.EMPTY)) {
			log.info("No link to PDF version found for doi: " + doi);
		}
		links[1] = extractHtmlLink(record);
		if (links[1].equals(StringUtils.EMPTY)) {
			log.info("No link to HTML version found for doi: " + doi);
		}
		return links;
	}

	private String extractHtmlLink(String record) {
		String s = StringUtils.EMPTY;
		if (record.contains(HTML_LINK_INDICATOR)) {
			int beginIndex = record.indexOf(HTML_LINK_INDICATOR)
					+ HTML_LINK_INDICATOR_LENGTH;
			if (beginIndex > PDF_LINK_INDICATOR_LENGTH) {
				int endIndex = getEndIndex(record, beginIndex);
				if (endIndex > -1) {
					s = record.substring(beginIndex, endIndex).replace("/>", "").replace(">", "").replace("\"", "");
				} else {
					log.debug("unexpected end of html link: " + s);
				}
			}
			log.debug("html link: " + s);
		}
		return s;
	}

	protected int getEndIndex(String record, int beginIndex) {
		int endTagIndex = record.indexOf(END_TAG, beginIndex);
		int endAttrIndex = record.indexOf(END_ATTR, beginIndex);
		int endIndex = -1;
		if (endTagIndex == -1 || endAttrIndex < endTagIndex) {
			endIndex = endAttrIndex;
		} else {
			endIndex = endTagIndex;
		}
		return endIndex;
	}


	private String extractPdfLink(String record) {
		String s = StringUtils.EMPTY;
		if (record.contains(PDF_LINK_INDICATOR)) {
			int beginIndex = record.indexOf(PDF_LINK_INDICATOR)
					+ PDF_LINK_INDICATOR_LENGTH;
			if (beginIndex > PDF_LINK_INDICATOR_LENGTH) {
				int endIndex = getEndIndex(record, beginIndex);
				if (endIndex > -1) {
					s = record.substring(beginIndex, endIndex).replace("/>", "").replace(">", "").replace("\"", "");
				} else {
					log.debug("unexpected end of pdf link: " + s);
				}
			}
			log.debug("pdf link: " + s);
		}
		return s;
	}


	public String get(String urlToGet) {
		HttpURLConnection conn = null;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			conn = openUrlConnection(new URL(urlToGet));
			conn.setRequestMethod("GET");
//			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Accept", "text/turtle");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			log.error("Error occurred during fetching information from Crossref API: \n\t"
					+ e.fillInStackTrace());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	protected static HttpURLConnection openUrlConnection(URL url)
			throws IOException {
		HttpURLConnection conn;
        String proxySettings = Config.getProxy();
		if (!StringUtils.isEmpty(proxySettings)) {
			String[] proxyComponents = proxySettings.split(":");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					proxyComponents[0], Integer.valueOf(proxyComponents[1])));
			conn = (HttpURLConnection) url.openConnection(proxy);
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		return conn;
	}

	public static void main (String[] args) {
		CrossrefApiCaller caller = new CrossrefApiCaller();
//		caller.getExternalLinksByDoi("10.1038/171737a0"); //nature magazine. returns empty record but was the example used by http://inkdroid.org/2011/04/25/dois-as-linked-data/
		caller.getExternalLinksByDoi("10.5964/jspp.v1i1.85");
	}

}
