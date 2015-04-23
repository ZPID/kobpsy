package org.zpid.se4ojs.annotation.umls;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.AoAnnotator;
import org.zpid.se4ojs.annotation.BOConcept;
import org.zpid.se4ojs.annotation.BOContext;
import org.zpid.se4ojs.annotation.Prefix;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

 /** 
 * Annotates the text content of a given file with vocabulary from the UMLS.
 * <p>
 * The MetaMap program which needs to run on the system this program is running on,
 * is queried via its Java API for each text of each paragraph. The concept mapping is restricted
 * to the passed in ontologies (to specify an ontology, the respective UMLS abbreviation must be used).
 * </p>
 * @author barth
 *
 */
public class UmlsAnnotator extends AoAnnotator {
	
	private static final String UMLS_APA_ABBR = "PSY";
	private static final String UMLS_ONTOLOGY_BASE_URI = Config.getUmlsBaseConceptUri();
	private static final String UMLS_CUI_URI_INFIX = ";0;1;CUI;";
	private static final String ZPID_APA_URI = "http://www.zpid.de/psyndex#";
	private static final String META_MAP_URL = "http://metamap.nlm.nih.gov";
	private static final String UMLS_CUI_URI_SUFFIX = ";EXACT_MATCH;*;";
	private static MetaMapApi api = new MetaMapApiImpl();
	
	private Logger log = Logger.getLogger(UmlsAnnotator.class);
	private UtsConceptIDMappingClient utsConceptMapper;
	private OntologyMappingHandler mappingHandler;

	public UmlsAnnotator(String ontologies) {
		init(ontologies);
	}

	/** 
	 * Initializes the MetaMap program via the MetaMap API.
	 * Sets the ontologies that will be used for annotations.
	 */
	private void init(String ontologies) {
		String opts = new StringBuilder(Config.getUmlsMetamapOptions())
				.append(" -R ")
				.append(ontologies).toString().trim();
		api.setOptions(opts);
//		api.setOptions(new StringBuilder("-R ").append(ontologies).toString());
		System.out.println(opts);
		mappingHandler = OntologyMappingHandler.getInstance();
		try {
			utsConceptMapper = UtsConceptIDMappingClient.getInstance(mappingHandler);
		} catch (UtsFault_Exception e) {
			log.error("The UMLS concept ID matcher could not be initialized. Unable to "
					+ "map Metathesaurus IDs to IDs of the source vocabularies. ");
		}
	}

	public void annotate(String baseURi,File paper,
			List<StructureElement> topLevelElements, String outputDir) throws IOException {
		String out = paper.toPath().getFileName().toString().replace(".xml", "-umlsAnnotations_psy.rdf");
		
		out = out.replace(".XML", "-umlsAnnotations.rdf");
		super.annotate(baseURi, paper, topLevelElements, Paths.get(outputDir, out));
	}

	public void annotateText(Model model, String paragraph, String subElementUri)
			throws Exception {

		List<Result> resultList = Collections.emptyList();
		try {
			resultList = api.processCitationsFromString(paragraph);
		} catch (Exception e) {
			log.error(String
					.format("UmlsAnnotation error: MetaMapError annotating paragraph %s.\n Exception: %s \n Text: %s\n\n",
							subElementUri, e.getLocalizedMessage(), paragraph));
			e.printStackTrace();
		}
		for (Result res : resultList) {
			List<Utterance> utteranceList = res.getUtteranceList();
			for (Utterance utterance : utteranceList) {
				List<PCM> pcmList = utterance.getPCMList();
				for (PCM pcm : pcmList) {
					for (Mapping map : pcm.getMappingList()) {
						for (Ev mapEv : map.getEvList()) {
							BOConcept concept = new BOConcept();
							String preferredName = mapEv.getPreferredName();
							String metathesaurusUri = createExactQualifier(
									model, mapEv.getConceptId(), preferredName);
							concept.setConceptUri(metathesaurusUri);
							addToConceptCount(metathesaurusUri);
							addContext(model, metathesaurusUri, subElementUri,
									mapEv.getConceptId(), mapEv);
							addBody(model, metathesaurusUri, preferredName);
							addMetaInfo(model, metathesaurusUri, META_MAP_URL);
							addUmlsAtomToNcboBrowserUri(model,
									metathesaurusUri, mapEv.getConceptId(),
									preferredName);
						}
					}
				}
			}
		}
	}

	/**
	 * Gets the terms from the source vocabularies that are mapped to the passed in
	 * metathesaurus concept ID.
	 * Creates a concept URI, i.e. a new "ao:topic" for each of these "atom-concepts".
	 *  
	 * TODO adapt concept count? Do we count each metathesaurus concept or each atom?
	 * Do we really list each atom?
	 * @param preferredName 
	 * 
	 */
	private void addUmlsAtomToNcboBrowserUri(Model model, String metaConceptUri, String metaConceptId, String metaPrefTerm)
			throws Exception {
		
		Set<BOSourceConcept> sourceConcepts = utsConceptMapper.mapCuiToAtomIDs(
				metaConceptId, metaPrefTerm);
		for (BOSourceConcept sc : sourceConcepts) {
			String urlCandidate = mappingHandler.mapUmlsSourceConceptToNcbo(sc);
			if (urlCandidate != null) {
				createTopic(model, metaConceptUri, urlCandidate);				
			}
		}
	}

