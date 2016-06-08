package org.zpid.se4ojs.refStructuring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public enum ReferenceType {
	JOURNAL_ARTICLE("journal"),
	BOOK("book"),
	BOOK_SECTION("bookSection"),
	CONFERENCE_PROCS("confproc"), //proceedings
	CONFERENCE_PAPER("confpaper"), //paper in proceedings
	THESIS("thesis"), //both, complete or partial
	GOV("gov"),
	PATENT("patent"),
	STANDARD("standard"),
	DATABASE("database"),
	WEBPAGE("webpage", "web"),
	COMMUN("commun"),
	DISCUSSION("discussion"),
	BLOG("blog"),
	WIKI("wiki"),
	REPORT("report"),
	SOFTWARE("software"),
	OTHER("other");
	
	private static Logger log = LogManager.getLogger(ReferenceType.class);
	private String type;
	private String altTypeLabel;

	private ReferenceType(String type) {
		this(type, null);
	}
	private ReferenceType(String type, String altTypeLabel) {
		this.type = type;
		this.altTypeLabel = altTypeLabel;
	}
	
	public String getType() {
		return type;
	}
	
	/**
	 * Compares the passed in string designating a reference type with the 
	 * predefined {@link ReferenceType}s. The matching {@link ReferenceType} is returned or null,
	 * if none of them match.
	 * 
	 * @param type the string description for a reference type
	 * @return the matching {@link ReferenceType} or null
	 */
	public static ReferenceType getReferenceType(String type) {
		for (ReferenceType refType : ReferenceType.values()) {
			if (type.equals(refType.getType())) {
				return refType;
			}
			if (type.equals(refType.getAltTypeLabel())) {
				return refType;
			}
		}
		log.error("Unrecognized reference type: " + type);
		return null;
	}
	private Object getAltTypeLabel() {
		return altTypeLabel;
	}
}