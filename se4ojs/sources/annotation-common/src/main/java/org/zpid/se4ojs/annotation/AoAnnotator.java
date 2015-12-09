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
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.impl.jena.ModelFactoryImpl;
import org.ontoware.rdf2go.impl.jena.ModelImplJena;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Statement;
import org.xml.sax.InputSource;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
public abstract class AoAnnotator {
	
	private static final String AOT_EXACT_QUALIFIER = "ExactQualifier";
	private static final String RDF_TYPE = "type";
	private static final String RDF_TYPE_PROPERTY = AnnotationUtils.createPropertyString(Prefix.RDF, RDF_TYPE);
	private static final String AO_ANNOTATES_RESOURCE = "annotatesResource";
	protected static final String AO_HAS_TOPIC = "hasTopic";
	protected static final String AO_CONTEXT = "context";
	private static final String BIOTEA_ELEMENT_SELECTOR = "ElementSelector";
	private static final String DCTERMS_REFERENCES = "references";
	private static final String AO_ON_RESOURCE = "onResource";
	private static final String AO_OFFSET = "offset";
	private static final String AO_RANGE = "range";
	private static final String AO_EXACT_MATCH = "exact";
	private static final String AO_BODY = "body";
	private static final String PAV_AUTHORED_BY = "authoredBy";
	private static final String PAV_CREATION_DATE = "createdOn";
	private static final String BIOTEA_OCCURRENCES = "occurrences";
	private static final String LANGUAGE_EN = "en";
	private static final String TEXTUAL_ENTITY_INFIX = "/textual-entity";

	protected static boolean isConceptIdBrowserUrl = Config.isUseBrowserUrlAsConceptId();
	private String articleUri;
	private Logger log  = Logger.getLogger(AoAnnotator.class);
	
	/** Stores a concept by its URI as a key and the number of times it occurs in the same paper as value. */
	private Map<String, Integer> conceptCount = new HashMap<>();

	@SuppressWarnings("deprecation")
	public void annotate(String baseUri, File paper, List<StructureElement> structureElements, Path outFile) throws IOException {
		Model model = RDF2Go.getModelFactory().createModel();
		AnnotationUtils.setNamespaces(model);
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
		articleUri = AnnotationUtils.getArticleUri(document, baseUri);
		model = annotateParagraphs(articleUri, structureElements, model);
		createConceptFrequencyInformation(model);
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
//	    catch (IOException e) {
//			throw new FileNotFoundException("IOException " + e.getMessage());
//		}
	    model.close();
		os.close();
		log.info("Finished annotation for paper: " + paper.getName());
	}
	
	private void createConceptFrequencyInformation(Model model) {
		for(Entry<String, Integer> entry : conceptCount.entrySet()) {
			AnnotationUtils.createLiteralTriple(entry.getKey(),
					AnnotationUtils.createPropertyString(Prefix.BIOTEA, BIOTEA_OCCURRENCES),
					entry.getValue().toString(), XSD.nonNegativeInteger, model);
		}
	}

	private Model annotateParagraphs(String parentUri, List<StructureElement> container, Model model) {
		List<Model> childModels = new ArrayList<>();
		Map<BOConcept, List<BOContext>> annotations = new HashMap<>();
		
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

	public abstract void annotateText(Model model, String text, String subElementUri) throws Exception;

	public String getArticleUri() {
		return articleUri;
	}
	
	public String createExactQualifier(Model model, String id, String name) {
		  String url = createConceptUri(id, name);
			AnnotationUtils.createResourceTriple(
					url,
					RDF_TYPE_PROPERTY,
					AnnotationUtils.createUriString(Prefix.AOT.getURL(), AOT_EXACT_QUALIFIER), model);
			StringBuilder articleUriBuilder = new StringBuilder(articleUri).append(TEXTUAL_ENTITY_INFIX);
			AnnotationUtils.createResourceTriple(url,
					AnnotationUtils.createPropertyString(Prefix.AO, AO_ANNOTATES_RESOURCE), articleUriBuilder.toString(), model);
			return url;
	}

	private String createConceptUri(String id, String name) {
		return AnnotationUtils.createUriString(
				  articleUri, AnnotationUtils.urlEncode(id), AnnotationUtils.urlEncode(name));
	}
	

	/**
	 * 
	 * @param model the rdf2go model
	 * @param conceptUri the unique ID of the concept
	 * @param subElementUri
	 * @param conceptId
	 * @return the URI of the created ao:context
	 */
	protected String createAoContext(Model model, String conceptUri, String subElementUri, String conceptId) {
		
		Integer currentCount = conceptCount.get(conceptUri);
		if (currentCount == null) {
			currentCount = 0;
		}
		String contextUri = AnnotationUtils.createUriString(subElementUri, "aoContext",
				conceptId + "_" + currentCount.toString());
		AnnotationUtils.createResourceTriple(
				conceptUri,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_CONTEXT), 
				contextUri, model);		
		AnnotationUtils.createResourceTriple(
				contextUri,
				RDF_TYPE_PROPERTY, 
				AnnotationUtils.createUriString(Prefix.BIOTEA.getURL(), BIOTEA_ELEMENT_SELECTOR), model);		
		AnnotationUtils.createResourceTriple(
				contextUri,
				AnnotationUtils.createPropertyString(Prefix.DCTERMS, DCTERMS_REFERENCES), 
				subElementUri, model);		
		AnnotationUtils.createResourceTriple(
				contextUri,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_ON_RESOURCE), 
				articleUri, model);	
		return contextUri;
	}
	/**
	 * 
	 * @param model
	 * @param url
	 * @param subElementUri
	 * @param conceptId
	 * @return the URI of the created ao:context
	 */
	protected void createPositionalInfot(Model model, String contextUri) {	
		AnnotationUtils.createResourceTriple(
				contextUri,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_ON_RESOURCE), 
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
				AnnotationUtils.createPropertyString(Prefix.AO, AO_OFFSET),
				Integer.toString(start), XSD.nonNegativeInteger, model);
		AnnotationUtils.createLiteralTriple(aoContext,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_RANGE),
				Integer.toString(end), XSD.nonNegativeInteger, model);
		
	}

	public void addExactMatch(Model model, String aoContext,
			String matchedWords) {
		AnnotationUtils.createLiteralTriple(aoContext,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_EXACT_MATCH),
				matchedWords, model);
	}
	

	protected void addBody(Model model, String url, String preferredName) {
		AnnotationUtils.createLiteralTriple(url,
				AnnotationUtils.createPropertyString(Prefix.AO, AO_BODY),
				preferredName, model);
		
	}

	protected void addMetaInfo(Model model, String url, String authoringToolUrl) {
		AnnotationUtils.createLiteralTriple(url,
				AnnotationUtils.createPropertyString(Prefix.PAV, PAV_AUTHORED_BY),
				authoringToolUrl, model);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		AnnotationUtils.createLiteralTriple(url,
				AnnotationUtils.createPropertyString(Prefix.PAV, PAV_CREATION_DATE),
				dateFormat.format(new Date()), XSD.date, model);
	}
	
	
}
