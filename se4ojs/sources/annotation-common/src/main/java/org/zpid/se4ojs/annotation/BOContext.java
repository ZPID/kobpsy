/**
 * 
 */
package org.zpid.se4ojs.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Describes the context of each occurrence of an annotation in the text.
 * @author barth
 */
public class BOContext {
	
	/** The URI of the text structure element (i.e. paragraph) the annotation occurs in. */
	private String subElementUri;
	
	/** Stores that the context is the nth occurrence of this concept 
	 *  - used to create unique URIs for the annotation context. 
	 */
	private String contextUri;
	
	public List<String> matchedWords = new ArrayList<>();
	
	/** offset from the beginning of the paragraph and range for the matched words. */
	public List<Pair<Integer, Integer>> offsetsAndRanges = new ArrayList<>();

	public String getSubElementUri() {
		return subElementUri;
	}

	public void setSubElementUri(String subElementUri) {
		this.subElementUri = subElementUri;
	}
	
	public String getContextUri() {
		return contextUri;
	}

	public void setContextUri(String contextUri) {
		this.contextUri = contextUri;
	}

	public List<String> getMatchedWords() {
		return matchedWords;
	}

	public void setMatchedWords(List<String> matchedWords) {
		this.matchedWords = matchedWords;
	}

	public List<Pair<Integer, Integer>> getOffsetsAndRanges() {
		return offsetsAndRanges;
	}

	public void setOffsetsAndRanges(List<Pair<Integer, Integer>> offsetsAndRanges) {
		this.offsetsAndRanges = offsetsAndRanges;
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
		BOContext rhs = (BOContext) obj;
		return new EqualsBuilder()
				.append(subElementUri, rhs.subElementUri)
				.append(contextUri, rhs.contextUri)
				.append(matchedWords, rhs.matchedWords)
				.append(offsetsAndRanges, rhs.offsetsAndRanges)
				.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(19, 39)
				.append(subElementUri)
				.append(contextUri)
				.append(matchedWords)
				.append(offsetsAndRanges).toHashCode();
	}
}
