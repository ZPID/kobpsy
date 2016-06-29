package org.zpid.se4ojs.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.RiotException;
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
import org.zpid.se4ojs.annotation.util.AnnotationListener;
import org.zpid.se4ojs.annotation.util.MappingsAnnotationListener;
import org.zpid.se4ojs.annotation.util.Observable;
import org.zpid.se4ojs.annotation.util.PaperAnnotationFinishedEvent;
import org.zpid.se4ojs.annotation.util.PaperAnnotationStartEvent;
import org.zpid.se4ojs.annotation.util.ParagraphAnnotationStartEvent;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.sparql.Prefix;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.hp.hpl.jena.vocabulary.XSD;

/**
 * <p>
 * An abstract Annotator that handles the common workflow of annotating 
 * the textual content of a JATs document.
 * </p>
 * <p>
 * The textual content is passed into this annotator as a hierarchy of
 * {@link BOStructureElement}s.
 * </p>
 * 
 * @author barth
 */
public abstract class OaAnnotator implements Observable {
	
	public static final String SEM_TYPE_RDF_FILENAME = "semType.rdf";
	private static final String TEXTUAL_ENTITY = "/textual-entity";
	protected static final String OA_ANNOTATION = "Annotation";
	protected static final String RDF_TYPE = "type";
	protected static final String RDF_TYPE_PROPERTY = AnnotationUtils.createPropertyString(Prefix.RDF, RDF_TYPE);
	private static final String RDF_VALUE = "value";
	private static final String RDF_VALUE_PROPERTY = AnnotationUtils.createPropertyString(Prefix.RDF, RDF_VALUE);
	protected static final String OA_HAS_BODY = "hasBody";
	protected static final String OA_HAS_TARGET = "hasTarget";
	protected static final String OA_HAS_SOURCE = "hasSource";
	protected static final String OA_HAS_SELECTOR = "hasSelector";
	protected static final String OA_ANNOTATED_BY = "annotatedBy";
	protected static final String OA_ANNOTATED_AT = "annotatedAt";
	protected static final String OA_EXACT_MATCH = "exact";
	protected static final String OA_SEMANTIC_TAG = "SemanticTag";
	protected static final String OA_SPECIFIC_RESOURCE = "SpecificResource";
	protected static final String FOAF_PAGE = "page";
	protected static final String FOAF_HOMEPAGE = "homepage";
	protected static final String CNT_TEXT = "ContentAsText";
	protected static final String CNT_CHARS = "chars";
	protected static final String OA_START_POS = "start";
	protected static final String OA_END_POS = "end";
	protected static final String OA_COMPOSITE = "Composite";
	protected static final String OA_ITEM = "item";
	protected static final String OA_FRAGMENT_SELECTOR = "FragmentSelector";
	protected static final String OA_TEXT_POS_SELECTOR = "TextPositionSelector";
	protected static final String OA_TEXT_QUOTE_SELECTOR = "TextQuoteSelector";
	protected static final String PROV_SOFTWARE_AGENT = "SoftwareAgent";
	private static final String LANGUAGE_EN = "en";
	private static final String OA_MOTIVATED_BY = "motivatedBy";
	private static final String OA_TAGGING = "tagging";
	private static final String SKOS_IN_SCHEME = "inScheme";
	private static final String SKOS_CONCEPT = "Concept";
	private static final String DC_TITLE = "title";
	private static final String SKOS_CONCEPT_SCHEME = "ConceptScheme";
	
	private String articleUri;
	private Logger log  = Logger.getLogger(OaAnnotator.class);
	private AnnotationUtils annotationUtils = new AnnotationUtils();
	private Model semTypeModel = null;
	private List<AnnotationListener> annotationListeners = new ArrayList<>();
	
	/** Stores a concept by its URI as a key and the number of times it occurs in the same paper as value. */
	private Map<String, Integer> conceptCount = new HashMap<>();
	private static Path semTypeOut = null;
	private boolean jsonAsAnnotationSource = false;

	
	public OaAnnotator() {
		super();
	}

