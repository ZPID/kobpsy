package org.zpid.se4ojs.annotation.util;

import com.fasterxml.jackson.databind.JsonNode;

public class MappingsResultEvent implements AnnotationEvent {
	private String conceptId;
	private JsonNode mappings;
	
	public MappingsResultEvent(String conceptId, JsonNode mappings) {
		this.conceptId = conceptId;
		this.mappings = mappings;
	}

	public String getConceptId() {
		return conceptId;
	}

	public JsonNode getMappings() {
		return mappings;
	}

	
}
