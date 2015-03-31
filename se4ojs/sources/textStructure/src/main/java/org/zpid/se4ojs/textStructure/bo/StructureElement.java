package org.zpid.se4ojs.textStructure.bo;

/**
 * Parent class for elements of an article that contain text.
 * 
 * @author barth
 *
 */
public class StructureElement {
	
	public static String ARTICLE_LANGUAGE = "articleLanguage";
	private String uriTitle;
	private String language;

	protected StructureElement(String uriTitle, String language) {
		this.uriTitle = uriTitle;
		if (language != null && !language.equals(ARTICLE_LANGUAGE)) {
			this.language = language.toLowerCase();
		}
	}

	public String getUriTitle() {
		return uriTitle;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
}