	public void annotate(String baseUri, File paper, List<BOStructureElement> bOStructureElements, Path outFile)
			throws IOException {
		initSemanticTypeAnnotation(outFile);
		notifyListeners(annotationListeners, new PaperAnnotationStartEvent(getJsonResultsListener()));
		Model model = RDF2Go.getModelFactory().createModel();
		model.open();
		annotationUtils.setNamespaces(model);
		Document document = createDocumentFromPaper(paper);
		articleUri = annotationUtils.getArticleUri(document, baseUri);
		model = annotateParagraphs(articleUri, bOStructureElements, model);
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
	    //@TODO create a semanticTypeModel listener 
	    if (semTypeModel != null && !semTypeModel.isEmpty()) {
		    OutputStream osSemType = new FileOutputStream(new File(semTypeOut.toString()));
		    try {
		    	semTypeModel.writeTo(osSemType);
		    	semTypeModel.close();
			} catch (ModelRuntimeException e) {
				log.error("Error writing semantic types (to semTypes.rdf): " + e.getLocalizedMessage());
		    	semTypeModel.close();
		    }
	    }
	    notifyListeners(annotationListeners, new PaperAnnotationFinishedEvent());
		os.close();
		log.info("Finished annotation for paper: " + paper.getName());
	}

	/**
	 * Initializes semantic type annotation if
	 * semantic type annotation should be active (according to the corresponding configuration property setting).
	 */
	private void initSemanticTypeAnnotation(Path outFile) {
		if (Config.isSemanticType()) {
			semTypeOut = Paths.get(outFile.toString().replace("ncboAnnotations.rdf", SEM_TYPE_RDF_FILENAME));
			semTypeModel = getSemanticTypeModel(
						semTypeOut);
			annotationUtils.setNamespaces(semTypeModel);
		}
	}

	/**
	 * Loads the semantic type model from the RDF-file if it exists. The semantic types found during the current 
	 * execution are appended to existing semantic types.
	 *  
	 * @param semTypeModelPath the path to the RDF-file were the semantic types of the annotated concepts are stored.
	 * @return the semantic type model
	 */
	private Model getSemanticTypeModel(Path semTypeModelPath) {
		Model typeModel = RDF2Go.getModelFactory().createModel();
		typeModel.open();
		if (Files.exists(semTypeModelPath, LinkOption.NOFOLLOW_LINKS)) {
			try (BufferedReader br = new BufferedReader(new FileReader(semTypeModelPath.toFile()))){
				typeModel.readFrom(br);
			} catch (IOException | RiotException io) {
				log.error("Unable to open or read from the Semantic Type Model."
						+ io.getLocalizedMessage());
			}
		} 
		return typeModel;
	}

	/**
	 * Builds a document from the input (xml-) file using a SAXBuilder.
	 * 
	 * @param paper the input JATS-XML-File
	 * @return the jdom2 document
	 */
	@SuppressWarnings("deprecation")
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

