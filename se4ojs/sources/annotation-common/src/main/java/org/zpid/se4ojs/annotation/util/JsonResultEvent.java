package org.zpid.se4ojs.annotation.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonResultEvent implements AnnotationEvent {

	JsonNode result;

	public JsonResultEvent(JsonNode result) {
		super();
		this.result = result;
	}

	public JsonNode getResult() {
		return result;
	}

	
	
}
