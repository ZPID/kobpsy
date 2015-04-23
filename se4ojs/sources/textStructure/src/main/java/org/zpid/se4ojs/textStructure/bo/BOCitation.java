/**
 * 
 */
package org.zpid.se4ojs.textStructure.bo;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author barth
 *
 */
public class BOCitation {

	private List<String> rids;
	
	private String text;
	
	private String startPage;

	private String endPage;

	public List<String> getRids() {
		return rids;
	}

	public void setRids(List<String> rids) {
		this.rids = rids;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	public void setEndPage(String endPage) {
		this.endPage = endPage;
	}

	public String getEndPage() {
		return endPage;
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
		BOCitation rhs = (BOCitation) obj;
		return new EqualsBuilder()
				.append(rids, rhs.rids)
				.append(text, rhs.text)
				.append(startPage, rhs.startPage)
				.append(endPage, rhs.endPage).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(rids)
				.append(text).append(startPage)
				.append(endPage).toHashCode();
	}
	
}
