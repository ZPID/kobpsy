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
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import net.sf.saxon.value.StringValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zpid.se4ojs.app.Config;

/**
 * Class that performs CrossRef Api Calls.
 * For ZPID-internal use only.
 * Do not use.
 * @author barth
 *
 */
public class PubPsychApiCaller {

	private static final String TAG_START = "<str>";
	private static final String PUBPSYCH_LINK_PREFIX = "not to be disclosed";
	private static final String PUBPSYCH_LINK_SUFFIX = "not to be disclosed";

	private static final String PUBPSYCH_QUERY_SERVER = "not to be disclosed";
	private static final String PUBPSYCH_QUERY_INFIX_URI = "not to be disclosed";
	private static final String PUBPSYCH_QUERY_SUFFIX_URI = "not to be disclosed";


	private static final String STR_NAME_ID = "not to be disclosed";
	private static final String SUBJECT_INDICATOR = "not to be disclosed";
	private static final int SUBJECT_INDICATOR_LENGTH = SUBJECT_INDICATOR.length();


	private static final String TAG_END = "not to be disclosed";


	private Logger log = LogManager.getLogger(PubPsychApiCaller.class);

	//careful: link can be empty string. check when reading from map
	private static Map<String, String> titlePubPsychLink = new HashMap<>();
	private static Map<String, List<StringValue>> titleKeywords = new HashMap<>();

	static {
		disableSslVerification();
	}

	private static void disableSslVerification() {
	    try
	    {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            @SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            @SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {

				}
				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {

				}
	        }
	        };

	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

	        // Create all-trusting host name verifier
	        HostnameVerifier allHostsValid = new HostnameVerifier() {
	            public boolean verify(String hostname, SSLSession session) {
	                return true;
	            }
	        };

	        // Install the all-trusting host verifier
	        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Returns the URL to the PDF and Html manifestations of the article with the given doi,
	 * or an array with empty string entries for a format that could not be retrieved.
	 * @param firstAuthor
	 *
	 * @param doi the article doi
	 * @return the URL to the PDF
	 */
	public String getPubPsychLinkByTitle(String title, String firstAuthor) {
			String link = titlePubPsychLink.get(title);
			String encodedTitle = StringUtils.EMPTY;
			String encodedAuthor = StringUtils.EMPTY;
			if (link != null) {
				return link;
			} else {
				InputStream inputStream = null;
			try {
//				@FIXME: problems occur with some url-encoded characters, e.g. forward slash: tomcat refuses it for security reasons
//				Workaround: explore whether it is sufficient to first replace spaces by '+' and escape other solr special characters
//				and then url-encode the title
				encodedTitle = title.replaceAll("/", " ");
				encodedTitle = encodedTitle.replaceAll("\\s","+");
				encodedTitle = encodedTitle.replaceAll("[+]{2,}","+");
				String encodedTitleUrl = URLEncoder.encode(encodedTitle, "UTF-8");

				encodedAuthor = firstAuthor.replaceAll("/", " ");
				encodedAuthor = encodedAuthor.replaceAll("\\s","+");
				encodedAuthor = encodedAuthor.replaceAll("[+]{2,}","+");
				String encodedAuthorUrl = URLEncoder.encode(encodedAuthor, "UTF-8");

				String url = PUBPSYCH_QUERY_SERVER
						+ encodedTitleUrl + PUBPSYCH_QUERY_INFIX_URI
						+ encodedAuthorUrl + PUBPSYCH_QUERY_SUFFIX_URI;

				URLConnection urlConnection = openUrlConnection(new URL(url));
				urlConnection.connect();
				inputStream = urlConnection.getInputStream();
				log.debug("encoded title: " + url);
			} catch (IOException e) {
				log.warn("Unable to resolve title: '" + encodedTitle
						+ "' by pubpsych API");
				e.printStackTrace();
			}
			if (inputStream != null) {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream))) {
					return fetchRecord(reader, title);
				} catch (IOException e) {
					log.warn("Error trying to obtain pubpsych record for title: "
							+ title);
					e.printStackTrace();
				}
			}
		}
		return link;
	}

	/**
	 * Obtains the subject keywords from the pubpsych record.
	 * @param title
	 * @FIXME Currently no API call is made, we tacitly assume that method {@link PubPsychApiCaller#getPubPsychLinkByTitle(String)}
	 * has been called previously and has stored the pubpsych metadata record if it had been available.
	 *
	 * @param doi the doi of the article
	 * @return the list of subject terms
	 */
	public List<StringValue> getSubjectsByTitle(String title) {
		List<StringValue> subjects = titleKeywords.get(title);
		if (subjects != null) {
			return subjects;
		}
		return Collections.emptyList();
	}

	private String fetchRecord(BufferedReader reader, String title) throws IOException {
		String record = StringUtils.EMPTY;
		String id = null;
		String link = StringUtils.EMPTY;
		String line = StringUtils.EMPTY;

		while ((line = reader.readLine()) != null) {
			if (line.contains(STR_NAME_ID)) {
				record = record + line;
				id = line.substring(line.indexOf(STR_NAME_ID) + STR_NAME_ID.length());
				id = id.substring(0, id.indexOf("<"));
			}
		}
		if (record.isEmpty()) {
			log.info("No pubpsych record found for title: " + title);
		} else if (! record.contains("numFound=\"1\"")) {
			log.warn("More than one pubpsych record found for title: " + title);
		}
		if (id == null) {
			log.info("No pubpsych id found for title: " + title);
		} else {
			link = PUBPSYCH_LINK_PREFIX + id + PUBPSYCH_LINK_SUFFIX;
		}
		titlePubPsychLink.put(title, link);
		if (record.contains(SUBJECT_INDICATOR)) {
			extractSubjects(
					record.substring(record.indexOf(SUBJECT_INDICATOR) + SUBJECT_INDICATOR_LENGTH), title);
		}
		return link;
	}

	private List<StringValue> extractSubjects(String subs, String title) {
		List<StringValue> subjects = new ArrayList<>();
		int end = 0;
		while (end != -1) {
			int start = subs.indexOf(TAG_START);
			end = subs.indexOf(TAG_END);
			String sub = StringUtils.EMPTY;
			if (start != -1 && end != -1) {
				sub = subs.substring(start + TAG_START.length(), end);
				end = end + TAG_END.length();
				subs = subs.substring(end, subs.length());
			}
			if (!StringUtils.isEmpty(sub)) {
				subjects.add(new StringValue(sub));
			}
		}
		titleKeywords.put(title, subjects);
		return subjects;
	}

	public String get(String urlToGet) {
		HttpURLConnection conn = null;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			conn = openUrlConnection(new URL(urlToGet));
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			log.error("Error occurred during fetching information from pubpsych API: \n\t"
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
		PubPsychApiCaller caller = new PubPsychApiCaller();
		caller.getPubPsychLinkByTitle(
				"Depression Screening","Allgaier"
				);
	}
}
