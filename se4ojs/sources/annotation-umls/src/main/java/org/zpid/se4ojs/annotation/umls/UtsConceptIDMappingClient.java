package org.zpid.se4ojs.annotation.umls;

import gov.nih.nlm.uts.webservice.content.AtomDTO;
import gov.nih.nlm.uts.webservice.content.Psf;
import gov.nih.nlm.uts.webservice.content.UtsWsContentController;
import gov.nih.nlm.uts.webservice.content.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.metadata.UtsWsMetadataController;
import gov.nih.nlm.uts.webservice.metadata.UtsWsMetadataControllerImplService;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.security.UtsWsSecurityControllerImplService;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.exception.AnnotationException;

/**
 * <p>
 * Web client that calls UTS 2.0 services to map metathesaurus-CUIs to 
 * the concept IDs of the UMLS source vocabularies configured by the user. 
 * </p>
 * 
 * @Singleton
 * 
 * @author barth
 *
 */
public class UtsConceptIDMappingClient {
	
	static final String DEFAULT_MULTI_WORD_DELIM = "_";

	static final String UMLS_VERSION = Config.getUmlsVersionForUtsServices();

	static final String OMIT_STOPWORDS = "stopWords";
	
	/** The singleton instance of this class. */
	private static UtsConceptIDMappingClient utsConceptIDMappingClient;

	private Logger log = Logger.getLogger(UtsConceptIDMappingClient.class);

	/** The set of ontologies. */
	private OntologyMappingHandler mappingHandler;
	
	/** Stores an object that passes properties to the service. */
	private Psf psf;

	private Map<String, Set<BOSourceConcept>> metaIdToSourceConcepts = new HashMap<String, Set<BOSourceConcept>>();

	private Set<String> ontologies;

	private UtsServiceTicketHandler utsServiceTicketHandler;

	private UtsConceptIDMappingClient(OntologyMappingHandler mappingHandler, UtsServiceTicketHandler ticketHandler) throws UtsFault_Exception {
		super();
		this.mappingHandler = mappingHandler;
		this.ontologies = mappingHandler.getOntologies();
		this.utsServiceTicketHandler = ticketHandler;
		init();
	}
	
	void init() throws UtsFault_Exception {
		utsServiceTicketHandler = initServices();
		psf = createPsf();
	}
	
	public static UtsConceptIDMappingClient getInstance(OntologyMappingHandler mappingHandler) throws UtsFault_Exception {
		if (utsConceptIDMappingClient != null) {
			return utsConceptIDMappingClient;
		}
		return new UtsConceptIDMappingClient(mappingHandler, null);
	}

	static UtsConceptIDMappingClient getInstance(OntologyMappingHandler ontologyMappingHandler,
			UtsServiceTicketHandler ticketGetter) throws UtsFault_Exception {
		return new UtsConceptIDMappingClient(ontologyMappingHandler, ticketGetter);
	}

	/**
	 * Maps the given cui to the IDs of the source vocabularies that are atoms
	 * of the metathesaurus concept designated by the cui.
	 * 
	 * @param cui the cui to resolve
	 * @return a map containing the sourceID concept and the acronym of the ontology it belongs to
	 */
	public Set<BOSourceConcept> mapCuiToAtomIDs(String cui, String cuiPrefLabel) {
		Set<BOSourceConcept> sourceConcepts = metaIdToSourceConcepts.get(cui);
	    if (sourceConcepts != null) {
	    	return sourceConcepts;
	    } else {
	    	sourceConcepts = new HashSet<>();
			try {
				List<AtomDTO> conceptAtoms = utsServiceTicketHandler.getContentService().getConceptAtoms(
						utsServiceTicketHandler.getSingleUseTicket(),
						UMLS_VERSION, cui, psf);
				log.info("cui pref label: " + cuiPrefLabel);
				for (AtomDTO atom : conceptAtoms) {
					String rootSource = atom.getRootSource();
					String ui = "";
					if (rootSource.equals("HL7V3.0")) {
						ui = atom.getConcept().getUi();
					} else {
						ui = atom.getCode().getSourceUi();
					}
					if (ui == null) {
						log.warn("no id for concept: " + cui + "with term: "
								+ cuiPrefLabel);
					} else {
						log.info("id: " + ui);
						log.info("source: " + rootSource);
						String sourcePreferredName = atom.getCode().getDefaultPreferredName();
						log.info("pref term: "
								+ extractConceptName(rootSource, sourcePreferredName));
						log.info("term type: " + atom.getTermType());
						if (!cuiPrefLabel.equals(extractConceptName(rootSource, sourcePreferredName))) {
							log.warn("Pref terms differ!");
						}
						sourceConcepts.add(new BOSourceConcept(
								ui, extractConceptName(rootSource, sourcePreferredName),
								rootSource, atom.getTermType()));
					}
				}
			} catch (gov.nih.nlm.uts.webservice.content.UtsFault_Exception e) {
				log.error(("Error mapping UMLS metathesaurus concept (cui / label) to source vocab ids:"
						+ " " + cui + cuiPrefLabel));
				e.printStackTrace();
			}	
	    }
	    metaIdToSourceConcepts.put(cui, sourceConcepts);
		return sourceConcepts;
	}

