package org.zpid.se4ojs.annotation.util;

import com.fasterxml.jackson.databind.JsonNode;

public class AnnotationResultsAvailableEvent implements AnnotationEvent {

	private JsonNode annotationResults;
	private String subElementUri;

	public AnnotationResultsAvailableEvent(JsonNode results, String subElementUri) {
		this.annotationResults = results;
		this.subElementUri = subElementUri;
	}

	public JsonNode getAnnotationResults() {
		return annotationResults;
	}

	public String getSubElementUri() {
		return subElementUri;
	}

}