	private Model annotateParagraphs(String parentUri, List<BOStructureElement> container, Model model) {
		List<Model> childModels = new ArrayList<>();
		
		for (BOStructureElement se : container) {
			String subElementUri = annotationUtils.createSubElementUri(se, articleUri, parentUri);

			if (se instanceof BOSection) {
				BOSection sec = (BOSection) se;
				List<BOStructureElement> childStructures = sec.getChildStructures();

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
						if (jsonAsAnnotationSource) {
							updateJsonResultListener(model, subElementUri);
							notifyListeners(annotationListeners,
									new ParagraphAnnotationStartEvent(subElementUri));
						} else {
							annotateText(model, text, subElementUri);
						}
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
	
	protected abstract void updateJsonResultListener(
			Model model, String subElementUri);

	protected abstract AnnotationListener getJsonResultsListener();

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
	 * @return the URI by which the annotation is referenced
	 */
	public String createAnnotation(Model model, String id) {
		  String url = createConceptUri(id);
			annotationUtils.createResourceTriple(
					url,
					RDF_TYPE_PROPERTY,
					annotationUtils.createUriString(Prefix.OA.getURL(), OA_ANNOTATION), model);
			return url;
	}

	private String createConceptUri(String id) {
		return annotationUtils.createUriString(
				  articleUri, annotationUtils.urlEncode(id));
	}
	
	/**
	 * Creates a triple that relates the target to the article that is annotated by the annotation.
	 * Example:
	 * {@code
	 * <rdf:Description rdf:about="urn:uuid:6C97B503-25EE-4E37-875C-B7C850E13194"> 
	 * 	  <oa:hasSource rdf:resource="http://www.zpid.de/resource/doi/10.5964/ejcop.v2i2.34/textual-entity"/>
     *    ...
	 * }
	 * 
	 * @param model the RDF2Go model
	 * @param targetId the id of the target
	 */
	protected void relateToArticle(Model model, String targetId) {	
		annotationUtils.createResourceTriple(
				targetId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_SOURCE), 
				articleUri + TEXTUAL_ENTITY, model);	
	}
	
	public void addToConceptCount(String conceptUri) {
		Integer count = conceptCount.get(conceptUri);
		if (count == null) {
			count = 0;
		}
		conceptCount.put(conceptUri, ++count);
	}
	
	/**
	 * 
	 * @param model
	 * @param aoContext
	 * @param matchedWords
	 */
	public void addExactMatch(Model model, String aoContext,
			String matchedWords) {
		annotationUtils.createLiteralTriple(aoContext,
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
		String bodyUri = annotationUtils.createUriString(conceptId);
		annotationUtils.createResourceTriple(annotationUri,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_BODY),
				bodyUri, model);
		return bodyUri;
	}

	/**
	 * Creates the "body"-information of the annotation.
	 * Subject is the annotation body. The body's type and concept name are specified.
	 * Example: 
	 * {@code
	 * <rdf:Description rdf:about="http://purl.obolibrary.org/obo/CHEBI_33232">
	 *   <rdf:type rdf:resource="http://www.w3.org/2011/content#ContentAsText"/>
	 *   <cnt:chars>application</cnt:chars>
     *   <rdf:type rdf:resource="http://www.w3.org/ns/oa#SemanticTag"/>
	 *   <foaf:page rdf:resource="http://bioportal.bioontology.org/ontologies/NIFSTD?p=classes&amp;conceptid=http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2Fchebi.owl%23CHEBI_33232"/>
	 *   ...
	 *   //ontological information
	 *   <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept"/>
	 *   <skos:inScheme rdf:resource=""http://purl.obolibrary.org/obo/CHEBI"/>
	 *   <rdf:Description rdf:about="http://purl.obolibrary.org/obo/CHEBI"/>
	 *   <rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#ConceptScheme"/>
	 *   <dc:title>CHEBI</dc:title>
	 *   ...
	 * }
	 * 
	 * @param model the RDF2Go model
	 * @param bodyUri the URI of the body
	 * @param conceptPrefLabel the preferred label of the concept
	 * @param conceptWebPage the URL to the web page of the concept
	 * @param ontology URI the URI of the ontology
	 */
	protected void addBodyInfo(Model model, String bodyUri, String conceptPrefLabel, String conceptWebPage, String ontologyUri) {
		annotationUtils.createResourceTriple(bodyUri, RDF_TYPE_PROPERTY, 
				annotationUtils.createUriString(Prefix.OA.getURL(), OA_SEMANTIC_TAG), model);
		annotationUtils.createResourceTriple(bodyUri, RDF_TYPE_PROPERTY, 
				annotationUtils.createUriString(Prefix.CNT.getURL(), CNT_TEXT), model);
		annotationUtils.createResourceTriple(bodyUri, AnnotationUtils.createPropertyString(Prefix.FOAF, FOAF_PAGE)
				, conceptWebPage, model);
		annotationUtils.createLiteralTriple(bodyUri, 
				AnnotationUtils.createPropertyString(Prefix.CNT, CNT_CHARS), conceptPrefLabel, model);
		//add ontological information
		annotationUtils.createResourceTriple(bodyUri, RDF_TYPE_PROPERTY, 
				annotationUtils.createUriString(Prefix.SKOS.getURL(), SKOS_CONCEPT), model);
		annotationUtils.createResourceTriple(bodyUri, AnnotationUtils.createPropertyString(Prefix.SKOS, SKOS_IN_SCHEME)
				, ontologyUri, model);
		//TODO if optimizing for speed, cache ontology names for program runtime
		annotationUtils.createLiteralTriple(ontologyUri, AnnotationUtils.createPropertyString(Prefix.DC, DC_TITLE)
				, ontologyUri.substring(ontologyUri.lastIndexOf("/") + 1, ontologyUri.length()), model);
		annotationUtils.createResourceTriple(ontologyUri, RDF_TYPE_PROPERTY, 
				annotationUtils.createUriString(Prefix.SKOS.getURL(), SKOS_CONCEPT_SCHEME), model);
	}

	/**
	 * Creates annotation meta-info: The author by whom the annotation has been created 
	 * and the date when it has been created.
	 * Example:
	 * {@code
	 *  <rdf:Description rdf:about="http://www.zpid.de/resource/CHEBI_33232"> 
	 * 	<oa:annotatedBy rdf:resource="http://bioportal.bioontology.org/annotator/">
	 *  <oa:annotatedAt rdf:datatype="http://www.w3.org/2001/XMLSchema#date">2015-06-30</oa:annotatedAt>
	 *  
	 *  <oa:motivatedBy oa:tagging/>
	 *  
	 *  <rdf:Description rdf:about="http://bioportal.bioontology.org/annotator">
	 *  <rdf:Type rdf:resource=""http://www.w3.org/ns/prov#SoftwareAgent">
	 *  <foaf:homepage "http://bioportal.bioontology.org/annotator"/>
	 *  
	 *  }
	 * @param model the RDF2Go model
	 * @param annotationId the URI of the Annotation
	 * @param authorID the identifier of the author
	 */
	protected void addAnnotationMetaInfo(Model model, String annotationId, String authorID) {
		annotationUtils.createResourceTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ANNOTATED_BY),
				authorID, model);
		annotationUtils.createResourceTriple(authorID, RDF_TYPE_PROPERTY,
				annotationUtils.createUriString(Prefix.PROV.getURL(), PROV_SOFTWARE_AGENT), model);
		annotationUtils.createResourceTriple(authorID, 
				AnnotationUtils.createPropertyString(Prefix.FOAF, FOAF_HOMEPAGE), 
				authorID, model);
		createDateTriple(model, annotationId);
		createMotivationTriple(model, annotationId);
	}

