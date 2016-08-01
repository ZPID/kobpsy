/**
 *
 */
package org.zpid.se4ojs.refStructuring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zpid.se4ojs.refStructuring.bo.BOReference;

;

/**
 * <p>
 * This class structures unstructured references: Authors, title, year and
 * editors are extracted from references which only contain this information as
 * strings. The extracted information is stored in the passed in
 * {@link BOReference}.
 * </p>
 *
 * @author barth
 *
 */
public class ReferenceStructurer {

	private static final String JATS__1_0_XSD = "/jats-publishing-xsd-1.0/JATS-journalpublishing1.xsd";

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	private static final String REGEX_AUTHOR_NAMES = "[A-Z]{1}[a-z]+, ([A-Z]{1}(\\.|[a-z]+) *)+";
	private static final String REGEX_EDITOR_NAMES = "([A-Z]{1}\\. )+([A-Za-z]{3,})";

	/**
	 * Regex to identify the standard APA syntax for publication year, e.g.
	 * "(1992)".
	 */
	private static final String REGEX_YEAR = "\\(\\d{4}\\)";

	/**
	 * Regex to identify a substring containing the year, e.g. "(1992, September
	 * 15).
	 */
	private static final String REGEX_YEAR_EXTENDED = "\\(\\d{4}(.)*\\)";

	/** Regex to identify a year, e.g. 1992 */
	private static final String REGEX_YEAR_FOUR_DIGITS = "(\\d{4})";

	private static final int MIN_STRING_LENGTH = 3;

	private static final String REF_TAG = "ref";
	private static final String TAG_AUTHOR_GROUP = "person-group";
	private static final String ATTR_ID = "id";
	private static final String TAG_ITALIC = "italic";
	private static final String ATTR_TAG_REF_TYPE = "publication-type";

	private int parsePosition = 0;
	private Logger log = LogManager.getLogger(ReferenceStructurer.class);

	/** Pre-compiled patterns for string extraction. */
	private Pattern yearPatternExtended = Pattern.compile(REGEX_YEAR_EXTENDED);
	private Pattern yearFourDigitsPattern = Pattern
			.compile(REGEX_YEAR_FOUR_DIGITS);
	private Pattern authorPattern = Pattern.compile(REGEX_AUTHOR_NAMES);
	private Pattern editorPattern = Pattern.compile(REGEX_EDITOR_NAMES);
	private Pattern yearPattern = Pattern.compile(REGEX_YEAR);

	private File inFile;
	private File outFile;
	private Document doc;
	private DocumentProcessor docProcessor;

	private BOReference reference;


	public ReferenceStructurer(File inFile, File outFile, String baseUri) {
		super();
		this.inFile = inFile;
		this.outFile = outFile;
	}

