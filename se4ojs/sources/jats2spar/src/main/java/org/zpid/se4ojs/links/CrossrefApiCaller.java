/**
 * 
 */
package org.zpid.se4ojs.links;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zpid.se4ojs.app.Config;

/**
 * Class that performs CrossRef Api Calls.
 * 
 * @author barth
 *
 */
public class CrossrefApiCaller {

	private static final int PDF_FILE_ENDING_LENGTH = 3;

	private static final String DOI_PREFIX_URI = "http://dx.doi.org/";
	
	private static final String PDF_LINK_INDICATOR = "name=\"citation_pdf_url\" content=\"";
	private static final String PDF_FILE_ENDING = "pdf\"";

	private static final int PDF_LINK_INDICATOR_LENGTH = PDF_LINK_INDICATOR.length();
	
	private Logger log = LogManager.getLogger(CrossrefApiCaller.class);
	

	/**
	 * Returns the URL to the PDF manifestation of the article with the given doi,
	 * or null if it could not be retrieved.
	 * 
	 * @param doi the article doi
	 * @return the URL to the PDF
	 */
	public String getPdfByDoi(String doi) {
		if (Config.isGenerateCrossrefApiPdf()) {
			String pdfLink = null;
			InputStream inputStream = null;
			try {
				URLConnection urlConnection = new URL(DOI_PREFIX_URI + doi).openConnection();
				urlConnection.setRequestProperty("Content-Type", "application/vnd.crossref.unixsd+xml");
				urlConnection.connect();
				inputStream = urlConnection.getInputStream();
			} catch (IOException e) {
				log.warn("Unable to resolve doi: " + doi + " by crossref API");
//				e.printStackTrace();
			}
			if (inputStream != null) {
				try (BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream))) {
					pdfLink = extractPdfLink(reader, doi);
				} catch (IOException e) {
					log.warn("Error trying to obtain PDF link for doi: " + doi);
					e.printStackTrace();
				} 
			}
			if (pdfLink != null) {
				return pdfLink;
			}
		}
		return StringUtils.EMPTY;
	}
	
	private String extractPdfLink(BufferedReader reader, String doi) throws IOException {
		String s = null;
		while ((s = reader.readLine()) != null) {

			String normalizedS = s.toLowerCase();
			if (normalizedS.contains(PDF_LINK_INDICATOR)) {

				int beginIndex = normalizedS.indexOf(PDF_LINK_INDICATOR)
						+ PDF_LINK_INDICATOR_LENGTH;
				int endIndex = normalizedS.lastIndexOf(PDF_FILE_ENDING)
						+ PDF_FILE_ENDING_LENGTH;

				if (beginIndex > PDF_LINK_INDICATOR_LENGTH
						&& endIndex > PDF_FILE_ENDING_LENGTH) {

					s = s.substring(beginIndex, endIndex);
				} else {
					log.info("No link to PDF version found for doi: " + doi);
				}
				return s;
			}
		}
		return null;
	}

}
