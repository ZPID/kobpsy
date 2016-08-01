package org.zpid.se4ojs.annotation.ncbo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.annotation.OaAnnotator;
import org.zpid.se4ojs.annotation.util.AnnotationResultsAvailableEvent;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * <p>
 * Annotates the textual content of a file with the NCBO annotator.
 * </p>
 *
 */
public class NcboAnnotator extends OaAnnotator {

	static final ObjectMapper mapper = new ObjectMapper();
	private static final String NCBO_ANNOTATOR_URL = "http://bioportal.bioontology.org/annotator";

	/**
	 * Constant that marks the beginning of a URI fragment. TODO: Improve the
	 * text-structure annotation, using real fragment URIs to describe sections
	 * and paragraphs. Then replace this constant's value by '#'.
	 */
	private static final String FRAGMENT_MARKER = "/";

	private static Logger log = LogManager.getLogger(NcboAnnotator.class);
	private String ontologies;

	public NcboAnnotator(String ontologies) {
		this.ontologies = ontologies;
	}

	/**
	 * @see org.zpid.se4ojs.annotation.OaAnnotator#annotateText(org.ontoware.rdf2go.model.Model,
	 *      java.lang.String, java.lang.String) This implementation retrieves
	 *      the annotating concepts from the bioportal annotator tool. The
	 *      results from calling this tool are used create the
	 *      RDF-representations of the annotations.
	 */
	@Override
	public void annotateText(Map<String,Model> ontologyModels, String text, String subElementUri)
			throws UnsupportedEncodingException {

		JsonNode results = callAnnotator(text);

		if (results != null) {
			rdfizeAnnotations(ontologyModels, results, subElementUri);
			notifyListeners(getAnnotationListeners(), new AnnotationResultsAvailableEvent(results, subElementUri));
		} else {
			log.error("NCBOAnnotator: Results are null!. : Text: " + text);
		}
	}

	/**
	 * Sets the configuration properties for the Bioportal Annotator tool, calls
	 * the service and returns the results.
	 *
	 * @param text
	 *            the text to annotate
	 * @return the annotations in JSON-format
	 * @throws UnsupportedEncodingException
	 */
	JsonNode callAnnotator(String text) throws UnsupportedEncodingException {
		StringBuilder urlParameters = new StringBuilder();
		urlParameters.append("&include=prefLabel");
		if (Config.getNcboCui()) {
			urlParameters.append(",cui");
		}
		if (Config.getNcboSemanticType()) {
			urlParameters.append(",semanticType");
		}
		urlParameters.append("&text=").append(
				URLEncoder.encode(text, "ISO-8859-1"));
		String ncboStopwords = Config.getNcboStopwords();
		if (!ncboStopwords.isEmpty()) {
			urlParameters.append("&stop_words=").append(ncboStopwords);
		}
		urlParameters.append("&exclude_synonyms="
				+ new Boolean(Config.getNcboIsExcludeSynonyms()).toString().toLowerCase());

		urlParameters.append(createUrlParameterForOntologies());

		// //TODO externalize following options in config.properties
		urlParameters.append("&exclude_numbers=true");
		urlParameters.append("&longest_only=true");
		urlParameters.append("&minimum_match_length=3");
		urlParameters.append("&display_context=false");


		return jsonToNode(post(Config.getNCBOServiceURL(),
				urlParameters.toString()));
	}

	private String createUrlParameterForOntologies() {
		return new StringBuilder("&ontologies=").append(ontologies).toString();
	}

	/**
	 * Creates the RDF representation of the concept annotation of the passed in
	 * text structure element.
	 *
	 * @param model
	 *            the RDF2Go model
	 * @param results
	 *            the results of the concept mapping
	 * @param textStructElementUri
	 *            the ID of the text structure element whose text is being
	 *            annotated
	 */
	private void rdfizeAnnotations(Map<String,Model> ontologyModels, JsonNode results,
			String textStructElementUri) {

		for (JsonNode result : results) {
			String rawClassDetails = "";

			// Get the details for the class that was found in the annotation
			rawClassDetails = get(result.get("annotatedClass").get("links")
					.get("self").asText());
			if (!rawClassDetails.isEmpty()) {
				JsonNode classDetails = jsonToNode(rawClassDetails);
				JsonNode annotationInfo = result.get("annotations");
				rdfizeAnnotation(ontologyModels, textStructElementUri,
						classDetails, annotationInfo);
			} else {
				log.error("Class details for json Node:" + result
						+ " are empty. Check json library version.");
			}
		}
	}

	private void rdfizeAnnotation(Map<String,Model> ontologyModels, String textStructElementUri,
			JsonNode classDetails, JsonNode annotationInfo) {
		String matchType = StringUtils.EMPTY;
		if (annotationInfo != null) {
			if (Config.getNcboIsExcludeSynonyms() == true) {
				matchType = getClassDetail(annotationInfo, "matchType");
				if (matchType.isEmpty()) {
					log.error("match type should never be empty");
				}
			}
			if (matchType.isEmpty() || matchType.equals("PREF")) {
				String ontology = extractOntologyName(classDetails);
				extractClassDetails(ontologyModels, textStructElementUri, classDetails,
						annotationInfo, ontology);
			}
		}
	}

	protected String extractOntologyName(JsonNode classDetails) {
		return classDetails.get("links").get("ontology")
				.asText();
	}

