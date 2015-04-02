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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.AoAnnotator;
import org.zpid.se4ojs.annotation.BOAnnotation;
import org.zpid.se4ojs.annotation.BOContext;
import org.zpid.se4ojs.annotation.Prefix;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 
 * <p>
 * Annotates the textual content of a file with the NCBO annotator.
 * </p>
 * 
 */
public class NcboAnnotator extends AoAnnotator{
	static final String REST_URL = "http://data.bioontology.org"; 
	static final ObjectMapper mapper = new ObjectMapper();
	private static final String NCBO_ANNOTATOR_URL = "http://bioportal.bioontology.org/annotator/";
	private static Logger log = Logger.getLogger(NcboAnnotator.class);
	private String ontologies;
   

	public NcboAnnotator(String ontologies) {
		this.ontologies = ontologies;
	}

//	TODO
//	public void annotateWithApaClusters(File paper,
//			List<StructureElement> topLevelElements, String outputDir) throws IOException {
//		String out = paper.toPath().getFileName().toString().replace(".xml", "-ncboAnnotations.rdf");
//		out = out.replace(".XML", "-ncboAnnotations.rdf");
//		annotate(paper, topLevelElements, Paths.get(outputDir, out));
//	}

	@Override
	public List<BOAnnotation> annotateText(Model model, String text,
			String subElementUri) throws UnsupportedEncodingException  {
		List<BOAnnotation> annotations = new ArrayList<>();
		StringBuilder urlParameters = new StringBuilder();
		JsonNode results;
		urlParameters.append("include=prefLabel,synonym,definition");
		urlParameters.append("&text=").append(URLEncoder.encode(text, "ISO-8859-1"));
		urlParameters.append("require_exact_match=true");
		urlParameters.append(createUrlParameterForOntologies());
		results = jsonToNode(post(REST_URL + "/annotator", urlParameters.toString()));
		if (results != null) {
			return rdfizeAnnotations(model, results, subElementUri, annotations);	
		} else {
			log.error("NCBOAnnotator: Results are null!. : Text: " + text);
		}
		return null;
		
	}

	//TODO implement and make configurable
    private String createUrlParameterAPAClusterOntologies() {
    	String apaOntos = "&ontologies=APAONTO,LEGALAPATEST2,APACOMPUTER,APADISORDERS,APAEDUCLUSTER,APANEUROCLUSTER,APAOCUEMPLOY,APASTATISTICAL,APATANDT,APATREATMENT";
		return apaOntos;
	}
    private String createUrlParameterForOntologies() {
    	return new StringBuilder("&ontologies=").append(ontologies).toString();
    }

	private List<BOAnnotation> rdfizeAnnotations(Model model, JsonNode results, String subElementUri, List<BOAnnotation> annotations) {

        for (JsonNode result : results) {
        	BOAnnotation annotation = new BOAnnotation();
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(result.get("annotatedClass").get("links").get("self").asText()));
			JsonNode annotationInfo = result.get("annotations");
            if (classDetails != null && annotation != null) {
                String conceptId = null;
    			try {
    				conceptId = classDetails.get("@id").asText();
    			} catch (Exception e) {
    				log.error("no concept id found");
    				e.printStackTrace();
    			}
    			log.debug("\tid of concept mapped: " + conceptId);
                String clusterPrefLabel = classDetails.get("prefLabel").asText();
    			log.debug("\tprefLabel: " + clusterPrefLabel);
                log.debug("\tontology: " + classDetails.get("links").get("ontology").asText() + "\n");

    			String url = createExactQualifier(model,
    					conceptId,
    					clusterPrefLabel);
    			log.debug("concept: " + url);
    			annotation.setConceptUri(url);
    			int startPos = -1;
    			int endPos = -1;
    			String matchedWords = null;

            	createExactQualifier(model, conceptId, clusterPrefLabel);
    			addBody(model, url, clusterPrefLabel);
    			addMetaInfo(model, url, NCBO_ANNOTATOR_URL);
                if (annotationInfo.isArray() && annotationInfo.elements().hasNext()) {
                    for (JsonNode inf : annotationInfo) {
                    	addToConceptCount(url);
                    	startPos = inf.get("from").asInt();
                    	endPos = inf.get("to").asInt();
                    	matchedWords = inf.get("text").asText();
            			BOContext context = addContext(model, url,
            					subElementUri, conceptId, clusterPrefLabel, startPos, endPos, matchedWords);
            			annotation.getContexts().add(context);
                    }
                }            	
            }
        }
		return annotations;
		
	}

	/**
	 * Creates the RDF ao:topic
	 */
	@Override
	public String createExactQualifier(Model model, String id, String name) {
        	String idInfix = id.substring(id.lastIndexOf("/") + 1);
			String url = super.createExactQualifier(model, idInfix, name);
			AnnotationUtils.createResourceTriple(url, AnnotationUtils
					.createPropertyString(Prefix.AO, AO_HAS_TOPIC), AnnotationUtils
					.createUriString(id), model);
			return url;
	}

	@Override
	public void annotate(String baseUri, File paper, List<StructureElement> structureElements,
			Path outputDir) throws IOException {
		String out = paper.toPath().getFileName().toString().replace(".xml", "-ncboAnnotations.rdf");
		out = out.replace(".XML", "-ncboAnnotations.rdf");
		super.annotate(baseUri, paper, structureElements, Paths.get(outputDir.toString(), out));
		
	}

	private BOContext addContext(Model model, String url,
			String subElementUri, String conceptId, String clusterPrefLabel, int startPos, int endPos,
			String matchedWords) {

		BOContext context = new BOContext();
		context.setSubElementUri(subElementUri);
		String idInfix = conceptId.substring(conceptId.lastIndexOf("/") + 1);
		String aoContext = createAoContext(model, url, subElementUri, idInfix);
		context.setContextUri(aoContext);
		int range = endPos - startPos;
		createPositionalTriples(model, aoContext, startPos, range);
		Pair<Integer, Integer> posPair = new ImmutablePair<Integer, Integer>(
				startPos, range);
		context.getOffsetsAndRanges().add(posPair);
		addExactMatch(model, aoContext, matchedWords, context);
		return context;
	}

	private static String post(String urlToGet, String urlParameters) {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        try {
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
    
	private void addExactMatch(Model model, String aoContext,
			String matchedWord, BOContext context) {
			addExactMatch(model, aoContext, matchedWord);
			context.getMatchedWords().add(matchedWord);
	}
}
