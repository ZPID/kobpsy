/**
 * 
 */
package org.zpid.se4ojs.textStructure.bo;
import java.util.ArrayList;
import java.util.List;
/**
 * @author barth
 *
 */
public class BOParagraph extends StructureElement {

	private String text;
//	private List<String> figures = new ArrayList<>();
//	private List<String> tables = new ArrayList<>();
	private List<BOCitation> citations = new ArrayList<>();
//	private boolean quote;
	
	public BOParagraph(int paraCount, String text, String language) {
		super("para_" + paraCount, language);
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public List<BOCitation> getCitations() {
		return citations;
	}
	public void setCitationsf(List<BOCitation> citations) {
		this.citations = citations;
	}
	public void addCitation(BOCitation citation) {
		citations.add(citation);
	}
	
}