	/**
	 * Capitalizes the concept name.
	 * Uses the specified delimiter for multi word concepts.
	 * 
	 * @param source the UMLS abbr. of the ontology source
	 * @param defaultPrefName the preferred name of the concept within the source ontology
	 * @return the concept name as it is used in the Concept URI.
	 */
	String extractConceptName(String source, String defaultPrefName) {
		log.info("source default pref name: " + defaultPrefName);
		String prefNameToUri = defaultPrefName.replace(" ", DEFAULT_MULTI_WORD_DELIM);
		if (prefNameToUri.compareTo(defaultPrefName) != 0) {
			OntologyMappingNcboUmls sourceMapping = mappingHandler.getSource(source);
			String delim = sourceMapping.getMultiWordDelim();
			if (StringUtils.isEmpty(delim)) {
				delim = "";
			}
			StringTokenizer tokenizer = new StringTokenizer(prefNameToUri,
					DEFAULT_MULTI_WORD_DELIM, true);
			StringBuilder modPrefName = new StringBuilder();
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.compareTo(DEFAULT_MULTI_WORD_DELIM) != 0 &&
						sourceMapping.getStopwords().equals(OMIT_STOPWORDS) && token.length() <= 2) {
					log.info("omitted stopword: " + token + "source: " + source + "term" + defaultPrefName);
				} else{
					String firstLetter = token.substring(0,1);
					modPrefName.append(token.replace(firstLetter, firstLetter.toUpperCase()));
				}
			}	
			String adaptedPrefName = modPrefName.toString();
			adaptedPrefName = adaptedPrefName.replace("__", "_");
			if (delim != DEFAULT_MULTI_WORD_DELIM) {
				 adaptedPrefName = adaptedPrefName.replace(DEFAULT_MULTI_WORD_DELIM, delim);
			}
			return adaptedPrefName;
		}

		return prefNameToUri;
	}

	/**
	 * Creates an object that stores properties to be adhered to by the web service.
	 * 
	 * @return the initialized psf object
	 */
	private Psf createPsf() {
		Psf psf = new Psf();
		psf.getIncludedSources().addAll(ontologies);		

//		psf.setCaseSensitive(false);
//		psf.setIncludedLanguage("en"); //TODO use config prop?
		psf.setIncludeObsolete(false);
		return psf;
	}

	/**
	 * Initializes the UTS Services and checks the user-specified UMLS version
	 * for correctness.
	 * 
	 * @param utsContentService the uninitialized content service.
	 * @return the initialized content service
	 * @throws UtsFault_Exception 
	 */
	UtsServiceTicketHandler initServices() throws UtsFault_Exception {
		
		if (utsServiceTicketHandler == null) {
			utsServiceTicketHandler = new UtsServiceTicketHandler();
		}
		utsServiceTicketHandler.createProxyGrantTicket();


		String allUMLSVersions = "";
		try {
			allUMLSVersions = utsServiceTicketHandler.getAllUMLSVersions();
			if(UMLS_VERSION == null || !allUMLSVersions.contains(UMLS_VERSION)) {
				throw new AnnotationException("Wrong UMLS Version for UTS serices specified. Please check the configuration.");
			}
		} catch (gov.nih.nlm.uts.webservice.metadata.UtsFault_Exception e) {
			log.error("UMLS Concept ID matching error. Unable to retrieve available UMLS versions from UTS service." 
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
		return utsServiceTicketHandler;
	}

	/**
	 * @return the proxyGrantTicketGetter
	 */
	UtsServiceTicketHandler getUtsServiceTicketHandler() {
		return utsServiceTicketHandler;
	}
	
}

class UtsServiceTicketHandler {
	
	// TODO move to Config
	private static final String UTS_SERVICE_BASE_URI = "http://umlsks.nlm.nih.gov";
	
	public static final int EXPIRY_INTERVALL = 8;
	
	private Date proxyGrantTicketExpiryTime;
	
	private Logger log = Logger.getLogger(UtsServiceTicketHandler.class);
	
	/** The security service that obtains the tickets. */
	private UtsWsSecurityController	utsSecurityService;
	
	/** This ticket authenticates the user at UTS and is valid for 8 hours. */
	private String proxyGrantTicket;

	/**
	 * Gets a ticket which authenticates the user at UTS and is valid for 8 hours.
	 * 
	 * @return the ticket
	 * @throws UtsFault_Exception if ticket could not be obtained
	 */
	String createProxyGrantTicket() throws UtsFault_Exception {
		
		utsSecurityService = 
				(new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, EXPIRY_INTERVALL);
		proxyGrantTicketExpiryTime = cal.getTime();
		proxyGrantTicket = utsSecurityService.getProxyGrantTicket(Config.getUMLSUsername(),
						Config.getUMLSPassword());
		return proxyGrantTicket;
	}
	
	public String getAllUMLSVersions() throws gov.nih.nlm.uts.webservice.metadata.UtsFault_Exception {
		return getMetadataService().getAllUMLSVersions(getSingleUseTicket());
	}

	UtsWsMetadataController getMetadataService() {
		return (new UtsWsMetadataControllerImplService())
				.getUtsWsMetadataControllerImplPort();
	}

	UtsWsContentController getContentService() {
		return (new UtsWsContentControllerImplService())
		   .getUtsWsContentControllerImplPort();
	}

	String getSingleUseTicket() {
		
		try {
			if (getDate().compareTo(proxyGrantTicketExpiryTime)  > 0) {
				proxyGrantTicket = createProxyGrantTicket();
			}
			return utsSecurityService.getProxyTicket(proxyGrantTicket,
					UTS_SERVICE_BASE_URI);
		} catch (Exception e) {
			//no exception thrown, because might be a one-off problem and users might be able to live with
			//incomplete annotations rather than interrupting the program.
			log.error("Unable to obtain a single use ticket for UTS services.");
			return null;
		}
	}
	
	
	Date getDate() {
		return new Date();
	}
	
	
}