package org.zpid.se4ojs.annotation.ncbo;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.annotation.OaAnnotator;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/** 
 * <p>
 * Annotates the textual content of a file with the NCBO annotator.
 * </p>
 * 
 */
public class NcboAnnotator extends OaAnnotator{
	
	static final String REST_URL = "http://data.bioontology.org"; 
	static final ObjectMapper mapper = new ObjectMapper();
	private static final String NCBO_ANNOTATOR_URL = "http://bioportal.bioontology.org/annotator";
	
	/** 
	 * Constant that marks the beginning of a URI fragment.
	 * TODO: Improve the text-structure annotation, using real fragment URIs to 
	 * describe sections and paragraphs. Then replace this constant's value by '#'. 
	 */
	private static final String FRAGMENT_MARKER = "/";
	
	private static Logger log = Logger.getLogger(NcboAnnotator.class);
	private String ontologies;
   

	public NcboAnnotator(String ontologies) {
		this.ontologies = ontologies;
	}

//	TODO
//	public void annotateWithApaClusters(File paper,
//			List<BOStructureElement> topLevelElements, String outputDir) throws IOException {
//		String out = paper.toPath().getFileName().toString().replace(".xml", "-ncboAnnotations.rdf");
//		out = out.replace(".XML", "-ncboAnnotations.rdf");
//		annotate(paper, topLevelElements, Paths.get(outputDir, out));
//	}


	/**
	 * @see org.zpid.se4ojs.annotation.OaAnnotator#annotateText(org.ontoware.rdf2go.model.Model, java.lang.String, java.lang.String)
	 *  This implementation retrieves the annotating concepts from the bioportal annotator tool.
	 *  The results from calling this tool are used create the RDF-representations of the annotations.
	 */
	@Override
	public void annotateText(Model model, String text,
			String subElementUri) throws UnsupportedEncodingException  {
		StringBuilder urlParameters = new StringBuilder();
		JsonNode results;
		//FIXME externalize url parameters as configurable properties
		urlParameters.append("include=prefLabel,synonym,definition");
		urlParameters.append("&text=").append(URLEncoder.encode(text, "ISO-8859-1"));
		urlParameters.append("require_exact_match=true");
		urlParameters.append(createUrlParameterForOntologies());
		results = jsonToNode(post(REST_URL + "/annotator", urlParameters.toString()));
		if (results != null) {
			rdfizeAnnotations(model, results, subElementUri);	
		} else {
			log.error("NCBOAnnotator: Results are null!. : Text: " + text);
		}
	}

    private String createUrlParameterForOntologies() {
    	return new StringBuilder("&ontologies=").append(ontologies).toString();
    }

    /**
     * Creates the RDF representation of the concept annotation of the passed in 
     * text structure element.
     * 
     * @param model the RDF2Go model
     * @param results the results of the concept mapping
     * @param textStructElementUri the ID of the text structure element whose text is being annotated
     */
	private void rdfizeAnnotations(Model model, JsonNode results, String textStructElementUri) {

        for (JsonNode result : results) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(result.get("annotatedClass").get("links").get("self").asText()));
			JsonNode annotationInfo = result.get("annotations");
            if (classDetails != null) {
                String conceptId = getClassDetail(classDetails, "@id");
                String prefLabel = getClassDetail(classDetails, "prefLabel");
                String conceptBrowserUrl = getClassDetail(classDetails, "links" , "ui");
    			log.debug("\tprefLabel: " + prefLabel);
                String ontology = classDetails.get("links").get("ontology").asText();
				log.debug("\tontology: " + ontology + "\n");

    			String annotationUri = createAnnotation(model,
    					conceptId,
    					prefLabel);
    			log.debug("Annotation URI: " + annotationUri);
    			addAnnotationMetaInfo(model, annotationUri, NCBO_ANNOTATOR_URL);    			
    			String bodyUri = createBody(model, annotationUri, conceptId);
    			addBodyInfo(model, bodyUri, prefLabel, conceptBrowserUrl, ontology);
    			createTargets(model, annotationInfo, annotationUri, textStructElementUri);
            }
        }
	}

	/**
	 * For each annotation a separate Annotation Target is created.
	 * 
	 * @param model the RDF2Go model
	 * @param annotationInfo the annotations
	 * @param textStructElementUri the URI of the paragraph that is being annotated
	 */
	private void createTargets(Model model, JsonNode annotationInfo, String annotationUri,
			String textStructElementUri) {
		int startPos = -1;
		int endPos = -1;
		String matchedWords = null;
		
        if (annotationInfo.isArray() && annotationInfo.elements().hasNext()) {
            for (JsonNode inf : annotationInfo) {
            	String targetId = getAnnotationUtils().generateUuidUri();
    			createTarget(model, annotationUri, targetId);
    			addTargetType(model, targetId);
    			relateToArticle(model, targetId);
    			String compSelId = addCompositeSelector(model, targetId);
            	startPos = inf.get("from").asInt();
            	endPos = inf.get("to").asInt();
            	matchedWords = inf.get("text").asText();
            	String fragmentUri = textStructElementUri.substring(
            			textStructElementUri.lastIndexOf(FRAGMENT_MARKER) + 1, textStructElementUri.length());
            	addCompositeItems(model, compSelId, fragmentUri, startPos, endPos, matchedWords);
            }
        }           		
	}
	

	/**
	 * Gets the value of one or more properties from the JSON class details.
	 * If more than one property is specified, the property calls to the JSON
	 * class details will be chained.
	 * 
	 * Logs an error if the property has not value.
	 * 
	 * @param props the name of the properties
	 * @return the text representation of the JSON node as property value
	 */
	private String getClassDetail(JsonNode classDetails, String ... props) {
		
		JsonNode node = null;
		for (String prop : props) {
			if (node == null) {
				node = classDetails.path(prop);
			} else {
				node = node.path(prop);
			}
	        if (node.equals(JsonNodeType.MISSING)) {
				log.error(String.format("no %s found. ", prop));
	        }
		}
  
	    return node.asText();
	}

	/**
	 * @see org.zpid.se4ojs.annotation.OaAnnotator#createAnnotation(Model, String, String)
	 * 
	 * Creates the main annotation triple, using the last part (name part) of the concept URI
	 * to create the annotation ID.
	 */
	@Override
	public String createAnnotation(Model model, String id, String name) {
        	String idSuffix = id.substring(id.lastIndexOf("/") + 1);
			String url = super.createAnnotation(model, idSuffix, name);
			return url;
	}

	@Override
	public void annotate(String baseUri, File paper, List<BOStructureElement> bOStructureElements,
			Path outputDir) throws IOException {
		String out = paper.toPath().getFileName().toString().replace(".xml", "-ncboAnnotations.rdf");
		out = out.replace(".XML", "-ncboAnnotations.rdf");
		super.annotate(baseUri, paper, bOStructureElements, Paths.get(outputDir.toString(), out));
		
	}

	private static String post(String urlToGet, String urlParameters) {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        try {
//        	System.out.println("url params for ncbo annotator: " + urlToGet + "params " + urlParameters);
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "apikey token=" + NcboUtils.API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            conn.disconnect();

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    
    private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }
    
    private static String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + NcboUtils.API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
        	log.error("Error occurred during fetching information from NCBO annotator: \n\t" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return result;
    }

}
