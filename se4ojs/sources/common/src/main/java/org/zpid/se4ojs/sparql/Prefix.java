package org.zpid.se4ojs.sparql;

import java.util.NoSuchElementException;

public enum Prefix {
	
	/** Namespace for representing content in RDF. */
	APPLICATION("http://purl.org/NET/mediatypes/application/", "application"),
	
	C4O("http://purl.org/spar/c4o/", "c4o"),
	
    CNT("http://www.w3.org/2011/content#", "cnt"),
    
	CO("http://purl.org/co/", "co"),
	
	DC("http://purl.org/dc/elements/1.1/", "dc"),
	
	DEO("http://purl.org/spar/deo/", "deo"),

	DCTERMS("http://purl.org/dc/terms/", "dcterms"),
	
	DOCO("http://purl.org/spar/doco/", "doco"),
	
	FABIO("http://purl.org/spar/fabio/", "fabio"),
	
	FOAF("http://xmlns.com/foaf/0.1/", "foaf"),
	
    FRBR("http://purl.org/vocab/frbr/core#", "frbr"),
    
    /** Namespace for the Open Annotation ontology. */
    OA("http://www.w3.org/ns/oa#", "oa"),
	
	OWL("http://www.w3.org/2002/07/owl#", "owl"),
	
    PAV("http://purl.org/pav/", "pav"),
    
	PO ("http://www.essepuntato.it/2008/12/pattern#", "po"),

	PROV("http://www.w3.org/ns/prov#", "prov"),

	PSYNDEX("http://www.zpid.de/psyndex#", "psyndex"),
	
	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"), 
	
	RDFS("http://www.w3.org/2000/01/rdf-schema#", "rdfs"),
	
	SKOS ("http://www.w3.org/2004/02/skos/core#", "skos"),
	
    SRO("http://salt.semanticauthoring.org/ontologies/sro#", "sro"),
    
	ZPID("http://www.zpid.de/resource", "zpid"),
	
	//Extended SPAR ontology
	ZPID_DOCO("http://www.zpid.de/zpid_doco#", "zpid_doco"), 

	PLACEHOLDER ("http://www.zpid.de/placeholder/", "zpid_placeholder_onto"),
	;

	private String url;
	private String ns;

	private Prefix(String url, String ns) {
		this.url = url;
		this.ns = ns;
	}

	public String getURL() {
		return (this.url);
	}

	public String getNS() {
		return (this.ns);
	}

	public String getPrefix() {
		return "PREFIX " + this.ns + ":<" + this.url + "> ";
	}

	public static Prefix getByNS(String ns) throws NoSuchElementException {
		for (Prefix prefix : Prefix.values()) {
			if (prefix.getNS().equals(ns)) {
				return prefix;
			}
		}
		throw new NoSuchElementException("The prefix with NS " + ns
				+ " cannot be resolved.");
	}
}