	private BOContext addContext(Model model, String url, String subElementUri,
			String conceptId, Ev mapEv) {
		BOContext context = new BOContext();
		List<String> matchedWords = null;
		List<Position> positionalInfo = null;
		try {
			positionalInfo = mapEv.getPositionalInfo();
			matchedWords = mapEv.getMatchedWords();

		} catch (Exception e) {
			log .error("UMLS Annotation error: Unable to compare size of mapEv lists with each other."
					+ " Occurred in: (text section, first matched word)  "
					+ subElementUri + matchedWords.get(0));
			e.printStackTrace();
		}
		assert(positionalInfo.size() == matchedWords.size());
			context.setSubElementUri(subElementUri);
			String aoContext = createAoContext(model, url, subElementUri, conceptId);
			context.setContextUri(aoContext);
			addPositionalInfo(model, aoContext, positionalInfo, context);
			addExactMatches(model, aoContext, matchedWords, context);		
		return context;
		
	}

	private void addExactMatches(Model model, String aoContext,
			List<String> matchedWords, BOContext context) {
		for(String match : matchedWords) {
			addExactMatch(model, aoContext, match);
			context.getMatchedWords().add(match);
		}
		
	}

	private void addPositionalInfo(Model model, String aoContext,
			List<Position> positionalInfo, BOContext context) {
		for(Position position : positionalInfo) {
			createPositionalTriples(model, aoContext, position.getX(), position.getY());
			Pair<Integer, Integer> posPair = new ImmutablePair<Integer, Integer>(position.getX(), position.getY());
			context.getOffsetsAndRanges().add(posPair);			
		}
		
	}

	@Override
	public String createExactQualifier(Model model, String id, String name) {
		String url = super.createExactQualifier(model, id, name);
		StringBuilder sb = new StringBuilder(UMLS_ONTOLOGY_BASE_URI)
		   .append("#").append(id)
		   .append(UMLS_CUI_URI_INFIX)
		   .append(UtsConceptIDMappingClient.UMLS_VERSION)
		   .append(UMLS_CUI_URI_SUFFIX);
		createTopic(model, url, sb.toString());
		return url;
	}

	/**
	 * Creates an "ao:topic" triple:
	 * Example: <rdf:Description rdf:about="http://www.zpid.de/resource/doi/10.5964/ejcop.v2i1.2/OntoAD-C0004927/Behavior">
				<ao:hasTopic rdf:resource="http://doe-generated-ontology.com/OntoAD#C0004927"/>
	 * @param model the model to add the triple to
	 * @param exactQualifier the subject
	 * @param conceptLink the object; i.e. link to a concept
	 */
	void createTopic(Model model, String exactQualifier, String... conceptLinks) {
		log.info("mapped Uri: " + AnnotationUtils.createUriString(conceptLinks)); //FIXME delete
		AnnotationUtils.createResourceTriple(exactQualifier, AnnotationUtils
				.createPropertyString(Prefix.AO, AO_HAS_TOPIC), AnnotationUtils
				.createUriString(conceptLinks), model);
	}

//	public static void main(String[] args) throws Exception {
//	MetaMapApi api = new MetaMapApiImpl();
//	api.setOptions("-C");
//	api.setOptions("-R PSY");
//	// api.setOptions("-R NCI");
//	String text = "In any social analysis, one can attribute observed behavioural outcomes to actions and inactions of people (agents) or to the presence or absence of certain structures or systems. The dualism of agent and structure is resolved through the concept of duality as proposed by Anthony Giddens in his structuration theory (ST). Though ST has been applied in other disciplines, it is either less known or applied in psychology. This paper sought to examine ST as a framework for understanding the interdependent relationship between structure and agents in the light of offering explanatory framework in social science research or policy formulation. It concluded with an integrated model comprising elements of both Bandura�s social-cognitive theory and Giddens� ST.";
//	List<Result> resultList = api.processCitationsFromString(text);
//	for (Result res : resultList) {
//		System.out.println(res.getMachineOutput());
//		List<Utterance> utteranceList = res.getUtteranceList();
//		for (Utterance utterance : utteranceList) {
//			List<PCM> pcmList = utterance.getPCMList();
//			for (PCM pcm : pcmList) {
//				System.out.println("Phrase:");
//				System.out.println(" text: "
//						+ pcm.getPhrase().getPhraseText());
//				List<Mapping> mappingList = pcm.getMappingList();
//				for (Mapping mapping : mappingList) {
//					System.out.println("Mappings:");
//					for (Mapping map : pcm.getMappingList()) {
//						System.out.println(" Map Score: " + map.getScore());
//						for (Ev mapEv : map.getEvList()) {
//							System.out.println("   Score: "
//									+ mapEv.getScore());
//							System.out.println("   Concept Id: "
//									+ mapEv.getConceptId());
//							System.out.println("   Concept Name: "
//									+ mapEv.getConceptName());
//							System.out.println("   Preferred Name: "
//									+ mapEv.getPreferredName());
//							System.out.println("   Matched Words: "
//									+ mapEv.getMatchedWords());
//							System.out.println("   Semantic Types: "
//									+ mapEv.getSemanticTypes());
//							System.out.println("   MatchMap: "
//									+ mapEv.getMatchMap());
//							System.out.println("   MatchMap alt. repr.: "
//									+ mapEv.getMatchMapList());
//							System.out.println("   is Head?: "
//									+ mapEv.isHead());
//							System.out.println("   is Overmatch?: "
//									+ mapEv.isOvermatch());
//							System.out.println("   Sources: "
//									+ mapEv.getSources());
//							System.out.println("   Positional Info: "
//									+ mapEv.getPositionalInfo());
//							PBTerm term = mapEv.getTerm();
//							System.out.println("   pbterm: "
//									+ term.getName());
//						}
//					}
//				}
//			}
//
//		}
//	}
//}

}