	public void extractReferences() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
					false);
			factory.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);

			factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			String schemaSource = getClass().getResource(JATS__1_0_XSD)
					.toString();
			factory.setAttribute(JAXP_SCHEMA_SOURCE, new File(schemaSource));
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(inFile);
			docProcessor = new DocumentProcessor(doc);
			processUnstructuredReferences(doc.getElementsByTagName(REF_TAG));
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(new DOMSource(doc), new StreamResult(outFile));
		} catch (ParserConfigurationException | SAXException | IOException
				| TransformerException e) {
			String msg = "Unable to parse input xml File. "
					+ inFile.getAbsolutePath() + " cause: " + e.getMessage();
			log.error(msg);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Iterates over the content of a reference.
	 *
	 * @param refs
	 *
	 * @param content
	 *            the content of the reference
	 * @return the {@link BOReference} that store the extracted information
	 */
	void processUnstructuredReferences(NodeList refs) {
		if (refs.getLength() > 0) {
			for (int i = 0; i < refs.getLength(); i++) {
				Element ref = (Element) refs.item(i);
				System.out.println(ref.getTextContent()); // TODO delete
				NodeList authorGroup = ref
						.getElementsByTagName(TAG_AUTHOR_GROUP);
				reference = new BOReference();
				reference.setInternalReferenceId(ref.getAttribute(ATTR_ID));
				if (authorGroup.getLength() == 0) {
					log.warn("Unstructured reference: "
							+ reference.getInternalReferenceId()
							+ " , trying to parse it: \n	"
							+ ref.getTextContent());
					Element mixedCitation = (Element) ref.getFirstChild();
					String refType = mixedCitation
							.getAttribute(ATTR_TAG_REF_TYPE);
					if (StringUtils.isEmpty(refType)) {
						log.error("Unable to extract reference type");
					}
					reference.setReferenceType(ReferenceType
							.getReferenceType(refType));
					parsePosition = 0;
					doProcess(ref.getTextContent(), ref);
					if (reference.getAuthors().isEmpty()
							|| reference.getYear() == null
							|| StringUtils.isEmpty(reference.getSourceTitle())) {
						log.error(reference.getInternalReferenceId()
								+ " Unable to process unstructured reference! \n	 No. of Authors: "
								+ reference.getAuthors().size() + " Year: "
								+ reference.getYear() + " Title: "
								+ reference.getSourceTitle());
						// throw new
						// IllegalStateException(reference.getInternalReferenceId()
						// + " Incomplete Reference!"); //TODO remove
					} else {
						try {
							docProcessor.addReference(ref, reference);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				// FIXME } else if (obj instanceof ExtLink) {
				// ExtLink link = (ExtLink) obj;
				// reference.setPublicationLink((String)
				// link.getContent().get(0));
				// log.info("Link: " + reference.getPublicationLink());
				// } else {
				// log.error(reference.getInternalReferenceId() +
				// " Unexcpected Object in reference: " + obj.getClass());
			}
		}
	}

	/**
	 * Coordinates the different extraction tasks: The references come in a
	 * predefined sequence (1st: author, 2nd: year, 3rd: title,...). The
	 * reference may contain all information in one string or in separate
	 * strings, this must be taken into account.
	 *
	 * @param str
	 *            part of a reference.
	 */
	private void doProcess(String str, Element ref) {
		try {
			boolean sourceTitleProcessed = false;
			if (!StringUtils.isEmpty(str) && str.length() > MIN_STRING_LENGTH) {
				System.out.println(str);
				if (reference.getAuthors().isEmpty()) {
					reference.setAuthors(parseAuthorString(str));
				}
				if (StringUtils.isEmpty(reference.getYear())) {
					reference.setYear(parseYearString(str));
					log.info(reference.getInternalReferenceId() + " Year: *"
							+ reference.getYear() + "*");
				}
				if (StringUtils.isEmpty(reference.getSectionTitle())) {
					if (parsePosition != -1
							&& parsePosition < str.length() - MIN_STRING_LENGTH
									+ 1) {
						str = str.substring(parsePosition);
						if (str.trim().startsWith(".")) {
							str = str.substring(1);
						}
						String title = parseTitle(str.trim());
						reference.setSectionTitleAndId(title.trim());
						reference.setSourceTitleAndSourceId(title.trim());
					}
				}
				if (reference.getSectionTitle() != null
						&& reference.getEditors().isEmpty()) {
					if (parsePosition != -1 && str.length() > MIN_STRING_LENGTH
							&& parsePosition < str.length() - MIN_STRING_LENGTH) {
						str = str.substring(parsePosition + 1);
						reference.setEditors(parseEditorString(str));

					}
				}
				if (!reference.getEditors().isEmpty() && !sourceTitleProcessed) {
					str = getRestString(str);
					String sourceTitle = parseSourceTitle(str, ref);
					if (!sourceTitle.isEmpty()) {
						reference.setSourceTitleAndSourceId(sourceTitle);
						parsePosition = str.indexOf(sourceTitle)
								+ sourceTitle.length();
						str = getRestString(str);
					} else {
						if (sourceTitleProcessed) {
							log.error(reference.getInternalReferenceId()
									+ "No source title found!");
						}
					}
				}
				if (str.length() >= MIN_STRING_LENGTH) {
					reference.setRest(str);
				}
				// TODO process source's page start and page end (see 671, r3)
				// TODO process: reference.getPublisherName();
				// TODO process: reference.getPublisherLocation()
				// TODO process references of type external link

			}
		} catch (Exception e) {
			log.error(" "
					+ reference.getInternalReferenceId()
					+ " ex: "
					+ e.fillInStackTrace());
			e.printStackTrace();
		}

	}

	private String getRestString(String str) {
		if (parsePosition != -1
				&& str.length() > parsePosition + MIN_STRING_LENGTH) {
			str = str.substring(parsePosition);
		}
		return str;
	}

	String parseSourceTitle(String str, Element ref) {
		NodeList italics = ref.getElementsByTagName(TAG_ITALIC);
		for (int i = 0; i < italics.getLength(); i++) {
			Element italic = (Element) italics.item(i);
			String italicText = italic.getTextContent();
			if (str.contains(italicText)) {
				log.info(reference.getInternalReferenceId() + "Source Title: "
						+ italicText.trim());
				return italicText.trim();
			}
		}
		return StringUtils.EMPTY;
	}

	private List<String> parseEditorString(String str) {
		List<String> editors = new ArrayList<>();
		if (str.startsWith(". In ")) {

			String ed = "(Ed.)";
			int editorsEnd = str.indexOf(ed);
			int editorStringLength = ed.length();
			if (editorsEnd == -1) {
				String eds = "(Eds.)";
				editorsEnd = str.indexOf(eds);
				editorStringLength = eds.length();
			}
			if (editorsEnd == -1) {
				log.error(reference.getInternalReferenceId()
						+ " End of editor substring not found: " + str);
				throw new IllegalStateException(
						reference.getInternalReferenceId()
								+ " End of editor substring not found: " + str); // TODO
																					// remove
			} else {
				parsePosition = editorsEnd + editorStringLength;
				for (MatchResult r : findMatches(editorPattern, str)) {
					System.out.println(r.group() + " von " + r.start()
							+ " bis " + r.end());
					log.info(reference.getInternalReferenceId() + " editor: "
							+ r.group());
					editors.add(r.group());
				}
			}
		}
		return editors;
	}

	/**
	 * Returns the first match of the regular expression as the title.
	 *
	 * @param restString
	 */
	String parseTitle(String restString) {
		boolean titleEnd = false;
		int posFullStop = -1;
		int posStart = 0;
		while (!titleEnd) {
			posFullStop = restString.indexOf(".", posStart);
			String wordPreceedingFullStop = StringUtils.EMPTY;
			if (posFullStop != -1) {
				wordPreceedingFullStop = getLastWordInString(restString
						.substring(0, posFullStop));

			}
			if (wordPreceedingFullStop.trim().length() > 1 || posFullStop == -1) {
				titleEnd = true;
			}
			posStart = posFullStop + 1;
		}
		if (posFullStop == -1) {
			log.warn(reference.getInternalReferenceId()
					+ " Title could not be extracted. Taking entire string for title: "
					+ restString);
			parsePosition = restString.length() - 1;
			return restString.trim();
		}

		String title = restString.substring(0, posFullStop + 1);
		log.info(reference.getInternalReferenceId() + " Title: *" + title + "*");

		parsePosition = posFullStop;
		return title.trim();
	}

	private String getLastWordInString(String str) {
		int blank = str.lastIndexOf(" ");
		if (blank == -1) {
			return str;
		}
		return str.substring(blank);
	}

	private List<String> parseAuthorString(String str) {
		List<String> authors = new ArrayList<>();
		if (str.contains("(")) {
			str = str.substring(0, str.indexOf("("));
			if (str.length() <= 0) {
				log.error(reference.getInternalReferenceId()
						+ "Author substring could not be identified.");
				throw new IllegalStateException(
						reference.getInternalReferenceId()
								+ "Author substring could not be identified."); // TODO
																				// remove
			} else {
				for (MatchResult r : findMatches(authorPattern, str)) {
					System.out.println(r.group() + " von " + r.start()
							+ " bis " + r.end()); // TODO remove
					log.info(reference.getInternalReferenceId() + " author: "
							+ r.group());
					authors.add(r.group());
					parsePosition = r.end();
				}
			}
		}
		return authors;
	}

	String parseYearString(String str) {
		Matcher matcher = yearPattern.matcher(str);
		if (matcher.find()) {
			String year = matcher.group();
			parsePosition = matcher.end();
			return (year.substring(1, year.length() - 1));
		}
		if (reference.getYear() == null) {
			matcher = yearPatternExtended.matcher(str);
			if (matcher.find()) {
				parsePosition = matcher.end();
				String yearCand = matcher.group();
				matcher = yearFourDigitsPattern.matcher(yearCand);
				if (matcher.find()) {
					return matcher.group();
				}
			}
		}

		if (reference.getYear() == null) {
			log.error(reference.getInternalReferenceId() + "No year found");
			// throw new
			// IllegalStateException(reference.getInternalReferenceId() +
			// "No year found");//TODO remove
		}
		return reference.getYear();
	}

	List<MatchResult> findMatches(Pattern pattern, String str) {
		List<MatchResult> results = new ArrayList<MatchResult>();
		for (Matcher m = pattern.matcher(str); m.find();) {
			results.add(m.toMatchResult());
		}
		return results;
	}
}
