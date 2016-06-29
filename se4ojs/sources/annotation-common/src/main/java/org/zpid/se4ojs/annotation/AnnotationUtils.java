package org.zpid.se4ojs.annotation;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.ontoware.rdf2go.model.Model;
import org.zpid.se4ojs.sparql.Prefix;
import org.zpid.se4ojs.textStructure.bo.BOParagraph;
import org.zpid.se4ojs.textStructure.bo.BOSection;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.uuid.JenaUUID;

/**
 * This class contains utility methods to create RDF-triples and URIs.
 * 
 * @author barth
 *
 */
public class AnnotationUtils {
	
	/**
	 * Simple Regular Expression that checks for unwanted characters in URI Strings. Note that
	 * the expression does not treat reserved characters separately and does not check
	 * whether a (subcomponent delimiter) character's position is correct.
	 * The expression is somewhat stricter than the RFC3986, e.g. we do not allow for 
	 * a question mark or for percent-encoded Unicode characters. 
	 */
	public static final String CHARS_NOT_ALLOWED_IN_URI =
			"[^A-Za-z0-9.\\-_~:/#?\\[\\]@!$&'\\(\\)*+,;=)]";
	
	static final String NAMESPACE_ZPID = "zpid";
	static final String URI_PREFIX_ZPID = "http://www.zpid.de";
	static final String URI_INFIX_DOI = "doi";
	static final String URI_SUFFIX_TEXTUAL_ENTITY = "textual-entity";
	static final String URI_INFIX_TEXT_QUOTE_SEL = "textQuoteSel";
	static final String URI_INFIX_FRAGEMENT_SEL = "fragmentSel";
	static final String URI_INFIX_TEXTPOS_SEL = "textPosSel";
	
	private Logger log = Logger.getLogger(AnnotationUtils.class);
	
	public void createLiteralTriple(String subject,
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
	public void createLiteralTriple(String subject,
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
	
	public String createUriString(String... uriParts) {
		StringBuilder sb = new StringBuilder();
		for (String part : uriParts) {
			sb.append(part);
			if(!part.endsWith("/") && !part.endsWith("#")){
				sb.append("/");
			}
		}
		if (sb.lastIndexOf("/") == sb.length() - 1) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}
	
	public void setNamespaces(Model model) {
		for (Prefix p : Prefix.values()) {
			model.setNamespace(p.getNS(), p.getURL());
		}
	}
	
    String getArticleUri(Document document, String baseUri) {
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
	
	String createSubElementUri(BOStructureElement se, String articleUri, String parentUri) {
		if (se instanceof BOSection) {
			return new StringBuffer(articleUri).append("/").append(se.getUriTitle()).toString();
		} else if (se instanceof BOParagraph) {
			return createParagraphFullUriTitle(parentUri, (BOParagraph) se);
		}
		return null;
	}
	
	private String createParagraphFullUriTitle(String parentUri,
			BOParagraph p) {
		return new StringBuilder(parentUri).append("_").append(p.getUriTitle()).toString();
	}
	
	/**
	 * Encodes the passed in string into a valid url.
	 * Characters not allowed in URIs are replaced by underscores.
	 * If more than one fragment identifier (the '#' character) is present,
	 * only the last one is retained and the others are replaced by underscores. 
	 * 
	 * @param s the string to convert to a valid url
	 * @return the converted string
	 */
	String urlEncode(String s) {
		String uri = s.replaceAll(CHARS_NOT_ALLOWED_IN_URI, "_");
		if (StringUtils.lastOrdinalIndexOf(uri, "#", 2) != -1) {
			String fragment = StringUtils.substringAfterLast(uri, "#");
			String base = StringUtils.substringBeforeLast(uri, "#");
			base = base.replaceAll("#", "_");
			uri = new StringBuilder(base).append("#").append(fragment).toString();
		}
		return uri.replaceAll("[_]+", "_");
	}
	
	public void createResourceTriple(String subject,
			String predicate, String object, Model model) {
		model.addStatement(model.createURI(subject), model.createURI(predicate), model.createURI(object));
	}

	public String generateUuidUri() {
		return new StringBuilder("urn:").append(JenaUUID.generate().asURI()).toString();
	}

}
