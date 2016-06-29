package org.zpid.se4ojs.annotation.util;

public class PaperAnnotationStartEvent implements AnnotationEvent {
	
	private AnnotationListener jsonResultListener;
	
	
	public PaperAnnotationStartEvent(AnnotationListener jsonResultListener) {
		super();
		this.jsonResultListener = jsonResultListener;
	}


	public AnnotationListener getJsonResultListener() {
		return jsonResultListener;
	}
}
