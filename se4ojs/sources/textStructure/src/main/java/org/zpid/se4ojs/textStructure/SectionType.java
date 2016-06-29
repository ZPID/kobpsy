package org.zpid.se4ojs.textStructure;

import org.zpid.se4ojs.sparql.Prefix;


public enum SectionType {

	ABSTRACT("abstract", "Abstract", Prefix.SRO),
	CASES("cases", null, null),
	CONCLUSION("conclusions", "Conclusion", Prefix.SRO),
	DISCUSSION("discussion","Discussion", Prefix.SRO),
	INTRODUCTION("intro", "Introduction", Prefix.DEO),
	MATERIALS("materials", "Materials", Prefix.DEO),
	METHODS("methods", "Methods", Prefix.DEO),
	RESULTS("results", "Results", Prefix.DEO),
	SUBJECTS("subjects", null, null),
	OTHER("other", null, null);

	
	private String label;
	private String ontologyClass;
	private Prefix prefix;

	private SectionType(String label, String ontologyClass, Prefix prefix) {
		this.label = label;
		this.ontologyClass = ontologyClass;
		this.prefix = prefix;
	}

	public String getLabel() {
		return label;
	}

	public String getOntologyClass() {
		return ontologyClass;
	}
	
	public Prefix getPrefix() {
		return prefix;
	}

	public static SectionType getSectionTypeFromLabel(String label) {
		for (SectionType type : SectionType.values()) {
			if(type.getLabel().equals(label)) {
				return type;
			}
		}
		return SectionType.OTHER;
	}
	
}
