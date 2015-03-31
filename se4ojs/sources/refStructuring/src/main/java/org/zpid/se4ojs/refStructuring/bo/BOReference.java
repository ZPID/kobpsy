package org.zpid.se4ojs.refStructuring.bo;

import java.util.ArrayList;
import java.util.List;

import org.zpid.se4ojs.refStructuring.MetaDataException;
import org.zpid.se4ojs.refStructuring.ReferenceType;


/**
 * <p>
 * Stores information to generate triples related to citations / references.
 * </p>
 * @author barth
 *
 */
public class BOReference {

	private String articleId;
	private String internalReferenceId;
	private String publicationLink;
	private String sourceTitle;
	private String sourceId;
	private String articleTitle;
	private String sectionTitle;
	private String sectionId;
	private String year;
	private String publisherName;
	private String publisherLocation;
	private String numPages;
	private String month;
	private String volume;
	private String issueNumber;
	private String pageStart;
	private String pageEnd;
	private String confName;
	private String confDate;
	private String confLocation;
	private String edition;
	private String comment;
	private String dateInCitation;
	private String accessDate;
	private String pageCount;
	private String transTitle;
	private String articleIdDoi;
	private String articleIdPm;
	private String articleIdOther;
	private List<String> editors = new ArrayList<String>();
	private List<String> translators = new ArrayList<String>();
	private List<String> authors = new ArrayList<String>();
	private List<String> authorsEditorsAndTranslators = new ArrayList<String>();
	private List<String> collabAuthors = new ArrayList<String>();
	private ReferenceType referenceType;
	private String rest;


	public String getArticleId() {
		return articleId;
	}

	public void setArticleId(String articleId) {
		if (articleId == null) {
			throw new MetaDataException("The article ID of a reference must not be null");
		}
		this.articleId = articleId;
	}

//	public ArticleIdType getArticleIdType() {
//		return articleIdType;
//	}
//
//	public void setArticleIdType(ArticleIdType articleIdType) {
//		this.articleIdType = articleIdType;
//	}

	public void setPublicationLink(String doiRdfUri) {
		this.publicationLink = doiRdfUri;
	}

	public String getPublicationLink() {
		return publicationLink;
	}

	public void setSourceTitleAndSourceId(String sourceTitle) {
		this.sourceTitle = sourceTitle;
		this.sourceId = sourceTitle.replaceAll(" ", "");
	}

	public String getSourceTitle() {
		return sourceTitle;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}

	public String getArticleTitle() {
		return articleTitle;
	}

	public void setSectionTitleAndId(String sectionTitle) {
		this.sectionTitle = sectionTitle;
		this.sectionId = sectionTitle.replaceAll(" ", "");
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getYear() {
		return year;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherLocation(String publisherLocation) {
		this.publisherLocation = publisherLocation;
	}

	public String getPublisherLocation() {
		return publisherLocation;
	}

	public void setNumPages(String numPages) {
		this.numPages = numPages;
	}

	public String getNumPages() {
		return numPages;
	}

//FIXME	public void setReferenceType(ReferenceType referenceType) {
//		this.referenceType = referenceType;
//	}
//
//	public ReferenceType getReferenceType() {
//		return referenceType;
//	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getVolume() {
		return volume;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getMonth() {
		return month;
	}

	public void setIssueNumber(String issueNo) {
		this.issueNumber = issueNo;
	}

	public String getIssueNumber() {
		return issueNumber;
	}

	public String getInternalReferenceId() {
		return internalReferenceId;
	}

	public void setInternalReferenceId(String internalReferenceId) {
		this.internalReferenceId = internalReferenceId;
	}

	public void setPageStart(String pageStart) {
		this.pageStart = pageStart;
	}

	public String getPageStart() {
		return pageStart;
	}

	public void setPageEnd(String pageEnd) {
		this.pageEnd = pageEnd;
	}

	public String getPageEnd() {
		return pageEnd;
	}

	public void setConfName(String confName) {
		this.confName = confName;
	}

	public String getConfName() {
		return confName;
	}

	public void setConfDate(String confDate) {
		this.confDate = confDate;
	}

	public String getConfDate() {
		return confDate;
	}

	public void setConfLocation(String confLocation) {
		this.confLocation = confLocation;
	}

	public String getConfLocation() {
		return confLocation;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getEdition() {
		return edition;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setDateInCitation(String dateInCitation) {
		this.dateInCitation = dateInCitation;
	}

	public String getDateInCitation() {
		return dateInCitation;
	}

	public void setAccessDate(String accessDate) {
		this.accessDate = accessDate;
	}

	public String getAccessDate() {
		return accessDate;
	}

	public void setPageCount(String pageCount) {
		this.pageCount = pageCount;
	}

	public String getPageCount() {
		return pageCount;
	}

	public void setTransTitle(String transTitle) {
		this.transTitle = transTitle;
	}

	public String getTransTitle() {
		return transTitle;
	}

	public void setArticleIdDoi(String id) {
		this.articleIdDoi = id;
	}

	public String getArticleIdDoi() {
		return articleIdDoi;
	}

	public void setArticleIdPm(String id) {
		this.articleIdPm = id;
	}

	public String getArticleIdPm() {
		return articleIdPm;
	}

	public void setArticleIdOther(String id) {
		this.articleIdOther = id;
	}

	public String getArticleIdOther() {
		return articleIdOther;
	}

	public List<String> getEditors() {
		return editors;
	}

	public void setEditors(List<String> editors) {
		this.editors = editors;
	}

	public List<String> getTranslators() {
		return translators;
	}

	public void setTranslators(List<String> translators) {
		this.translators = translators;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public List<String> getCollabAuthors() {
		return collabAuthors;
	}

	public void setCollabAuthors(List<String> collabAuthors) {
		this.collabAuthors = collabAuthors;
	}

	public List<String> getAuthorsEditorsAndTranslators() {
		return authorsEditorsAndTranslators;
	}
	
	public void setAuthorsEditorsAndTranslators(
			List<String> authorsEditorsAndTranslators) {
		this.authorsEditorsAndTranslators = authorsEditorsAndTranslators;
	}

	/**
	 * Returns false if fundamental information of the reference is not present.
	 * 
	 * @return false if fundamental information of the reference is not present.
	 */
	public boolean checkReferenceProcessed() {
		if (getAuthors().isEmpty() || (getSourceTitle() == null && getSectionTitle() == null || getReferenceType() == null)) {
			return false;
		}
		return true;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public void setReferenceType(ReferenceType referenceType) {
		this.referenceType = referenceType;
	}

	public void setRest(String restString) {
		this.rest = restString;
	}

	public String getRest() {
		return rest;
	}

}
