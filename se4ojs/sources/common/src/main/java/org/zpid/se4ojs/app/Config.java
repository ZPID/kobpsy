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
import org.apache.log4j.Logger;

/**
 * Reads the configuration properties from a file.
 * First looks into the 
 * @author barth
 *
 */
public class Config {
	
	private static final Logger LOGGER;
	private static final Properties PROPERTIES = new Properties();
	private static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";
	
	static {
		//Load the properties file
		LOGGER = Logger.getLogger(Config.class);
		try {
			URI jarpath = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			Path parentPath = Paths.get(jarpath).getParent();
			Path propFilePath = Paths.get(parentPath.toString(), CONFIG_PROPERTIES_FILE_NAME);
			if (!Files.exists(propFilePath, LinkOption.NOFOLLOW_LINKS)) {
				propFilePath = Paths.get(Config.class.getClassLoader().getResource(CONFIG_PROPERTIES_FILE_NAME).toURI());
			}
			LOGGER.debug("properties file: " + propFilePath);
			PROPERTIES.load(new FileInputStream(
					propFilePath.toFile()));
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("Unable to locate the properties file");
			e.printStackTrace();
		}
	}
    /**
     * Base URI of the organistation or institute that does the rdfization (e.g. "zpid"). 
     */
    public final static String INSTITUTION_URL = Config.getBaseURI() + "/";

    private static String getProperty(String prop) {
        if (PROPERTIES.containsKey(prop)) {
            return PROPERTIES.getProperty(prop);
        }
        return ("");
    }

    
    /**
     * Gets the basic part of the URI for the generated resources.
     * 
     * @return the base URI
     */
    public static String getBaseURI() {
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
        return getProperty("conn.proxy");
    }


    //NCBO Annotator
    public static String getNCBOServiceURL(){
    	return (Config.getProperty("ncbo.service.url"));
    }

    public static String getNCBOAPIKey(){
    	return (Config.getProperty("ncbo.apikey"));
    }
    
    public static String getNCBOStopwords(){
    	return (Config.getProperty("ncbo.stopwords"));
    }
    
    public static String getNCBOBaseConceptUri() {
    	return (Config.getProperty("ncbo.baseConceptUri"));
    }
    
    //UMLS Annotator
    public static String getUMLSUsername(){
    	return (Config.getProperty("umls.username"));
    }
    
    public static String getUMLSPassword(){
    	return (Config.getProperty("umls.password"));
    }
    
    public static String getUmlsOntologiesAsString() {
		return Config.getProperty("umls.annotator.ontologies");
    }
    public static String getNcboOntologiesAsString() {
    	return Config.getProperty("ncbo.annotator.ontologies");
    }
    
    public static Set<String> getUmlsOntologiesAsSet() {
    		StringTokenizer tokenizer = new StringTokenizer(Config.getProperty("umls.annotator.ontologies"), ",");
    		Set<String> ontologySet = new HashSet<>();
    		while (tokenizer.hasMoreTokens()) {
    			ontologySet.add(tokenizer.nextToken());
    		}
    		return ontologySet;
    }
    
    public static String getUmlsBaseConceptUri() {
    	return Config.getProperty("umls.baseConceptUri");
    }
    
    public static boolean isAddNcboConceptUris() {
    	if (Config.getProperty("umls.addNcboConceptUris").compareToIgnoreCase(Boolean.TRUE.toString()) == 0) {
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
    	return (Config.getProperty("umls.version.uts"));
    }
    
	public static String getUmlsMetamapOptions() {
		String options = Config.getProperty("umls.metamap.options");
		if (!StringUtils.isEmpty(options)) {
			return options;
		}
		return "";
	}
    
    //Other URLS
    public static String getPubMedURL(){
    	return (Config.getProperty("pubmed.url"));
    }
    
    public static String getDOIURL(){
    	return (Config.getProperty("doi.url"));
    }
    

    /**
     * The expected file types for articles to be processed.
     * 
     * @return the suffix of the file type
     */
	public static String getInputFileSuffix() {
		return Config.getProperty("input.file.suffix");
	}

	public static String getLanguages() {
		String languages = Config.getProperty("languagesIncluded");
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
		if (Config.getProperty("annotation.browserUrlAsAnnotationTopic")
				.equalsIgnoreCase(Boolean.TRUE.toString())) {
			return true;
		}
		return false;
	}

}