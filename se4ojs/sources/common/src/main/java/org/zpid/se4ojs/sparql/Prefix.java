package org.zpid.se4ojs.sparql;

import java.util.NoSuchElementException;

public enum Prefix {

	/** Namespace for representing content in RDF. */
	APPLICATION("http://purl.org/NET/mediatypes/application/", "application"),

	BIRO ("http://purl.org/spar/biro/", "biro"),

	C4O("http://purl.org/spar/c4o/", "c4o"),

	CITO("http://purl.org/spar/cito/", "cito"),

    CNT("http://www.w3.org/2011/content#", "cnt"),

	CO("http://purl.org/co/", "co"),

	DATACITE("http://purl.org/spar/datacite/", "datacite"),

	DC("http://purl.org/dc/elements/1.1/", "dc"),

	DEO("http://purl.org/spar/deo/", "deo"),

	DCTERMS("http://purl.org/dc/terms/", "dcterms"),

	DOCO("http://purl.org/spar/doco/", "doco"),

	FABIO("http://purl.org/spar/fabio/", "fabio"),

	FOAF("http://xmlns.com/foaf/0.1/", "foaf"),

    FRBR("http://purl.org/vocab/frbr/core#", "frbr"),

    LITERAL ("http://www.essepuntato.it/2010/06/literalreification/", "literal"),

    /** Namespace for the Open Annotation ontology. */
    OA("http://www.w3.org/ns/oa#", "oa"),

	OWL("http://www.w3.org/2002/07/owl#", "owl"),

    PAV("http://purl.org/pav/", "pav"),

	PO ("http://www.essepuntato.it/2008/12/pattern#", "po"),

	PRISM ("http://prismstandard.org/namespaces/basic/2.0/", "prism"),

	PRO ("http://purl.org/spar/pro/", "pro"),

	PROV ("http://www.w3.org/ns/prov#", "prov"),

	PSYNDEX("http://www.zpid.de/psyndex#", "psyndex"),

	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),

	RDFS("http://www.w3.org/2000/01/rdf-schema#", "rdfs"),

	SKOS ("http://www.w3.org/2004/02/skos/core#", "skos"),

    SRO("http://salt.semanticauthoring.org/ontologies/sro#", "sro"),

    TVC("http://www.essepuntato.it/2012/04/tvc/", "tvc"),

    VCARD ("http://www.w3.org/2006/vcard/ns#", "vcard"),

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
