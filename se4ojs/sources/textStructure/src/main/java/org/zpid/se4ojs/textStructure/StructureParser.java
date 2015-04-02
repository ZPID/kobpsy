/**
 * 
 */
package org.zpid.se4ojs.textStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.zpid.se4ojs.textStructure.bo.BOCitation;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

/**
 * Parses section nodes and paragraph nodes contained therein.
 * 
 * @author barth
 *
 */
public class StructureParser {
	
	private enum StructureElementType {
		SECTION("sec"),
		PARAGRAPH("p");
		
		private final String label;

		private StructureElementType(String label) {
			this.label = label;
		}

	}

	private static final String ATTR_LANGUAGE = "lang";
	private static final String EL_TITLE = "title";
	private Logger log = Logger.getLogger(StructureParser.class);
	private Map<String, Integer> internalCitationCounts = new HashMap<String, Integer>();
	private String languages;
	private Namespace namespace;
	
	public StructureParser(String languages) {
		this.languages = languages;
	}

	/**
	 * <p>
	 * Parses the document for Structure elements, such as abstract, sub-articles, sections and paragraph.
	 * </p>
	 * <p>
	 * Structure elements whose language is not supported are not processed. 
	 * </p>
	 * 
	 * @param rootNode the article root node
	 * @param topLevelStructures the tree-like data structure that stores all structure elements
	 * @param languages the supported languages
	 * @return the list of structure elements
	 * @throws JDOMException
	 * @throws IOException
	 */
	public List<StructureElement> parse(Element rootNode, 
			List<StructureElement> topLevelStructures) throws JDOMException, IOException {
		namespace = rootNode.getNamespacesInherited().get(1);
		parseArticle(rootNode, topLevelStructures);
		List<Element> subArticles = rootNode.getChildren("sub-article");
		int subArtCount = 0;
		for (Element subArt : subArticles) {
			parseSubArticle(subArt, topLevelStructures, ++subArtCount);
		}
		return topLevelStructures;

	}

	private void parseArticle(Element rootNode, List<StructureElement> topLevelStructures) {
		parseAbstract(rootNode, topLevelStructures, "");
		Element body = rootNode.getChild("body");
		parseChildStructureElements(body, topLevelStructures, StringUtils.EMPTY);
	}

	private void parseSubArticle(Element subArt, List<StructureElement> container, int subArtCount) {
		String language = getLanguage(subArt);
		if (language != null) {
			String titleTag = "textual-entity/sub" + subArtCount;
			List<SectionType> types = getSectionTypes(subArt.getAttributeValue("sec-type"));
			BOSection sec = createSection(titleTag, types, container, "", language);
			BOSection abztract = parseAbstract(subArt, sec.getChildStructures(), "sub" + subArtCount);
			if (abztract != null) {
				sec.addChildStructure(abztract);			
			}
			Element body = subArt.getChild("body");
			parseChildStructureElements(body, sec.getChildStructures(), titleTag);			
		}
	}
	
	private void parseChildStructureElements(Element node,
			List<StructureElement> container, String parentTitle) {
		List<Element> children = node.getChildren();
		int paraCount = 0;
		for (Element child : children) {
			String name = child.getName();
			String language = getLanguage(child);
			if (language != null) {
				if(name.equals(StructureElementType.SECTION.label)) {
					List<SectionType> types = getSectionTypes(child.getAttributeValue("sec-type"));
					String titleTag = child.getChildText("title");
					BOSection sec = createSection(titleTag, types, container, parentTitle, language);
					parseChildStructureElements(child, sec.getChildStructures(), sec.getUriTitle());
				}
				if (name.equals(StructureElementType.PARAGRAPH.label)) {
					BOParagraph paraBo = createParagraph(child, container, ++paraCount, language);
					parseParagraph(child, paraBo);
				}
			}
		}
	}

	private String getLanguage(Element element) {
		if (element == null) {
			return null;
		}
		Attribute languageAttr = element.getAttribute(ATTR_LANGUAGE, namespace);
		if (languageAttr != null) {
			String lg = languageAttr.getValue().toLowerCase();
			if (languages.toLowerCase().contains(lg)) {
				return lg;
			}
			log.warn("Element (and children) skipped: Unsupported language in element: "
					+ element.getName()
					+ " with Title: "
					+ element.getChildText(EL_TITLE)
					+ " with text: \n\t"
					+ element.getText());
			return null;
		}
		return StructureElement.ARTICLE_LANGUAGE;
	}

	private List<SectionType> getSectionTypes(String type) {
		List<SectionType> types = new ArrayList<>();
		if (!StringUtils.isEmpty(type)) {
			StringTokenizer tokenizer = new StringTokenizer(type, "|");
			while (tokenizer.hasMoreTokens()) {
				types.add(
						SectionType.getSectionTypeFromLabel(tokenizer.nextToken().trim()));
			}			
		}
		return types;
	}

	private BOSection parseAbstract(Element node, List<StructureElement> topLevelStructures, String parentTitle) {
		Element front = node.getChild("front");
		if (front != null) {
			Element articleMeta = front.getChild("article-meta");
			if (articleMeta != null) {
				Element abztract = articleMeta.getChild("abstract");
				String language = null;
					language = getLanguage(abztract);				
				if (language != null) {
					BOSection section = createSection(
							"abstract", Collections.singletonList(SectionType.ABSTRACT),
							topLevelStructures, "", language);
					List<Element> abstractParagraphs = abztract.getChildren();
					int paraCount = 0;
					for (Element p : abstractParagraphs) {
						String pLanguage = getLanguage(p);
						if (pLanguage != null) {
							BOParagraph paraBo = createParagraph(p, section.getChildStructures(), ++paraCount, language);
							parseParagraph(p, paraBo);							
						}
					}	
					return section;
				}
			}
		}
		return null;
	}
	
	private void parseParagraph(Element p, BOParagraph paraBo) {
		List<Element> xrefs = p.getChildren("xref");
		for (Element xref : xrefs) {
			String refType = xref.getAttributeValue("ref-type");
			if (refType.equals("bibr")){
				BOCitation citation = createCitation(xref);
				paraBo.addCitation(citation);
				for (String rid : citation.getRids()) {
					Integer count = internalCitationCounts.get(rid);
					if (count == null) {
						count = 0;
					}
					internalCitationCounts.put(rid, ++count);
				}
			}
		}
	}

	private BOCitation createCitation(Element xref) {
		BOCitation citation = new BOCitation();
		String ridVal = xref.getAttributeValue("rid");
		List<String> rids = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(ridVal, " ");
		while(tokenizer.hasMoreTokens()) {
			rids.add(tokenizer.nextToken().trim());
		}
		citation.setRids(rids);
		citation.setText(xref.getText());
		return citation;
	}

	private BOSection createSection(String titleTag, List<SectionType> types,
			List<StructureElement> container, String parentTitle, String language) {
		
		String uriTitle = BOSection.createUriTitle(
				titleTag, types, container.size() + 1, parentTitle);
		BOSection section = new BOSection(titleTag, uriTitle, types, language);
		container.add(section);
		return section;
	}
	
	private BOParagraph createParagraph(Element p, List<StructureElement> container, int paraCount, String language) {
		BOParagraph paragraph = new BOParagraph(paraCount, p.getText(), language);
		container.add(paragraph);
		return paragraph;
	}

	public Map<String, Integer> getInternalCitationCounts() {
		return internalCitationCounts;
	}
	
}
