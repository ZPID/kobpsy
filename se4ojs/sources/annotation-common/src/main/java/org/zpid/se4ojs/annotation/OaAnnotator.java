package org.zpid.se4ojs.annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.xml.sax.InputSource;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

import com.hp.hpl.jena.vocabulary.XSD;

/**
 * <p>
 * An abstract Annotator that handles the common workflow of annotating 
 * the textual content of a JATs document.
 * </p>
 * <p>
 * The textual content is passed into this annotator as a hierarchy of
 * {@link StructureElement}s.
 * </p>
 * 
 * @author barth
 */
public abstract class OaAnnotator {
	
	protected static final String OA_ANNOTATION = "Annotation";
	protected static final String RDF_TYPE = "type";
	protected static final String RDF_TYPE_PROPERTY = AnnotationUtils.createPropertyString(Prefix.RDF, RDF_TYPE);
	protected static final String OA_HAS_BODY = "hasBody";
	protected static final String OA_HAS_TARGET = "hasTarget";
	protected static final String OA_HAS_SOURCE = "hasSource";
	protected static final String OA_HAS_SELECTOR = "hasSelector";
	protected static final String OA_AUTHORED_BY = "authoredBy";
	protected static final String OA_CREATION_DATE = "createdOn";
	protected static final String OA_EXACT_MATCH = "exact";
	protected static final String OA_SEMANTIC_TAG = "SemanticTag";
	protected static final String OA_SPECIFIC_RESOURCE = "SpecificResource";
	protected static final String FOAF_PAGE = "page";
	protected static final String CNT_TEXT = "ContentAsText";
	protected static final String CNT_CHARS = "chars";
	protected static final String OA_START_POS = "start";
	protected static final String OA_END_POS = "end";
	protected static final String OA_COMPOSITE = "Composite";
	protected static final String OA_ITEM = "item";
	protected static final String OA_FRAGMENT_SELECTOR = "FragmentSelector";
	protected static final String OA_TEXT_POS_SELECTOR = "TextPositionSelector";
	protected static final String OA_TEXT_QUOTE_SELECTOR = "TextQuoteSelector";
	private static final String LANGUAGE_EN = "en";

	private String articleUri;
	private Logger log  = Logger.getLogger(OaAnnotator.class);
	
	/** Stores a concept by its URI as a key and the number of times it occurs in the same paper as value. */
	private Map<String, Integer> conceptCount = new HashMap<>();

	public void annotate(String baseUri, File paper, List<StructureElement> structureElements, Path outFile) throws IOException {
		Model model = RDF2Go.getModelFactory().createModel();
		model.open();
		AnnotationUtils.setNamespaces(model);
		Document document = createDocumentFromPaper(paper);
		articleUri = AnnotationUtils.getArticleUri(document, baseUri);
		model = annotateParagraphs(articleUri, structureElements, model);
	    OutputStream os = new FileOutputStream(new File(outFile.toString()));
	    try {
	    	com.hp.hpl.jena.rdf.model.Model jenaModel = 
	    			(com.hp.hpl.jena.rdf.model.Model)model.getUnderlyingModelImplementation();
			jenaModel.write(os);
			model.close();
			jenaModel.close();
		} catch (ModelRuntimeException e) {
			throw new FileNotFoundException("ModelRuntimeException " + e.getMessage());
		} 
	    model.close();
		os.close();
		log.info("Finished annotation for paper: " + paper.getName());
	}

