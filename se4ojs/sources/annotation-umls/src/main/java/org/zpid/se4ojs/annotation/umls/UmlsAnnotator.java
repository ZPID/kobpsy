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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.AoAnnotator;
import org.zpid.se4ojs.annotation.BOConcept;
import org.zpid.se4ojs.annotation.BOContext;
import org.zpid.se4ojs.annotation.Prefix;
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
	private static final String UMLS_URI = "http://umls.nlm.nih.gov/sab";
	private static final String UMLS_CUI_URI_INFIX = "cui";
	private static final String ZPID_APA_URI = "http://www.zpid.de/psyndex#";
	private static final String META_MAP_URL = "http://metamap.nlm.nih.gov";
	private static MetaMapApi api = new MetaMapApiImpl();
	
	private Logger log = Logger.getLogger(UmlsAnnotator.class);
	private UtsConceptIDMappingClient utsConceptMapper;

	public UmlsAnnotator(String ontologies) {
		init(ontologies);
	}

	/** 
	 * Initializes the MetaMap program via the MetaMap API.
	 * Sets the ontologies that will be used for annotations.
	 */
	private void init(String ontologies) {
		api.setOptions(new StringBuilder("-R ").append(ontologies).toString());
		try {
			utsConceptMapper = UtsConceptIDMappingClient.getInstance();
		} catch (UtsFault_Exception e) {
			log.error("The UMLS concept ID matcher could not be initialized. Unable to "
					+ "map Metathesaurus IDs to IDs of the source vocabularies. ");
		}
	}

	public void annotate(String baseURi,File paper,
			List<StructureElement> topLevelElements, String outputDir) throws IOException {
		String out = paper.toPath().getFileName().toString().replace(".xml", "-umlsAnnotations_psy.rdf");
		
		out = out.replace(".XML", "-umlsAnnotations_psy.rdf");
		super.annotate(baseURi, paper, topLevelElements, Paths.get(outputDir, out));
	}

	public Map<BOConcept, List<BOContext>> annotateText(Model model, String paragraph,
			String subElementUri) throws Exception {
		
		Map<BOConcept, List<BOContext>> annotations = new HashMap<BOConcept, List<BOContext>>();
		List<Result> resultList = Collections.emptyList();
		try {
			resultList = api.processCitationsFromString(paragraph);
		} catch (Exception e) {
			log.error(String.format("UmlsAnnotation error: MetaMapError annotating paragraph %s.\n Exception: %s \n Text: %s\n\n",
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
							String url = setConceptUris(model, mapEv);
							concept.setConceptUri(url);
							addToConceptCount(url);
							BOContext context = addContext(model, url,
									subElementUri, mapEv.getConceptId(), mapEv);
							addBody(model, url, mapEv.getPreferredName());
							addMetaInfo(model, url, META_MAP_URL);
							List<BOContext> contexts = annotations.get(concept);
							if (contexts.isEmpty()) {
								contexts = new ArrayList<BOContext>();
							}
							contexts.add(context);
							annotations.put(concept, contexts);
						}
					}
				}
			}
		}
		return annotations;
	}

	/**
	 * TODO adapt concept count? Do we count each metathesaurus concept or each atom?
	 * Do we really list each atom?
	 * 
	 * @param model
	 * @param mapEv
	 * @param annotation
	 * @return
	 * @throws Exception
	 */
	private String setConceptUris(Model model, Ev mapEv)
			throws Exception {
		mapMetathesaurusCuiToAtomIDs(model, mapEv.getConceptId(), mapEv.getPreferredName());
		return setMetathesaurusUri(model, mapEv);
	}

	private void mapMetathesaurusCuiToAtomIDs(Model model, String cui, String cuiPrefTerm) {
		@SuppressWarnings("unused")
		Map<String, String> mapCuiToAtomIDs = utsConceptMapper.mapCuiToAtomIDs(cui, cuiPrefTerm);
	}

	private String setMetathesaurusUri(Model model, Ev mapEv) throws Exception {
		String url = createExactQualifier(model,
				mapEv.getConceptId(),
				mapEv.getPreferredName()
				);
		mapEv.getTerm().getName();
		return url;
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
		AnnotationUtils.createResourceTriple(url, AnnotationUtils
				.createPropertyString(Prefix.AO, AO_HAS_TOPIC), AnnotationUtils
				.createUriString(UMLS_URI, UMLS_CUI_URI_INFIX,
						id), model);
//TODO		AnnotationUtils.createResourceTriple(url, AnnotationUtils
//				.createPropertyString(Prefix.AO, AO_HAS_TOPIC), AnnotationUtils
//				.createUriString(mapUmlsIdToApaId(id)), model);
		return url;
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
