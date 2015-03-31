package org.zpid.se4ojs.refStructuring;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zpid.se4ojs.refStructuring.bo.BOReference;

public class DocumentProcessor {

	private static final String REGEX_EDITOR_NAME_SPLITTER = "([A-Z]\\. )+";

	private static final String TAG_NAME = "name";

	private static final String TAG_PERSON_GROUP = "person-group";

	private static final String ATTR_TAG_PERSON_GROUP_TYPE = "person-group-type";

	private static final String ATTR_VAL_PERSON_GROUP_TYPE_AUTHOR = "author";
	private static final String ATTR_VAL_PERSON_GROUP_TYPE_EDITOR = "editor";

	private static final String ATTR_TAG_NAME_STYLE = "name-style";

	private static final String TAG_SURNAME = "surname";

	private static final String TAG_GIVEN_NAME = "given-names";

	private static final String ATTR_VAL_NAME_STYLE = "western";

	private static final String TAG_YEAR = "year";

	private static final String ATTR_TAG_PUBLICATION_TYPE = "publication-type";

	private static final String ATTR_VAL_PUB_TYPE_BOOK = "book";

	private static final String TAG_SECTION_TITLE = "chapter-title";

	private static final String TAG_SOURCE_TITLE = "source";

	private Document doc;

	public DocumentProcessor(Document doc) {
		this.doc = doc;
	}

	private void addAuthors(Element mixedCitation, List<String> authors,
			String attrValPersonGroupType) {
		if (!authors.isEmpty()) {
			Element personGroupElement = doc.createElement(TAG_PERSON_GROUP);
			Attr personGroupAttr = doc
					.createAttribute(ATTR_TAG_PERSON_GROUP_TYPE);
			personGroupAttr.setNodeValue(attrValPersonGroupType);
			personGroupElement.setAttributeNode(personGroupAttr);

			mixedCitation.appendChild(personGroupElement);
			for (String author : authors) {
				Element nameElement = (Element) personGroupElement
						.appendChild(doc.createElement(TAG_NAME));
				Attr nameAttr = doc.createAttribute(ATTR_TAG_NAME_STYLE);
				nameAttr.setTextContent(ATTR_VAL_NAME_STYLE);
				nameElement.setAttributeNode(nameAttr);
				String[] split = new String[2];
				if (attrValPersonGroupType
						.equals(ATTR_VAL_PERSON_GROUP_TYPE_AUTHOR)) {
					split = author.split(",");
					Element surnameElement = (Element) nameElement
							.appendChild(doc.createElement(TAG_SURNAME));
					surnameElement.setTextContent(split[0].trim());
				} else if (attrValPersonGroupType
						.equals(ATTR_VAL_PERSON_GROUP_TYPE_EDITOR)) {
					 Pattern pattern = Pattern.compile(REGEX_EDITOR_NAME_SPLITTER);
					 Matcher matcher = pattern.matcher(author);
					if (matcher.find()) {
						 split[0] = matcher.group();
					 }
					split[1] = author.substring(matcher.end());
					if (split[1] != null) {
						Element surnameElement = (Element) nameElement
								.appendChild(doc.createElement(TAG_SURNAME));
						surnameElement.setTextContent(split[1].trim());
					}
				}
				if (attrValPersonGroupType
						.equals(ATTR_VAL_PERSON_GROUP_TYPE_AUTHOR)) {
					if (split[1] != null) {
						Element givennameElement = (Element) nameElement
								.appendChild(doc.createElement(TAG_GIVEN_NAME));
						givennameElement.setTextContent(split[1].trim());
					}
				} else if (attrValPersonGroupType
						.equals(ATTR_VAL_PERSON_GROUP_TYPE_EDITOR)) {
					Element givennameElement = (Element) nameElement
							.appendChild(doc.createElement(TAG_GIVEN_NAME));
					givennameElement.setTextContent(split[0].trim());
				}
			}
		}
	}

	private void addYear(Element mixedCitation, String year) {
		mixedCitation.appendChild(doc.createTextNode(" ("));
		Element yearElement = doc.createElement(TAG_YEAR);
		yearElement.setTextContent(year);
		mixedCitation.appendChild(yearElement);
		mixedCitation.appendChild(doc.createTextNode("). "));
	}

	public void addReference(Element ref, BOReference reference) {
		NodeList childNodes = ref.getChildNodes();
		if (childNodes == null) {
			System.out.println("childNodes is null");//TODO log etc. also below.
		}
		Element mixedCitation = (Element) childNodes.item(0);
		if (mixedCitation == null) {
			System.out.println("citation is null");
		}
		if (reference.checkReferenceProcessed()) {
			if (reference.getReferenceType().equals(ReferenceType.BOOK)) {
				mixedCitation.setAttribute(ATTR_TAG_PUBLICATION_TYPE,
						ATTR_VAL_PUB_TYPE_BOOK);
			}
			mixedCitation.setTextContent(null);
			addAuthors(mixedCitation, reference.getAuthors(),
					ATTR_VAL_PERSON_GROUP_TYPE_AUTHOR);
			addYear(mixedCitation, reference.getYear());
			if (!reference.getSectionTitle().contentEquals(
					reference.getSourceTitle())) {
				addTitle(mixedCitation, reference.getSectionTitle(),
						TAG_SECTION_TITLE);
			}
			addAuthors(mixedCitation, reference.getEditors(),
					ATTR_VAL_PERSON_GROUP_TYPE_EDITOR);
			addTitle(mixedCitation, reference.getSourceTitle(),
					TAG_SOURCE_TITLE);
		}
		if (reference.getRest() != null) {
			mixedCitation.appendChild(doc.createTextNode(reference.getRest()));			
		}

	}

	private void addTitle(Element mixedCitation, String title, String titleTag) {
		Element titleElement = doc.createElement(titleTag);
		titleElement.setTextContent(title);
		mixedCitation.appendChild(titleElement);
	}

	// FIXME List<Name> extractEditorNames(List<String> editors) {
	// List<Name> names = new ArrayList<>();
	// for(String editor : editors) {
	// NameE name = new NameE();
	// String[] split = editor.trim().split(" ");
	// GivenNames givenNames = new GivenNames();
	// givenNames.setContent(split[0].trim());
	// Surname surname = new Surname();
	// surname.setContent(split[1].trim());
	// name.setContent(Arrays.asList(new Object[]{surname, givenNames}));
	// names.add(name);
	// }
	// return names;
	// }
}