	/**
	 * Builds a document from the input (xml-) file using a SAXBuilder.
	 * 
	 * @param paper the input JATS-XML-File
	 * @return the jdom2 document
	 */
	private Document createDocumentFromPaper(File paper) {
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setFeature("http://xml.org/sax/features/validation", false);
		builder.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
				false);
		builder.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd",
				false);
		InputSource is;
		is = new InputSource(paper.getAbsolutePath());
		is.setEncoding("UTF-8");
		Document document = null;
		try {
			document = (Document) builder.build(is);
		} catch (JDOMException | IOException e) {
			log.error("Annotation error - unable to read input document");
			e.printStackTrace();
		}
		return document;
	}

	private Model annotateParagraphs(String parentUri, List<StructureElement> container, Model model) {
		List<Model> childModels = new ArrayList<>();
		
		for (StructureElement se : container) {
			String subElementUri = AnnotationUtils.createSubElementUri(se, articleUri, parentUri);

			if (se instanceof BOSection) {
				BOSection sec = (BOSection) se;
				List<StructureElement> childStructures = sec.getChildStructures();

				if (!childStructures.isEmpty()) {
					Model childModel = RDF2Go.getModelFactory().createModel();
					childModel.open();
					childModels.add(annotateParagraphs(subElementUri, childStructures, childModel));
				}
			} else if (se instanceof BOParagraph) {
				BOParagraph p = (BOParagraph) se;
				try {
					String text = p.getText().replaceAll("[\\t\\n\\r]"," ");
					if (!StringUtils.isEmpty(text) && text.trim().length() > 0  && isSupportedLanguage(p)) {
						annotateText(model, text, subElementUri);
					}
				} catch (Exception e) {
					log.error("Error annotating paragraph " + subElementUri + " : " + e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}


		if (!childModels.isEmpty()) {
			for (Model m : childModels) {
				ClosableIterator<Statement> iterator = m.iterator();
				model.addAll(iterator);
				m.close();
			}
		}
		return model;
	}
	
	private boolean isSupportedLanguage(BOParagraph p) {
		if (p.getLanguage() != null && !p.getLanguage().equals(LANGUAGE_EN)) {
			return false;
		}
		return true;
	}

	/**
	 * Annotates the passed in text with concepts from pre-configured ontologies.
	 * The implementing classes calling this method are determined by the specifications
	 * in the configuration file.
	 * 
	 * @param model the RDF2Go model
	 * @param text the text to annotate
	 * @param subElementUri the ID of the text structure element that contains the passed in text 
	 * @throws Exception
	 */
	public abstract void annotateText(Model model, String text, String subElementUri) throws Exception;

	public String getArticleUri() {
		return articleUri;
	}
	
	/**
	 * Creates the ao:Annotation:
	 * Creates the URI by which the annotation is referenced.
	 * Creates a triple that describes that the entity referenced by this URI is of type Annotation.
	 * Returns the URI of the Annotation instance for further use in other triples.
	 * 
	 * @param model the RDF model.
	 * @param id the id of the annotated concept
	 * @param name the name of the annotated concept
	 * @return the URI by which the annotation is referenced
	 */
	public String createAnnotation(Model model, String id, String name) {
		  String url = createConceptUri(id, name);
			AnnotationUtils.createResourceTriple(
					url,
					RDF_TYPE_PROPERTY,
					AnnotationUtils.createUriString(Prefix.OA.getURL(), OA_ANNOTATION), model);
			return url;
	}

	private String createConceptUri(String id, String name) {
		return AnnotationUtils.createUriString(
				  articleUri, AnnotationUtils.urlEncode(id));
	}
	
	/**
	 * Creates a triple that relates the target to the article that is annotated by the annotation.
	 * Example:
	 * {@code
	 * <rdf:Description rdf:about="urn:uuid:6C97B503-25EE-4E37-875C-B7C850E13194"> 
	 * 	  <oa:hasSource rdf:resource="http://www.zpid.de/resource/doi/10.5964/ejcop.v2i2.34/textualEntity"/>
     *    ...
	 * }
	 * 
	 * @param model the RDF2Go model
	 * @param targetId the id of the target
	 */
	protected void relateToArticle(Model model, String targetId) {	
		AnnotationUtils.createResourceTriple(
				targetId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_SOURCE), 
				articleUri, model);	
	}
	
	public void addToConceptCount(String conceptUri) {
		Integer count = conceptCount.get(conceptUri);
		if (count == null) {
			count = 0;
		}
		conceptCount.put(conceptUri, ++count);
	}
	

	public void createPositionalTriples(Model model, String aoContext, int start, int end) {
		AnnotationUtils.createLiteralTriple(aoContext,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_START_POS),
				Integer.toString(start), XSD.nonNegativeInteger, model);
		AnnotationUtils.createLiteralTriple(aoContext,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_END_POS),
				Integer.toString(end), XSD.nonNegativeInteger, model);
		
	}

	/**
	 * 
	 * @param model
	 * @param aoContext
	 * @param matchedWords
	 */
	public void addExactMatch(Model model, String aoContext,
			String matchedWords) {
		AnnotationUtils.createLiteralTriple(aoContext,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_EXACT_MATCH),
				matchedWords, model);
	}
	
	/**
	 * Creates the body of the annotation.
	 * This is one triple consisting of the AnnotationUri as subject,
	 * the oa:hasBody predicate and the conceptID as object.
	 * Example: {@code 
	 * <
	 * <rdf:Description rdf:about="http://www.zpid.de/resource/CHEBI_33232">
	 * oa:hasBody rdf:resource="http://purl.obolibrary.org/obo/CHEBI_33232"/>
	 * } 
     *
	 * @param model the RDF2Go model
	 * @param annotationUri the annotation ID
	 * @param conceptId
	 * 
	 * @return the URI of the body
	 */
	protected String createBody(Model model, String annotationUri, String conceptId) {
		String bodyUri = AnnotationUtils.createUriString(conceptId);
		AnnotationUtils.createResourceTriple(annotationUri,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_BODY),
				bodyUri, model);
		return bodyUri;
	}

	/**
	 * Creates the "body"-information of the annotation.
	 * Subject is the annotation body. The body's type and concept name are specified.
	 * Example: 
	 * {@code
	 * <rdf:Description rdf:about="http://purl.obolibrary.org/obo/CHEBI_33232"/>
	 *   <rdf:type rdf:resource="http://www.w3.org/2011/content#ContentAsText"/>
	 *   <cnt:chars>application</cnt:chars>
     *   <rdf:type rdf:resource="http://www.w3.org/ns/oa#SemanticTag"/>
	 *   <foaf:page rdf:resource="http://bioportal.bioontology.org/ontologies/NIFSTD?p=classes&amp;conceptid=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2Fchebi.owl%23CHEBI_33232"/>
	 * }
	 * 
	 * @param model the RDF2Go model
	 * @param bodyUri the URI of the body
	 * @param conceptPrefLabel the preferred label of the concept
	 * @param conceptWebPage the URL to the web page of the concept
	 */
	protected void addBodyInfo(Model model, String bodyUri, String conceptPrefLabel, String conceptWebPage) {
		AnnotationUtils.createResourceTriple(bodyUri, RDF_TYPE_PROPERTY, 
				AnnotationUtils.createUriString(Prefix.OA.getURL(), OA_SEMANTIC_TAG), model);
		AnnotationUtils.createResourceTriple(bodyUri, RDF_TYPE_PROPERTY, 
				AnnotationUtils.createUriString(Prefix.CNT.getURL(), CNT_TEXT), model);
		AnnotationUtils.createResourceTriple(bodyUri, AnnotationUtils.createPropertyString(Prefix.FOAF, FOAF_PAGE)
				, conceptWebPage, model);
		AnnotationUtils.createLiteralTriple(bodyUri, 
				AnnotationUtils.createPropertyString(Prefix.CNT, CNT_CHARS), conceptPrefLabel, model);
	}

	/**
	 * Creates annotation meta-info: The author by whom the annotation has been created 
	 * and the date when it has been created.
	 * Example:
	 * {@code
	 *  <rdf:Description rdf:about="http://www.zpid.de/resource/CHEBI_33232"> 
	 * 	<oa:annotatedBy rdf:resource="http://bioportal.bioontology.org/annotator/"/>
	 *  <oa:annotatedAt rdf:datatype="http://www.w3.org/2001/XMLSchema#date">2015-06-30</oa:annotatedAt>
	 *  ...}
	 * @param model the RDF2Go model
	 * @param annotationId the URI of the Annotation
	 * @param authorID the identifier of the author
	 */
	protected void addAnnotationMetaInfo(Model model, String annotationId, String authorID) {
		AnnotationUtils.createLiteralTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_AUTHORED_BY),
				authorID, model);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		AnnotationUtils.createLiteralTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_CREATION_DATE),
				dateFormat.format(new Date()), XSD.date, model);
	}
	
	protected void createTarget(Model model, String annotationUri, String targetId) {
		AnnotationUtils.createResourceTriple(annotationUri, 
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_TARGET), targetId, model);
	}
	
	protected void addTargetType(Model model, String targetId) {
		AnnotationUtils.createResourceTriple(targetId, RDF_TYPE_PROPERTY, 
				AnnotationUtils.createUriString(Prefix.OA.getURL(), OA_SPECIFIC_RESOURCE), model);
	}

	/**
	 * Adds a composite selector to the target. Also creates the triple
	 * that determines the type of the selector.
	 * Returns the id of the composite selector.
	 * 
	 * Example: 
	 * {@code
	 * <rdf:Description rdf:about="urn:uuid:6C97B503-25EE-4E37-875C-B7C850E13194"/>
	 *    <oa:hasSelector rdf:resource="urn:uuid:7C97B503-25EE-4E37-875C-B7C850E13194"/> 
     *    ...
     * <rdf:Description rdf:about="urn:uuid:7C97B503-25EE-4E37-875C-B7C850E13194"> 
	 * s<rdf:type rdf:resource="http://www.w3.org/ns/oa#Composite"/>
     *
	 * }
	 * @param model the RDF2Go model
	 * @param targetId the id of the target
	 * @return the composite selector id
	 */
	protected String addCompositeSelector(Model model, String targetId) {
		String compSelectorId = AnnotationUtils.generateUuidUri();
		AnnotationUtils.createResourceTriple(targetId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_SELECTOR),
				compSelectorId, model);
		AnnotationUtils.createResourceTriple(compSelectorId, RDF_TYPE_PROPERTY,
				AnnotationUtils.createUriString(Prefix.OA.getURL(), OA_COMPOSITE), model);
		return compSelectorId;
	}
	
	/**
	 * <p>
	 * Adds the items of a composite selector to the target. 
	 * Here, each composite selector has 3 items:
	 * <ul>
	 * <li>
	 * The first item is a FragmentSelector which stores the URI fragment of the annotated paragraph.
	 * </li>
	 * 
	 * <li>
	 * The second item is a Text Position Selector that stores the start and end position of the
	 * annotated text relative to the beginning of the paragraph
	 * </li>
	 * 
	 * <li>
	 * The third item is a TextQuoteSelector that stores the passage of text that has been annotated.
	 * </li>
	 * </ul>
	 * 
	 * Example: 
	 * {@code
	 * <rdf:Description rdf:about="urn:uuid:7C97B503-25EE-4E37-875C-B7C850E13194">
	 * <rdf:type rdf:resource="http://www.w3.org/ns/oa#Composite"/>
     * <oa:item rdf:resource="urn:uuid:8C97B503-25EE-4E37-875C-B7C850E13194"/> 
     *
	 * }
	 * </p>
	 * 
	 * <p>
	 * For each of these selectors a triple describing its type is created. Example:
	 * {@code
	 *    <rdf:Description rdf:about="urn:uuid:8C97B503-25EE-4E37-875C-B7C850E13194"> 
	 *    <rdf:type rdf:resource="http://www.w3.org/ns/oa#FragmentSelector"/>
     *
	 * }
	 * 
	 * </p>
	 * 
	 * <p>
	 * Finally the values which the respective selectors hold are expressed via suitable triples.
	 * </p>
	 * 
	 * @param model the RDF2Go model
	 * @param compSelId the id of the composite selector
	 * @param fragment the text paragraph that is being annotated
	 * @param startPos the annotation's start position relative to the beginning of the paragraph
	 * @param endPos the annotation's end position relative to the beginning of the paragraph
	 * @param exact the text passage that has been matched
	 * @return the composite selector id
	 */
	protected void addCompositeItems(Model model, String compSelId, String fragment, int startPos, int endPos, String exactMatch) {
		String fragSelectorId = AnnotationUtils.generateUuidUri();
		String posSelectorId = AnnotationUtils.generateUuidUri();
		String quoteSelectorId = AnnotationUtils.generateUuidUri();
		
		AnnotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				fragSelectorId, model);
		AnnotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				posSelectorId, model);
		AnnotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				quoteSelectorId, model);

		AnnotationUtils.createResourceTriple(fragSelectorId, RDF_TYPE_PROPERTY,
				AnnotationUtils.createUriString(Prefix.OA.getURL(),
						OA_FRAGMENT_SELECTOR), model);
		AnnotationUtils.createResourceTriple(posSelectorId, RDF_TYPE_PROPERTY,
				AnnotationUtils.createUriString(Prefix.OA.getURL(),
						OA_TEXT_POS_SELECTOR), model);
		AnnotationUtils.createResourceTriple(quoteSelectorId, RDF_TYPE_PROPERTY,
				AnnotationUtils.createUriString(Prefix.OA.getURL(),
						OA_TEXT_QUOTE_SELECTOR), model);
		
		AnnotationUtils.createLiteralTriple(fragSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_START_POS),
				fragment, model);
		
		AnnotationUtils.createLiteralTriple(fragSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_START_POS),
				Integer.toString(startPos), XSD.nonNegativeInteger, model);
		
		AnnotationUtils.createLiteralTriple(fragSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_END_POS),
				Integer.toString(endPos), XSD.nonNegativeInteger, model);
		
		AnnotationUtils.createLiteralTriple(fragSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_EXACT_MATCH),
				exactMatch, model);
	}

	public void setArticleUri(String articleUri) {
		this.articleUri = articleUri;
	}
	
}
