package org.zpid.se4ojs.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads the configuration properties from a file.
 * This class is a Singleton.
 * @author barth
 *
 */
public class Config {
	
	private static final Config INSTANCE = new Config();
	private static Logger LOGGER = null;
	private static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";
	
	private Properties properties;
    private String institutionUrl;
    
	private Config() {
		super();
	    LOGGER = LogManager.getLogger(Config.class);
		initialize();
	}
	
	private void initialize() {
		//Load the properties file
		try {
			URI jarpath = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			LOGGER.debug("jarpath: " + jarpath.toString());
			Path parentPath = Paths.get(jarpath).getParent();
//			LOGGER.debug("jarpath's parent: " + parentPath);
			Path propFilePath = Paths.get(parentPath.toString(), CONFIG_PROPERTIES_FILE_NAME);
//			LOGGER.debug("if we run the app from the jar, properties should be located here: " + propFilePath);
			if (!Files.exists(propFilePath, LinkOption.NOFOLLOW_LINKS)) {
//				propFilePath = Paths.get(Config.class.getResourceAsStream(CONFIG_PROPERTIES_FILE_NAME).toString());
				propFilePath = Paths.get(Config.class.getClassLoader().getResource(CONFIG_PROPERTIES_FILE_NAME).toURI());
//				propFilePath = Paths.get(INSTANCE.getClass().getClassLoader().getResource(CONFIG_PROPERTIES_FILE_NAME).toURI());
			}
			LOGGER.debug("properties file was located here:: " + propFilePath);
			if (!Files.exists(propFilePath, LinkOption.NOFOLLOW_LINKS)) {
				throw new IOException();
			}
			properties = new Properties();
			properties.load(new FileInputStream(
					propFilePath.toFile()));
			institutionUrl = getBaseURI() + "/";
			
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Unable to locate the properties file");
			e.printStackTrace();
		}
	}

    /**
     * Base URI of the organistation or institute that does the rdfization (e.g. "zpid"). 
     */
    public static String getInstitutionUrl() {
		return INSTANCE.institutionUrl;
	}

	private String getProperty(String prop) {
        if (properties.containsKey(prop)) {
            return properties.getProperty(prop);
        }
        return ("");
    }

    
    /**
     * Gets the basic part of the URI for the generated resources.
     * 
     * @return the base URI
     */
    private String getBaseURI() {
        String baseUri = getProperty("baseUri");
        if (baseUri.isEmpty()) {
        	LOGGER.error("No base URI provided in Properties file!");
        }
        return baseUri;
    }
    
    /**
     * Gets the basic part of the URI for the generated resources.
     * 
     * @return the base URI
     */
    public static String getProxy() {
        return INSTANCE.getProperty("conn.proxy");
    }


    //NCBO Annotator
    public static String getNCBOServiceURL(){
    	return (INSTANCE.getProperty("ncbo.service.url"));
    }

    public static String getNCBOAPIKey(){
    	return (INSTANCE.getProperty("ncbo.apikey"));
    }
    
    public static String getNCBOStopwords(){
    	return (INSTANCE.getProperty("ncbo.stopwords"));
    }
    
    public static String getNCBOBaseConceptUri() {
    	return (INSTANCE.getProperty("ncbo.baseConceptUri"));
    }
    
    //UMLS Annotator
    public static String getUMLSUsername(){
    	return (INSTANCE.getProperty("umls.username"));
    }
    
    public static String getUMLSPassword(){
    	return (INSTANCE.getProperty("umls.password"));
    }
    
    public static String getUmlsOntologiesAsString() {
		return INSTANCE.getProperty("umls.annotator.ontologies");
    }
    public static String getNcboOntologiesAsString() {
    	return INSTANCE.getProperty("ncbo.annotator.ontologies");
    }
    
    public static Set<String> getUmlsOntologiesAsSet() {
    		StringTokenizer tokenizer = new StringTokenizer(INSTANCE.getProperty("umls.annotator.ontologies"), ",");
    		Set<String> ontologySet = new HashSet<>();
    		while (tokenizer.hasMoreTokens()) {
    			ontologySet.add(tokenizer.nextToken());
    		}
    		return ontologySet;
    }
    
    public static String getUmlsBaseConceptUri() {
    	return INSTANCE.getProperty("umls.baseConceptUri");
    }
    
    public static boolean isAddNcboConceptUris() {
    	if (INSTANCE.getProperty("umls.addNcboConceptUris").compareToIgnoreCase(Boolean.TRUE.toString()) == 0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Returns the Version of the UMLS used by the
     * UTS services.
     * 
     * @return the UMLS version for UTS services
     */
    public static String getUmlsVersionForUtsServices() {
    	return (INSTANCE.getProperty("umls.version.uts"));
    }
    
	public static String getUmlsMetamapOptions() {
		String options = INSTANCE.getProperty("umls.metamap.options");
		if (!StringUtils.isEmpty(options)) {
			return options;
		}
		return "";
	}
    
    //Other URLS
    public static String getPubMedURL(){
    	return (INSTANCE.getProperty("pubmed.url"));
    }
    
    public static String getDOIURL(){
    	return (INSTANCE.getProperty("doi.url"));
    }
    

    /**
     * The expected file types for articles to be processed.
     * 
     * @return the suffix of the file type
     */
	public static String getInputFileSuffix() {
		return INSTANCE.getProperty("input.file.suffix");
	}

	public static String getLanguages() {
		String languages = INSTANCE.getProperty("languagesIncluded");
		if (StringUtils.isEmpty(languages)) {
			languages = "en";
		}
		return languages;
	}

	public static InputStream getOntologyMappingFile() {
		return Config.class.getClassLoader().
		getResourceAsStream("ontologyNameMappings_Ncbo_Umls.txt");
	}

	public static boolean isUseBrowserUrlAsConceptId() {
		if (INSTANCE.getProperty("annotation.browserUrlAsAnnotationTopic")
				.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		return false;
	}

	public static boolean isGenerateCrossrefApiPdf() {
		if (INSTANCE.getProperty("crossrefApi.links.pdf")
				.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		return false;
	}
}