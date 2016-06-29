package org.zpid.se4ojs.annotation.util;

import java.util.List;

public interface Observable {
	

	public void addListener(AnnotationListener listener);
	
	public default void notifyListeners(List<AnnotationListener> listeners, AnnotationEvent event) {
		for (AnnotationListener listener : listeners) {
			listener.update(event);
		}
	}
	
}
