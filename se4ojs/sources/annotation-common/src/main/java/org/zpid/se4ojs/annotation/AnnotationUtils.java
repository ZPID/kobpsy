package org.zpid.se4ojs.annotation;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

import com.hp.hpl.jena.rdf.model.Resource;


public class AnnotationUtils {
	
	static final String NAMESPACE_ZPID = "zpid";
	static final String URI_PREFIX_ZPID = "http://zpid.de";
	static final String URI_INFIX_DOI = "doi";
	static final String URI_SUFFIX_TEXTUAL_ENTITY = "textual-entity";
	public static final String CHAR_NOT_ALLOWED = "[^A-Za-z0-9]";
	
	private static Logger log = Logger.getLogger(AnnotationUtils.class);
	
	public static void createLiteralTriple(String subject,
			String predicate, String object, Model model) {
		model.addStatement(
				model.createURI(subject),
				model.createURI(predicate),
		        model.createPlainLiteral(object));
	}
	
	/**
	 * Creates a typed literal.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param resource the datatype
	 * @param model
	 */
	public static void createLiteralTriple(String subject,
			String predicate, String object,
			Resource resource, Model model) {
		model.addStatement(
				model.createURI(subject),
				model.createURI(predicate),
		        model.createDatatypeLiteral(object, model.createURI(resource.toString())));
	}	
	public static String createPropertyString(Prefix p, String pred) {
		return
				new StringBuilder(p.getURL()).append(pred).toString();
	}
	
	public static String createUriString(String... uriParts) {
		StringBuilder sb = new StringBuilder();
		for (String part : uriParts) {
			sb.append(part);
			if(!part.endsWith("/")){
				sb.append("/");
			}
		}
		if (sb.lastIndexOf("/") == sb.length() - 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public static void setNamespaces(Model model) {
		for (Prefix p : Prefix.values()) {
			model.setNamespace(p.getNS(), p.getURL());
		}
	}
	
	static String getArticleUri(Document document, String baseUri) {
		Element rootElement = document.getRootElement();
		List<Element> articleIds = rootElement.getChild("front").getChild("article-meta").getChildren("article-id");
		for (Element articleId : articleIds) {
			if (articleId.getAttributeValue("pub-id-type").equals("doi")) {
				return createUriString(baseUri, URI_INFIX_DOI, articleId.getText()).toString();
			}
		}
		log.error("Unable to extract article URI.");
		return null;
	}
	
	static String createSubElementUri(StructureElement se, String articleUri, String parentUri) {
		if (se instanceof BOSection) {
			return new StringBuffer(articleUri).append("/").append(se.getUriTitle()).toString();
		} else if (se instanceof BOParagraph) {
			return createParagraphFullUriTitle(parentUri, (BOParagraph) se);
		}
		return null;
	}
	
	private static String createParagraphFullUriTitle(String parentUri,
			BOParagraph p) {
		return new StringBuilder(parentUri).append("_").append(p.getUriTitle()).toString();
	}
	
	static String urlEncode(String s) {
		String uri = s.replaceAll(CHAR_NOT_ALLOWED, "-");
		return uri.replaceAll("[-]+", "-");
	}
	
	public static void createResourceTriple(String subject,
			String predicate, String object, Model model) {
		model.addStatement(model.createURI(subject), model.createURI(predicate), model.createURI(object));
	}

}
