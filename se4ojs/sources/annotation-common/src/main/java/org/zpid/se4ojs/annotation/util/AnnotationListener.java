/**
 * 
 */
package org.zpid.se4ojs.annotation.util;


/**
 * @author barth
 *
 */
public interface AnnotationListener {

	public void handlePaperAnnotationFinished();

	public void update(AnnotationEvent event);
}
