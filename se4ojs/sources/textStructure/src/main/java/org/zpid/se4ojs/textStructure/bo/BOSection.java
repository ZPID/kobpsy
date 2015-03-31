package org.zpid.se4ojs.textStructure.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.zpid.se4ojs.textStructure.SectionType;

public class BOSection extends StructureElement {
	public static final int ARBITRARY_TITLE_LENGTH = 30;
	public static final String CHAR_NOT_ALLOWED = "[^A-Za-z0-9]";
	private List<SectionType> types = new ArrayList<>();

	private List<String> externalLinks = new ArrayList<>();
//	private StringBuilder ancestorUri; FIXME
	private List<String> lists = new ArrayList<>();
	private List<StructureElement> childStructures = new ArrayList<>();
	private String title;

	public BOSection(String uriTitle) {
		this(uriTitle, null);
	}

	public BOSection(String uriTitle, List<SectionType> types) {
		this(null, uriTitle, types, null);
	}
	
	public BOSection(String title, String uriTitle, List<SectionType> types, String language) {
		super(uriTitle, language);
		this.title = title;
		if (types == null) {
			this.types.add(SectionType.OTHER);
		} else {
			this.types = types;			
		}
	}
	
	public static String createUriTitle(String titleTagValue, List<SectionType> types,
			int idx, String parentTitle) {
		String uriTitle = titleTagValue.replaceAll(CHAR_NOT_ALLOWED, "-");
		uriTitle = uriTitle.replaceAll("[-]+", "-");
		if (uriTitle.length() > ARBITRARY_TITLE_LENGTH) {
			uriTitle = uriTitle.substring(0, ARBITRARY_TITLE_LENGTH - 1);
		}
		StringBuilder strBuilder = new StringBuilder();
		if (!StringUtils.isEmpty(parentTitle)) {
			strBuilder.append(parentTitle);
			strBuilder.append("_");
		}
		if (!StringUtils.isEmpty(uriTitle)) {
			return strBuilder.append(uriTitle).toString();
		}
		if (!types.isEmpty() && !types.get(0).equals(SectionType.OTHER)) {
			return strBuilder.append(types.get(0).getLabel()).toString();
		}
		return strBuilder.append("sec_untitled_").append(idx).toString();
	}
	
//	FIXME. Create ancestor uri from parent not from BOSection
//	private StringBuilder createAncestorUri(BOSection sectionBO, StringBuilder titleInUrl) {
//		if (parent != null) {
//			StringBuilder title = new StringBuilder(parent.getTitle()).append("_");
//			titleInUrl.insert(0, title.toString());
//			createAncestorUri(parent, titleInUrl);
//		}
//		return titleInUrl;
//	}	

	public List<String> getExternalLinks() {
		return externalLinks;
	}

	public void setExternalLinks(List<String> extLinks) {
		this.externalLinks = extLinks;
	}

//	FIXME
//	public StringBuilder getAncestorUri() {
//		if (ancestorUri == null) {
//			return createAncestorUri(this, new StringBuilder());
//		}
//		return ancestorUri;
//	}

	public List<String> getLists() {
		return lists;
	}

	public List<SectionType> getTypes() {
		return types;
	}

	public void addChildStructure(StructureElement childStructure) {
		childStructures.add(childStructure);
	}

	public List<StructureElement> getChildStructures() {
		return childStructures;
	}

	public String getTitle() {
		return title;
	}

}
