/**
 * 
 */
package org.zpid.se4ojs.annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a concept.
 * 
 * @author barth
 */
public class BOConcept {
	
	private String conceptUri;
	private String preferredName;
	private String name;
	/** The uris denoting the source terminology and conceptId. */
	private List<String> topicUris = new ArrayList<>();
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
}
