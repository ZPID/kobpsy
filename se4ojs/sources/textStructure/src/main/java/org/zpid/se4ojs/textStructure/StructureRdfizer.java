package org.zpid.se4ojs.textStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.zpid.se4ojs.textStructure.bo.BOCitation;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class StructureRdfizer {
	
	static final String URI_INFIX_DOI = "doi";
	static final String URI_SUFFIX_TEXTUAL_ENTITY = "textual-entity";
	private static final String C4O_HAS_CONTENT = "hasContent";
	private static final String C4O_TEXT_REF_PTR = "InTextReferencePointer";
	private static final String C4O_DENOTES = "denotes";
	private static final String C4O_HAS_CONTEXT = "hasContext";
	private static final String C4O_HAS_IN_TEXT_FREQ = "hasInTextCitationFrequency";
	private static final String CO_SIZE = "size";
	private static final String CO_INDEX = "index";
	private static final String CO_LIST = "List";
	private static final String CO_LIST_ITEM = "ListItem";
	private static final String CO_FIRST_ITEM = "firstItem";
	private static final String CO_NEXT_ITEM = "nextItem";
	private static final String CO_ITEM_CONTENT = "itemContent";
	private static final String DCTERMS_TITLE = "title";
	private static final String DOCO_SECTION = "Section";
	private static final String DOCO_PARAGRAPH = "Paragraph";
	private static final String PO_CONTAINS = "contains";
	private static final String RDF_TYPE = "type";
	private static final String RDF_TYPE_PROPERTY = createPropertyString(Prefix.RDF, RDF_TYPE);
	private static final String PO_STRUCTURED = "Structured";


	private Logger log = LogManager.getLogger(StructureRdfizer.class);
	private List<BOStructureElement> topLevelElements;
	private Document document;
	private String articleUri;
	private Map<String, Integer> citationCounts;
	private String baseUri;

	public StructureRdfizer(String baseUri, List<BOStructureElement> topLevelElements, Document document, Map<String, Integer> citationCounts) {
		this.baseUri = baseUri;
		this.topLevelElements = topLevelElements;
		this.document = document;
		this.citationCounts = citationCounts;
	}

	public Model rdfize() {
		Model model = RDF2Go.getModelFactory().createModel();	
		model.open();
		setNamespaces(model);
		articleUri = getArticleUri();
		model = rdfizeStructureElements(createUriString(articleUri, URI_SUFFIX_TEXTUAL_ENTITY), 
				topLevelElements, model);
		return model;
	}

	private Model rdfizeStructureElements(String parentUri, List<BOStructureElement> container, Model model) {
		int subItemCounter = 0;
		List<Model> childModels = new ArrayList<>();
		String prevListItem = null;
		
		for (BOStructureElement se : container) {
			String subElementUri = createSubElementUri(se, articleUri, parentUri);
			prevListItem = createOrderedList(parentUri, subElementUri, prevListItem, ++subItemCounter, model, container.size());
			createPartOfRelations(parentUri, subElementUri, model);

			if (se instanceof BOSection) {
				BOSection sec = (BOSection) se;
				List<BOStructureElement> childStructures = sec.getChildStructures();
				createTitleTriple(subElementUri, sec.getTitle(), model);
				createResourceTriple(
						subElementUri,
						RDF_TYPE_PROPERTY, createUriString(Prefix.DOCO.getURL(), DOCO_SECTION), model);
				createSectionTypeTriple(sec.getTypes(), subElementUri, model);
				if (!childStructures.isEmpty()) {
					Model childModel = RDF2Go.getModelFactory().createModel();
					childModel.open();
					childModels.add(rdfizeStructureElements(subElementUri, childStructures, childModel));
				}
			} else if (se instanceof BOParagraph) {
				BOParagraph p = (BOParagraph) se;
  				createResourceTriple(
						subElementUri,
						RDF_TYPE_PROPERTY, createUriString(Prefix.DOCO.getURL(), DOCO_PARAGRAPH), model);
				createLiteralTriple(
						subElementUri,
						createPropertyString(Prefix.C4O, C4O_HAS_CONTENT), p.getText(), model);
				rdfizeCitations(subElementUri, p, model);
			}
		}


		if (!childModels.isEmpty()) {
			for (Model m : childModels) {
				ClosableIterator<Statement> iterator = m.iterator();
				model.addAll(iterator);
			}
		}
		return model;
	}

	private void rdfizeCitations(String subElementUri, BOParagraph p, Model model) {
		int counter = 0;
		for (BOCitation citation : p.getCitations()) {
			StringBuilder ridUriPartBuilder = new StringBuilder();
			int ridcount = 0;
			for (String rid : citation.getRids()) {
				ridUriPartBuilder.append(rid);
				if (citation.getRids().size() > 1 && ridcount++ < citation.getRids().size()) {
					ridUriPartBuilder.append("_");
				}
			}
			String refPointerUri = createUriString(subElementUri, ridUriPartBuilder.toString(), Integer.toString(++counter));
			createResourceTriple(refPointerUri, createPropertyString(Prefix.C4O, C4O_HAS_CONTEXT), 
					subElementUri , model);
			createLiteralTriple(refPointerUri, createPropertyString(Prefix.C4O, C4O_HAS_CONTENT), 
					citation.getText() , model);
			createResourceTriple(refPointerUri, RDF_TYPE_PROPERTY, 
					createUriString(Prefix.C4O.getURL(), C4O_TEXT_REF_PTR) , model);
			for (String rid : citation.getRids()) {
				String refUri = createUriString(articleUri, "reference-" + rid);
				createResourceTriple(refPointerUri, createPropertyString(Prefix.C4O, C4O_DENOTES), 
						refUri , model);
				Integer count = citationCounts.get(rid);
				if (count != null) {
					createLiteralTriple(refUri, createPropertyString(Prefix.C4O, C4O_HAS_IN_TEXT_FREQ), 
							Integer.toString(count), XSD.nonNegativeInteger, model);
					citationCounts.remove(rid);		
				}
			}

		}
		
	}

	private void createSectionTypeTriple(List<SectionType> types, String subElementUri,
			Model model) {
		for (SectionType type : types) {
			if (type.getOntologyClass() != null) {
				createResourceTriple(subElementUri, RDF_TYPE_PROPERTY, 
						createPropertyString(type.getPrefix(), type.getOntologyClass()),
						model);				
			}
		}
	}

	private String createOrderedList(String parentUri, String subElementUri, String prevListItem,
			int counter, Model model, int listSize) {

		String listItemType = createUriString(Prefix.CO.getURL(), CO_LIST_ITEM);
		String listItemIndividual = createUriString(parentUri, "_listItem", Integer.toString(counter));
		String itemContent = subElementUri;
		if (counter == 1) {
			String list = new StringBuffer(parentUri).append("_list").toString();
			createResourceTriple(list,
					RDF_TYPE_PROPERTY, createUriString(Prefix.CO.getURL(), CO_LIST), model);
			createResourceTriple(list,
					RDF_TYPE_PROPERTY, createUriString(Prefix.PO.getURL(), PO_STRUCTURED), model);
			createResourceTriple(parentUri, createPropertyString(Prefix.PO, PO_CONTAINS), list, model);
			createLiteralTriple(list, createPropertyString(Prefix.CO, CO_SIZE), Integer.toString(listSize), XSD.nonNegativeInteger, model);
			createResourceTriple(list, createPropertyString(Prefix.CO, CO_FIRST_ITEM), listItemIndividual, model);
		} else {
			createResourceTriple(prevListItem, createPropertyString(Prefix.CO, CO_NEXT_ITEM) , listItemIndividual, model);
		}
		createLiteralTriple(listItemIndividual, createPropertyString(Prefix.CO, CO_INDEX) , Integer.toString(counter), XSD.nonNegativeInteger, model);
		createResourceTriple(listItemIndividual, RDF_TYPE_PROPERTY , listItemType, model);
		createResourceTriple(listItemIndividual, createPropertyString(Prefix.CO, CO_ITEM_CONTENT) , itemContent, model);
		return listItemIndividual;
	}

	private String createSubElementUri(BOStructureElement se, String articleUri, String parentUri) {
		if (se instanceof BOSection) {
			return new StringBuffer(articleUri).append("/").append(se.getUriTitle()).toString();
		} else if (se instanceof BOParagraph) {
			return createParagraphFullUriTitle(parentUri, (BOParagraph) se);
		}
		return null;
	}

	private String createParagraphFullUriTitle(String parentUri,
			BOParagraph p) {
		return new StringBuilder(parentUri).append("_").append(p.getUriTitle()).toString();
	}

	private void createTitleTriple(String subElementUri, String title, Model model) {
		if (title != null) {
			createLiteralTriple(subElementUri,
					createPropertyString(Prefix.DCTERMS, DCTERMS_TITLE), title, model);				
		}
	}
	
	private void createPartOfRelations(String parentUri, String subElementUri, Model model) {
		model.addStatement(
				model.createURI(parentUri),
				model.createURI(Prefix.PO.getURL() + PO_CONTAINS),
				model.createURI(subElementUri));
	}


	/**
	 * Creates a plain literal.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param model
	 */
	private void createLiteralTriple(String subject,
			String predicate, String object, Model model) {
		model.addStatement(
				model.createURI(subject),
				model.createURI(predicate),
		        model.createPlainLiteral(object));
	}

	/**
	 * Creates a typed literal.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param resource the datatype
	 * @param model
	 */
	private void createLiteralTriple(String subject,
			String predicate, String object,
			Resource resource, Model model) {
		model.addStatement(
				model.createURI(subject),
				model.createURI(predicate),
		        model.createDatatypeLiteral(object, model.createURI(resource.toString())));
	}

	private void createResourceTriple(String subject,
			String predicate, String object, Model model) {
		model.addStatement(model.createURI(subject), model.createURI(predicate), model.createURI(object));
	}

	private String createUriString(String... uriParts) {
		StringBuilder sb = new StringBuilder();
		for (String part : uriParts) {
			sb.append(part);
			if(!part.endsWith("/") && (!part.endsWith("#"))){
				sb.append("/");
			}
		}
		if (sb.lastIndexOf("/") == sb.length() - 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}


	private static String createPropertyString(Prefix p, String pred) {
		return
				new StringBuilder(p.getURL()).append(pred).toString();
	}
	
	private void setNamespaces(Model model) {
		for (Prefix p : Prefix.values()) {
			model.setNamespace(p.getNS(), p.getURL());
		}
	}

	private String getArticleUri() {
		Element rootElement = document.getRootElement();
		List<Element> articleIds = rootElement.getChild("front").getChild("article-meta").getChildren("article-id");
		for (Element articleId : articleIds) {
			if (articleId.getAttributeValue("pub-id-type").equals("doi")) {
				return createUriString(baseUri, URI_INFIX_DOI, articleId.getText()).toString();
			}
		}
		log.error("Unable to extract article URI.");
		return null;
	}

}
