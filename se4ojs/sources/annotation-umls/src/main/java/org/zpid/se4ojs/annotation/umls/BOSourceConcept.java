/**
 * 
 */
package org.zpid.se4ojs.annotation.umls;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * <p>
 * Class that stores information about a "source-concept".
 * </p>
 * <p>
 * Source concept is defined as a concept from the UMLS, which
 * is not a metathesaurs-concept, but linked to one.
 * </p>
 * 
 * @author barth
 *
 */
public final class BOSourceConcept {

	private String conceptId;
	
	private String preferredName;
	
	private String sourceVocabulary;
	
	private String termType;

	public BOSourceConcept(String conceptId, String preferredName,
			String sourceVocabulary, String termType) {
		super();
		this.conceptId = conceptId;
		this.preferredName = preferredName;
		this.sourceVocabulary = sourceVocabulary;
		this.termType = termType;
	}

	/**
	 * @return the conceptId
	 */
	public String getConceptId() {
		return conceptId;
	}

	/**
	 * @return the preferredName
	 */
	public String getPreferredName() {
		return preferredName;
	}

	/**
	 * @return the sourceVocabulary
	 */
	public String getSourceVocabulary() {
		return sourceVocabulary;
	}

	/**
	 * @return the termType
	 */
	public String getTermType() {
		return termType;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BOSourceConcept rhs = (BOSourceConcept) obj;
		return new EqualsBuilder()
				.append(conceptId, rhs.conceptId)
				.append(preferredName, rhs.preferredName)
				.append(sourceVocabulary, rhs.sourceVocabulary)
				.append(termType, rhs.termType).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(conceptId)
				.append(preferredName).append(sourceVocabulary)
				.append(termType).toHashCode();
	}
}
