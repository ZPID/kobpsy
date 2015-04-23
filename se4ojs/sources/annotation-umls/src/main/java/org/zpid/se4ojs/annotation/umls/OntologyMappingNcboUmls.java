package org.zpid.se4ojs.annotation.umls;


/**
 * <p>
 * This class stores information about a subset of the ontologies available for
 * the annotators.
 * </p>
 * <p>
 * It stores a mapping between the different ontology acronyms used by Ncbo and
 * Umls annotators.
 * </p>
 * 
 * @author barth
 *
 */
public class OntologyMappingNcboUmls {

	private String ontoName;

	private String ncboAbbr;

	private String umlsAbbr;
	
	private String ncboOntologyUri;

	private String conceptSeparator = "";

	private String conceptRepresentation = "";

	private String stopwords = "";

	private String multiWordDelim = "";

	public OntologyMappingNcboUmls(String ontoName, String ncboAbbr,
			String umlsAbbr, String ncboOntologyUri, String sep, String rep, String... specialOpts) {
		super();
		this.ontoName = ontoName;
		this.ncboAbbr = ncboAbbr;
		this.umlsAbbr = umlsAbbr;
		this.ncboOntologyUri = ncboOntologyUri;
		this.conceptSeparator = sep;
		this.conceptRepresentation = rep;
		this.multiWordDelim = specialOpts[0];
		this.stopwords = specialOpts[1];
	}

	/**
	 * @return the ontoName
	 */
	public String getOntoName() {
		return ontoName;
	}

	/**
	 * @return the ncboAbbr
	 */
	public String getNcboAbbr() {
		return ncboAbbr;
	}

	/**
	 * @return the umlsAbbr
	 */
	public String getUmlsAbbr() {
		return umlsAbbr;
	}

	/**
	 * @return the ncboOntologyUri
	 */
	public String getNcboOntologyUri() {
		return ncboOntologyUri;
	}

	/**
	 * @return the conceptSeparator
	 */
	public String getConceptSeparator() {
		return conceptSeparator;
	}

	/**
	 * @return the conceptRepresentation
	 */
	public String getConceptRepresentation() {
		return conceptRepresentation;
	}

	/**
	 * @return the stopwords
	 */
	public String getStopwords() {
		return stopwords;
	}

	public String getMultiWordDelim() {
		return multiWordDelim;
	}

}
