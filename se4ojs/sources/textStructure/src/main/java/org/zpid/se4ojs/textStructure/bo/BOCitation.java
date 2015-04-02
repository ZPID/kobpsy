/**
 * 
 */
package org.zpid.se4ojs.textStructure.bo;

import java.util.List;

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
	
}
