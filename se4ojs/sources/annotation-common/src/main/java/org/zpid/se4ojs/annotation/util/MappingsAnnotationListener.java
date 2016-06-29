package org.zpid.se4ojs.annotation.util;

import org.zpid.se4ojs.annotation.JsonAnnotationHandler;

public class MappingsAnnotationListener implements AnnotationListener {
	private AnnotationListener listener;
	
	public MappingsAnnotationListener(AnnotationListener listener) {
		super();
		this.listener = listener;
	}

	@Override
	public void handlePaperAnnotationFinished() {
		listener.handlePaperAnnotationFinished();
		
	}

	@Override
	public void update(AnnotationEvent event) {
		if (event instanceof MappingsResultEvent) {
			MappingsResultEvent mappingEvent = (MappingsResultEvent) event;
			if (listener instanceof JsonAnnotationHandler) {
				((JsonAnnotationHandler)listener).saveMapping(
						mappingEvent.getConceptId(), mappingEvent.getMappings());
			}
		}
	}

}
