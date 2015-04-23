/**
 * 
 */
package org.zpid.se4ojs.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a concept.
 * 
 * @author barth
 */
public class BOConcept {
	
	private String conceptUri;
	/** The uris denoting the source terminology and conceptId. */
	private List<String> topicUris = new ArrayList<>();
	
	private String preferredName;
	private String name;

	private String authorUri;
	
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getTopicUris() {
		return topicUris;
	}
	public void setTopicUris(List<String> topicUris) {
		this.topicUris = topicUris;
	}
	public String getAuthorUri() {
		return authorUri;
	}
	public void setAuthorUri(String authorUri) {
		this.authorUri = authorUri;
	}
	public void setConceptUri(String url) {
		this.conceptUri = url;
	}
	public String getConceptUri() {
		return conceptUri;
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
		BOConcept rhs = (BOConcept) obj;
		return new EqualsBuilder()
				.append(conceptUri, rhs.conceptUri)
				.append(preferredName, rhs.preferredName)
				.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(19, 39).append(conceptUri)
				.append(preferredName).toHashCode();
	}
}
