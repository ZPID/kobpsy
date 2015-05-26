package org.zpid.se4ojs.annotation;

import com.hp.hpl.jena.rdfxml.xmloutput.impl.Basic;

public class BasicSubstitute extends Basic {

	private static final CharSequence AMPERSAND = "&";
	private static final CharSequence AMPERSAND_IN_URI = "&amp;";

	@Override
	protected String substitutedAttribute(String s) {
		
		if (s.contains("http://bioportal.bioontology.org/ontologies/")) {
			return s;
		} 
		return super.substitutedAttribute(s);
	}

}
