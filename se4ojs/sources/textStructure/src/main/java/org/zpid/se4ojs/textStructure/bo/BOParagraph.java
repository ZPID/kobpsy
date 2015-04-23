/**
 * 
 */
package org.zpid.se4ojs.textStructure.bo;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BOParagraph rhs = (BOParagraph) obj;
		return new EqualsBuilder()
				.append(text, rhs.text)
				.append(citations, rhs.citations)
				.isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(text)
				.append(text).append(citations).toHashCode();
	}
	
	
}
