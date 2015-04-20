package org.zpid.se4ojs.app;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Config {
	
	private static ResourceBundle res = ResourceBundle.getBundle("config");
	private static Logger logger = Logger.getLogger(Config.class);
    public static boolean useBio2RDF = false;
    /**
     * Used only when files are directly uploaded to Virtuoso (not recommended)
     */
    public static final String PUBMED_OA = "pubmedOpenAccess";
    /**
     * Base URI of the organistation or institute that does the rdfization (e.g. "biotea" or "zpid". 
     */
    public final static String INSTITUTION_URL = Config.getBaseURI() + "/";
    /**
     * URI for other datasets linked to
     */
    public static final String IDENTIFIERS_ORG_PUBMED = "http://identifiers.org/pubmed/";
    public static final String BIO2RDF_PUBMED = "http://bio2rdf.org/pubmed:";

    public static String getProperty(String prop) {
        try {
            return res.getString(prop);
        } catch (Exception e) {
        	logger.warn("---WARNING configuration---: " + e.getMessage());
            return ("");
        }
    }
    
    //Virtual ids
    public static String[] getNCBOAnnotatorIncludeOnly() {    	
    	try {
    		return res.getString("ncbo.annotator.include.only").split(",");    		
    	} catch (Exception e) {
    		return null;
    	}
    }
    public static String[] getNCBOAnnotatorExclude() {    	
    	try {
    		return res.getString("ncbo.annotator.exclude").split(",");    		
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    /**
     * Gets the basic part of the URI for the generated resources.
     * 
     * @return the base URI
     */
    public static String getBaseURI() {
        try {
            return res.getString("baseUri");
        } catch (Exception e) {
            return ("biotea.ws");
        }
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
    
    
    //UMLS Annotator
    public static String getUMLSUsername(){
    	return (Config.getProperty("umls.username"));
    }
    
    public static String getUMLSPassword(){
    	return (Config.getProperty("umls.password"));
    }
    
    public static Set<String> getUmlsOntologiesAsSet() {
    		StringTokenizer tokenizer = new StringTokenizer(Config.getProperty("umls.annotator.ontologies"), ",");
    		Set<String> ontologySet = new HashSet<>();
    		while (tokenizer.hasMoreTokens()) {
    			ontologySet.add(tokenizer.nextToken());
    		}
    		return ontologySet;
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
}