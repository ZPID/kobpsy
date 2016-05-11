/**
 * 
 */
package org.zpid.se4ojs.spar;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author barth
 *
 */
public class TestJats2Spar {

	@Test
	public void testCheckArticleLanguage_Null() {
		Jats2Spar jats2Spar = new Jats2Spar();
		assertTrue("The check should pass if no languages are defined", 
				jats2Spar.checkArticleLanguage(Mockito.mock(Document.class), null));
		
	}
	
	@Test
	public void testCheckArticleLanguage_Empty() {
		Jats2Spar jats2Spar = new Jats2Spar();
		assertTrue("The check should pass if no languages are defined", 
				jats2Spar.checkArticleLanguage(Mockito.mock(Document.class), StringUtils.EMPTY));
		
	}
	
	@Test
	public void testCheckArticleLanguage_Unsupported() {
		Jats2Spar jats2Spar = new Jats2Spar();
		String languages = "fr,es";
		
		Document document = Mockito.mock(Document.class);
		NodeList nodeList = Mockito.mock(NodeList.class);
		Element article = Mockito.mock(Element.class);
		Attr lgAttr = Mockito.mock(Attr.class);
		Mockito.when(document.getElementsByTagName(Jats2Spar.TAG_ARTICLE)).thenReturn(nodeList);
		Mockito.when(nodeList.item(0)).thenReturn(article);
		Mockito.when(article.getAttributeNode(Jats2Spar.ATTR_LANGUAGE)).thenReturn(lgAttr);
		Mockito.when(lgAttr.getValue()).thenReturn("en");
		assertFalse("Language English is not allowed", jats2Spar.checkArticleLanguage(document, languages));
	}
	
	@Test
	public void testCheckArticleLanguage_Supported() {
		Jats2Spar jats2Spar = new Jats2Spar();
		String languages = "en";
		
		Document document = Mockito.mock(Document.class);
		NodeList nodeList = Mockito.mock(NodeList.class);
		Element article = Mockito.mock(Element.class);
		Attr lgAttr = Mockito.mock(Attr.class);
		Mockito.when(document.getElementsByTagName(Jats2Spar.TAG_ARTICLE)).thenReturn(nodeList);
		Mockito.when(nodeList.item(0)).thenReturn(article);
		Mockito.when(article.getAttributeNode(Jats2Spar.ATTR_LANGUAGE)).thenReturn(lgAttr);
		Mockito.when(lgAttr.getValue()).thenReturn("en");
		assertTrue("Language English should be allowed", jats2Spar.checkArticleLanguage(document, languages));
	}

}
