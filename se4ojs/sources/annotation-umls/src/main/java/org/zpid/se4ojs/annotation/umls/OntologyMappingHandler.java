/**
 * 
 */
package org.zpid.se4ojs.annotation.umls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.exception.Se4ojsConfigurationException;

/**
 * <p>
 * This class allows for mapping between atomic concepts from the UMLS and there
 * respective URI in bioontology.org. The mappings are defined in a separate
 * resource which may be extended by the user.
 * </p>
 * <p>
 * First, the mapping is loaded.
 * 
 * @author barth
 *
 */
public class OntologyMappingHandler {

	private static final String URI_BIOONTOLOGY_INFIX_CONCEPT_SEPARATOR_PREF_LABEL = "P";

	private static final String URI_BIOONTOLOGY_INFIX_CONCEPT_SEPARATOR_CODE = "C";

	private static final String URL_BIOONTOLOGY_INFIX = "?p=classes&conceptid=";

	private static OntologyMappingHandler ontologyMappingHandler;

	private Logger log = Logger.getLogger(OntologyMappingHandler.class);

	private Map<String, OntologyMappingNcboUmls> umlsToOntologyMapping;

	private String ncboBaseOntologyUri;

	public static OntologyMappingHandler getInstance() {
		if (ontologyMappingHandler == null) {
			ontologyMappingHandler = new OntologyMappingHandler();
		}
		return ontologyMappingHandler;
	}

	private OntologyMappingHandler() {
		super();
		initMapping();
		ncboBaseOntologyUri = getNcboBaseOntologyUri();
	}

	private String getNcboBaseOntologyUri() {
		return Config.getNCBOBaseConceptUri();
	}

	private void initMapping() {
		umlsToOntologyMapping = parseMappingFile(getMappingResource());
	}

	private Map<String, OntologyMappingNcboUmls> parseMappingFile(
			InputStream mappingFile) {
		Map<String, OntologyMappingNcboUmls> umlsToNcbo = new HashMap<>();
		int lineNo = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				mappingFile))) {
			String line = null;
			while ((line = br.readLine()) != null) { // while loop begins here
				lineNo++;
				if (! line.isEmpty() && !line.startsWith("#")) {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					String ontoName = tokenizer.nextToken().trim();
					String ncboAbbr = tokenizer.nextToken().trim();
					String umlsAbbr = tokenizer.nextToken().trim();
					String bioOntoUrl = tokenizer.nextToken().trim();
					String separator = tokenizer.nextToken().trim();
					String code = tokenizer.nextToken().trim();
					String multiWordDelim = "";
					String stopwords = "";
					if (tokenizer.hasMoreTokens()) {
						multiWordDelim = tokenizer.nextToken().trim();
					}
					if (tokenizer.hasMoreTokens()) {
						stopwords = tokenizer.nextToken().trim();
					}
					OntologyMappingNcboUmls mapping = new OntologyMappingNcboUmls(
							ontoName, ncboAbbr, umlsAbbr, bioOntoUrl, separator, code, multiWordDelim, stopwords);
					umlsToNcbo.put(umlsAbbr, mapping);
				}
			}
		} catch (NoSuchElementException | IOException e) {
			if (e instanceof NoSuchElementException) {
				throw new Se4ojsConfigurationException(
						"Error in ontology mapping file line: " + lineNo
								+ ".Cause: " + e.getLocalizedMessage());
			}
			log.error("Unable to read ontology mapping: "
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
		log.info("Ontologies whose concepts will be mapped to bioportal: " + umlsToNcbo.keySet().toString());
		return umlsToNcbo;
	}

	private InputStream getMappingResource() {
		return Config.getOntologyMappingFile();
	}

	public String mapUmlsSourceConceptToNcbo(BOSourceConcept sourceConcept) {
		StringBuilder sb = new StringBuilder(ncboBaseOntologyUri);
		OntologyMappingNcboUmls ontologyMappingNcboUmls = umlsToOntologyMapping
				.get(sourceConcept.getSourceVocabulary());
		if (ontologyMappingNcboUmls != null) {
			sb.append(ontologyMappingNcboUmls.getNcboAbbr());
			sb.append(URL_BIOONTOLOGY_INFIX);
			sb.append(ontologyMappingNcboUmls.getNcboOntologyUri());
			if (!StringUtils.isEmpty(ontologyMappingNcboUmls.getConceptSeparator())) {
				sb.append(ontologyMappingNcboUmls.getConceptSeparator());	
			}
			if (ontologyMappingNcboUmls.getConceptRepresentation().equals(
					URI_BIOONTOLOGY_INFIX_CONCEPT_SEPARATOR_CODE)) {
				sb.append(sourceConcept.getConceptId());
			} else if (ontologyMappingNcboUmls.getConceptRepresentation()
					.equals(URI_BIOONTOLOGY_INFIX_CONCEPT_SEPARATOR_PREF_LABEL)) {
				sb.append(sourceConcept.getPreferredName());
			}
			return sb.toString();
		}

		return null;
	}

	/**
	 * @return the umlsToOntologyMapping
	 */
	Map<String, OntologyMappingNcboUmls> getUmlsToOntologyMapping() {
		return umlsToOntologyMapping;
	}

	public OntologyMappingNcboUmls getSource(String source) {
		return getUmlsToOntologyMapping().get(source);
	}

	public Set<String> getOntologies() {
		return getUmlsToOntologyMapping().keySet();
	}

}
