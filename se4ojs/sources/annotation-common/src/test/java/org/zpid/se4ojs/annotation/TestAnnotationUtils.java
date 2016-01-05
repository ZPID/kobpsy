package org.zpid.se4ojs.annotation;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TestAnnotationUtils {

	private AnnotationUtils annotationUtils;
	
	@Before
	public void beforeTest() {
		annotationUtils = new AnnotationUtils();
	}
	
	@Test
	public void testUrlEncode_MoreThanOneFragmentID() {
		String s = "http:/#www.zpid.de/resource/doi#10.5964#ejop.v8i4.483";
		String expected = "http:/_www.zpid.de/resource/doi_10.5964#ejop.v8i4.483";
		assertEquals("An unexpected substitution has been performed", expected, annotationUtils.urlEncode(s));
	}
	
	@Test
	public void testUrlEncode_OneFragmentID() {
		String s = "http://www.zpid.de/resource/doi/10.5964#ejop.v8i4.483";
		String expected = "http://www.zpid.de/resource/doi/10.5964#ejop.v8i4.483";
		assertEquals("An unexpected substitution has been performed", expected, annotationUtils.urlEncode(s));
	}
	
	@Test
	public void testUrlEncode_CharsNotAllowed() {
		String s = "http://éwww.zpid.de/ resource/doi@10.5964#ejop.v8i4.483";
		String expected = "http://_www.zpid.de/_resource/doi_10.5964#ejop.v8i4.483";
		assertEquals("An unexpected substitution has been performed", expected, annotationUtils.urlEncode(s));
	}
	
	@Test
	public void testCreateUriString() {
		String s = "http://éwww.zpid.de/ resource/doi@10.5964#ejop.v8i4.483";
		String expected = "http://_www.zpid.de/_resource/doi_10.5964#ejop.v8i4.483";
		assertEquals("An unexpected substitution has been performed", expected, annotationUtils.urlEncode(s));
	}
}
