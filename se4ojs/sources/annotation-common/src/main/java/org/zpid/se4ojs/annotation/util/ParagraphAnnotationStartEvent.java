package org.zpid.se4ojs.annotation.util;

public class ParagraphAnnotationStartEvent implements AnnotationEvent {

	
	private String subElementUri;
	
	public ParagraphAnnotationStartEvent(String subElementUri) {
		this.subElementUri = subElementUri;
	}

	public String getSubElementUri() {
		return subElementUri;
	}


	
}