	private void createMotivationTriple(Model model, String annotationId) {
		annotationUtils.createResourceTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_MOTIVATED_BY),
				annotationUtils.createUriString(Prefix.OA.getURL(), OA_TAGGING), model);
		
	}

	protected void createDateTriple(Model model, String annotationId) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		annotationUtils.createLiteralTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ANNOTATED_AT),
				dateFormat.format(new Date()), XSD.date, model);
	}
	
	protected void createTarget(Model model, String annotationUri, String targetId) {
		annotationUtils.createResourceTriple(annotationUri, 
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_TARGET), targetId, model);
	}
	
	protected void addTargetType(Model model, String targetId) {
		annotationUtils.createResourceTriple(targetId, RDF_TYPE_PROPERTY, 
				annotationUtils.createUriString(Prefix.OA.getURL(), OA_SPECIFIC_RESOURCE), model);
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
		String compSelectorId = annotationUtils.generateUuidUri();
		annotationUtils.createResourceTriple(targetId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_HAS_SELECTOR),
				compSelectorId, model);
		annotationUtils.createResourceTriple(compSelectorId, RDF_TYPE_PROPERTY,
				annotationUtils.createUriString(Prefix.OA.getURL(), OA_COMPOSITE), model);
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
	 *      <rdf:Description rdf:about="urn:uuid:8bb63d59-2b21-11b2-8081-e47fb211d05d">
     *		<oa:item rdf:resource="http://www.zpid.de/textQuoteSel/SUBSTANCE"/>
     *
	 * }
	 * 
	 * </p>
	 * 
	 * <p>
	 * Finally the values held by the respective selectors are expressed via suitable triples.
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
	protected void addCompositeItems(Model model, String compSelId, String fragment,
			int startPos, int endPos, String exactMatch) {
		
		String fragmentUri = annotationUtils.urlEncode(fragment);
		String fragSelectorId = annotationUtils.createUriString(
				articleUri, AnnotationUtils.URI_INFIX_FRAGEMENT_SEL,
				fragmentUri);
		String posUri = new StringBuilder(Integer.toString(startPos)).append("_").append(endPos).toString();
		String posSelectorId = annotationUtils.createUriString(
				fragSelectorId.replace(AnnotationUtils.URI_INFIX_FRAGEMENT_SEL, AnnotationUtils.URI_INFIX_TEXTPOS_SEL),
				annotationUtils.urlEncode(posUri));
		String quoteSelectorId = annotationUtils.createUriString(
				AnnotationUtils.URI_PREFIX_ZPID, AnnotationUtils.URI_INFIX_TEXT_QUOTE_SEL,
				annotationUtils.urlEncode(exactMatch));
		
		annotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				fragSelectorId, model);
		annotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				posSelectorId, model);
		annotationUtils.createResourceTriple(compSelId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ITEM),
				quoteSelectorId, model);

		annotationUtils.createResourceTriple(fragSelectorId, RDF_TYPE_PROPERTY,
				annotationUtils.createUriString(Prefix.OA.getURL(),
						OA_FRAGMENT_SELECTOR), model);
		annotationUtils.createResourceTriple(posSelectorId, RDF_TYPE_PROPERTY,
				annotationUtils.createUriString(Prefix.OA.getURL(),
						OA_TEXT_POS_SELECTOR), model);
		annotationUtils.createResourceTriple(quoteSelectorId, RDF_TYPE_PROPERTY,
				annotationUtils.createUriString(Prefix.OA.getURL(),
						OA_TEXT_QUOTE_SELECTOR), model);
		
		annotationUtils.createLiteralTriple(fragSelectorId,
				RDF_VALUE_PROPERTY,
				fragment, model);
		
		annotationUtils.createLiteralTriple(posSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_START_POS),
				Integer.toString(startPos), XSD.nonNegativeInteger, model);
		
		annotationUtils.createLiteralTriple(posSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_END_POS),
				Integer.toString(endPos), XSD.nonNegativeInteger, model);
		
		annotationUtils.createLiteralTriple(quoteSelectorId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_EXACT_MATCH),
				exactMatch, model);
	}

	public void setArticleUri(String articleUri) {
		this.articleUri = articleUri;
	}

	protected AnnotationUtils getAnnotationUtils() {
		return annotationUtils;
	}

	protected void setAnnotationUtils(AnnotationUtils annotationUtils) {
		this.annotationUtils = annotationUtils;
	}

	protected Model getSemTypeModel() {
		return semTypeModel;
	}
	
	@Override
	public void addListener(AnnotationListener listener) {
		annotationListeners.add(listener);
	}
	
	public void addMappingsListener(AnnotationListener listener) {
		annotationListeners.add(new MappingsAnnotationListener(listener));
	}

	protected List<AnnotationListener> getAnnotationListeners() {
		return annotationListeners;
	}

	protected boolean isJsonAsAnnotationSource() {
		return jsonAsAnnotationSource;
	}

	protected void setJsonAsAnnotationSource(boolean jsonAsAnnotationSource) {
		this.jsonAsAnnotationSource = jsonAsAnnotationSource;
	}

}