	private void extractClassDetails(Map<String,Model> ontologyModels,
			String textStructElementUri, JsonNode classDetails,
			JsonNode annotationInfo, String ontology) {

		String conceptId = getClassDetail(classDetails, "@id");
		String prefLabel = getClassDetail(classDetails, "prefLabel");
		String conceptBrowserUrl = getClassDetail(classDetails,
				"links", "ui");
		String ontologyName	= ontology.subSequence(ontology.lastIndexOf("/") + 1, ontology.length()).toString();
		log.trace("\tprefLabel: " + prefLabel);
		log.trace("\tontology: " + ontology + "\n");
		Model model = ontologyModels.get(ontologyName);
		createSingleAnnotation(textStructElementUri, annotationInfo, conceptId,
				prefLabel, conceptBrowserUrl, ontology, model);
	}

	private void createSingleAnnotation(String textStructElementUri,
			JsonNode annotationInfo, String conceptId, String prefLabel,
			String conceptBrowserUrl, String ontology, Model model) {

		String annotationUri = createAnnotationUri(model, conceptId);
		log.trace("Annotation URI: " + annotationUri);
		addAnnotationMetaInfo(model, annotationUri, NCBO_ANNOTATOR_URL);
		String bodyUri = createBody(model, annotationUri, conceptId);
		addBodyInfo(model, bodyUri, prefLabel, conceptBrowserUrl,
				ontology);
		createTargets(model, annotationInfo, annotationUri,
				textStructElementUri);
	}



	/**
	 * For each annotation a separate Annotation Target is created.
	 *
	 * @param model
	 *            the RDF2Go model
	 * @param annotationInfo
	 *            the annotations
	 * @param textStructElementUri
	 *            the URI of the paragraph that is being annotated
	 */
	protected void createTargets(Model model, JsonNode annotationInfo,
			String annotationUri, String textStructElementUri) {
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
						textStructElementUri.lastIndexOf(FRAGMENT_MARKER) + 1,
						textStructElementUri.length());
				addCompositeItems(model, compSelId, fragmentUri, startPos,
						endPos, matchedWords);
			}
		}
	}

	/**
	 * Gets the value of one or more properties from the JSON class details. If
	 * more than one property is specified, the property calls to the JSON class
	 * details will be chained.
	 *
	 * If the classDetail is stored as an element of an ArrayNode, the first
	 * element's value is returned.
	 *
	 * Logs an error if the property has not value.
	 *
	 * @param props
	 *            the name of the properties
	 * @return the text representation of the JSON node as property value
	 */
	public String getClassDetail(JsonNode classDetails, String... props) {

		JsonNode node = null;
		for (String prop : props) {
			if (node == null) {
				node = classDetails.findValue(prop);
				if (node instanceof ArrayNode) {
					ArrayNode anode = (ArrayNode) node;
					if (anode.size() == 1) {
						node = anode.get(0);
					} else {
						node = anode;
						log.trace("more than one nodes in array! " + node.asText());
					}
				}
			} else {
				node = node.path(prop);
			}
			if (node.getNodeType().equals(JsonNodeType.MISSING)) {
				log.error(String.format("Error extracting class detail from json node. No %s found. ", prop));
			}
		}

		return node.asText();
	}

	/**
	 * @see org.zpid.se4ojs.annotation.OaAnnotator#createAnnotation(Model,
	 *      String, String)
	 *
	 *      Creates the main annotation triple, using the last part (name part)
	 *      of the concept URI to create the annotation ID.
	 */
	@Override
	public String createAnnotationUri(Model model, String id) {
		String idSuffix = id.substring(id.lastIndexOf("/") + 1);
		String url = super.createAnnotationUri(model, idSuffix);
		return url;
	}

	@Override
	public void annotate(String baseUri, File paper,
			List<BOStructureElement> bOStructureElements, Path outputDir)
			throws IOException {
		String out = paper.toPath().getFileName().toString()
				.replace(".xml", "-ncboAnnotations.rdf");
		out = out.replace(".XML", "-ncboAnnotations.rdf");
		super.annotate(baseUri, paper, bOStructureElements,
				Paths.get(outputDir.toString(), out));
	}

	private static String post(String urlToGet, String urlParameters) {

		HttpURLConnection conn = null;
		String line;
		String result = "";
		try {
			conn = openUrlConnection(new URL(urlToGet));
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "apikey token="
					+ NcboUtils.API_KEY);
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			log.error("Error during processing of paragraph. Details: "
					+ e.fillInStackTrace());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return result;
	}

	public static JsonNode jsonToNode(String json) {
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

	public static String get(String urlToGet) {

		HttpURLConnection conn = null;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			conn = openUrlConnection(new URL(urlToGet));
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "apikey token="
					+ NcboUtils.API_KEY);
			conn.setRequestProperty("Accept", "application/json");
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			log.error("Error occurred during fetching information from NCBO annotator: \n\t"
					+ e.fillInStackTrace());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}

	protected static HttpURLConnection openUrlConnection(URL url)
			throws IOException {
		HttpURLConnection conn;
		String proxySettings = Config.getProxy();
		if (!StringUtils.isEmpty(proxySettings)) {
			String[] proxyComponents = proxySettings.split(":");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					proxyComponents[0], Integer.valueOf(proxyComponents[1])));
			conn = (HttpURLConnection) url.openConnection(proxy);
		} else {
			conn = (HttpURLConnection) url.openConnection();
		}
		return conn;
	}



}
