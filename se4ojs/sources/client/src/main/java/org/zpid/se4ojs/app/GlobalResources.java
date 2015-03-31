package org.zpid.se4ojs.app;

import org.zpid.se4ojs.app.Config;

/**
 * <p>
 * This class defines a set of resource constants required for both biotea and zpid.
 * </p>
 * 
 * @author barth
 *
 */
public class GlobalResources {
	/** Institution base URI, as defined in the configuration.properties */
	public final static String BASE_URL = Config.INSTITUTION_URL; //"http://" + Config.getBioteaBase(); // + "/pubmedOpenAccess/rdf/"

	/** Stores the expected file type for input files*/
	public static final String INPUT_FILE_SUFFIX = Config.getInputFileSuffix();
	
	/** Constant for the doi organisation's URI. */
	public final static String doiURI = "http://dx.doi.org/";
	
	/** Additional URIs, fixed */
	public final static String pmcURI = "http://www.ncbi.nlm.nih.gov/pmc/articles/PMC"; 	
	public final static String NLM_JOURNAL_CATALOG = "http://www.ncbi.nlm.nih.gov/nlmcatalog?term=";
	public final static String pubMedURI = "http://www.ncbi.nlm.nih.gov/pubmed/";

}
