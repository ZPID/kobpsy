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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.exception.AnnotationException;

/**
 * <p>
 * Web client that calls UTS 2.0 services to map metathesaurus-CUIs to 
 * the concept IDs of the UMLS source vocabularies configured by the user. 
 * </p>
 *  
 * @author barth
 *
 */
public class UtsConceptIDMappingClient {
	
	private static final String UMLS_VERSION = Config.getUmlsVersionForUtsServices();
	
	// TODO move to Config
	private static final String serviceName = "http://umlsks.nlm.nih.gov";
	
	private UtsWsSecurityController	utsSecurityService =
			(new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();

	private Logger log = Logger.getLogger(UtsConceptIDMappingClient.class);

	/** The set of ontologies. */
	private Set<String> ontologies = Config.getUmlsOntologiesAsSet();
	
	/** This ticket authenticates the user at UTS and is valid for 8 hours. */
	private String proxyGrantTicket;
	
	private Psf psf;

	private UtsWsContentController utsContentService;

	public UtsConceptIDMappingClient() throws UtsFault_Exception {
		super();
		init();
	}
	
	private void init() throws UtsFault_Exception {
		proxyGrantTicket = getProxyGrantTicket();
		utsContentService = initServices(utsContentService);
		psf = createPsf();
	}

	
	/**
	 * Gets a ticket which authenticates the user at UTS and is valid for 8 hours.
	 * 
	 * @return the ticket
	 * @throws UtsFault_Exception if ticket could not be obtained
	 */
	private String getProxyGrantTicket() throws UtsFault_Exception {
		UtsWsSecurityController securityService = 
				(new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();
		return securityService.getProxyGrantTicket(Config.getUMLSUsername(),
						Config.getUMLSPassword());

	}

	/**
	 * Maps the given cui to the IDs of the source vocabularies that are atoms
	 * of the metathesaurus concept designated by the cui.
	 * 
	 * @param cui the cui to resolve
	 * @return a map containing the sourceID concept and the acronym of the ontology it belongs to
	 */
	public Map<String, String> mapCuiToAtomIDs(String cui, String cuiPrefLabel) {
		Map<String, String> atoms = new HashMap<>();
	    
		try {
			List<AtomDTO> conceptAtoms = utsContentService.getConceptAtoms(getSingleUseTicket(), UMLS_VERSION, "C0870087", psf);
			for (AtomDTO atom : conceptAtoms) {
				String rootSource = atom.getRootSource();
				String ui = atom.getSourceUi();
				if (ontologies.contains(rootSource)) {
					atoms.put(ui, rootSource);
					System.out.println("id: " + ui);
					System.out.println("source: " + atom.getRootSource() + "\n");
					log.info("id: " + ui);
					log.info("source: " + atom.getRootSource());
					log.info("pref terms: " + atom.getConcept().getDefaultPreferredName());
					if (!cuiPrefLabel.equals(atom.getConcept().getDefaultPreferredName())) {
						log.info("Pref terms differ!");
					}
					log.info("tree position count: " + atom.getTreePositionCount());
				} else {
					log.warn("No atoms found for CUI: " + cui + "with name : " + cuiPrefLabel);
				}
			}

		} catch (gov.nih.nlm.uts.webservice.content.UtsFault_Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return atoms;
	}

	/**
	 * Creates an object that stores properties to be adhered to by the web service.
	 * 
	 * @return the intialized psf object
	 */
	private Psf createPsf() {
		Psf psf = new Psf();
//		psf.setCaseSensitive(false);
//		psf.setIncludedLanguage("en"); //TODO use config prop?
//		psf.setIncludeObsolete(false);
		return psf;
	}

	/**
	 * Initializes the UTS Services and checks the user-specified UMLS version
	 * for correctness.
	 * 
	 * @param utsContentService the uninitialized content service.
	 * @return the initialized content service
	 */
	private UtsWsContentController initServices(
			UtsWsContentController utsContentService) {
		UtsWsMetadataController utsMetadataService = null;

		utsContentService = (new UtsWsContentControllerImplService())
				.getUtsWsContentControllerImplPort();

		utsMetadataService = (new UtsWsMetadataControllerImplService())
				.getUtsWsMetadataControllerImplPort();

		String allUMLSVersions = "";
		try {
			allUMLSVersions = utsMetadataService.getAllUMLSVersions(getSingleUseTicket());
			if(UMLS_VERSION == null || !allUMLSVersions.contains(UMLS_VERSION)) {
				throw new AnnotationException("Wrong UMLS Version for UTS serices specified. Please check the configuration.");
			}
		} catch (gov.nih.nlm.uts.webservice.metadata.UtsFault_Exception e) {
			log.error("UMLS Concept ID matching error. Unable to retrieve available UMLS versions from UTS service." 
					+ e.getLocalizedMessage());
			e.printStackTrace();
		}
		return utsContentService;
	}
	
	private String getSingleUseTicket() {
		try {
			return utsSecurityService.getProxyTicket(proxyGrantTicket,
					serviceName);
		} catch (Exception e) {
			//no exception thrown, because might be a one-off problem and users might be able to live with
			//incomplete annotations rather than interrupting the program.
			log.error("Unable to obtain a single use ticket for UTS services.");
			return null;
		}
	}

	/**
	 * Just for testing..
	 * 
	 * @param args
	 * @throws UtsFault_Exception
	 */
	public static void main(String[] args) throws UtsFault_Exception {
		UtsConceptIDMappingClient mappingClient = new UtsConceptIDMappingClient();
		mappingClient.mapCuiToAtomIDs("C0870087", "Abuse of Power");
	}
}
