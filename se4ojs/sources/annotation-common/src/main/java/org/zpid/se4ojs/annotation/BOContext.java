/**
 * 
 */
package org.zpid.se4ojs.annotation;

import java.util.ArrayList;
import java.util.List;

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
	
}
