package org.zpid.se4ojs.textStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Model;
import org.xml.sax.InputSource;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

/**
 * Rdfizes sections and citations of a JATS-1.0 document.
 * 
 * @author barth
 */
public class StructureTransformer {

	private List<BOStructureElement> topLevelElements = new ArrayList<BOStructureElement>();
	
	int idx = 0;

	private Logger log = LogManager.getLogger(StructureTransformer.class);

	private String baseUri;

	private String languages;

	public StructureTransformer(String baseURI, String inputLanguages) {
		this.baseUri = baseURI;
		this.languages = inputLanguages;
	}

	public static void main(String[] args) throws JDOMException, IOException {
//		String RESOURCE_TEST_PAPER = "/testArticleShort.xml";
		String RESOURCE_TEST_PAPER = "/psychOpen_testArticle.xml";
		File datafile = new File(StructureTransformer.class.getResource(RESOURCE_TEST_PAPER).getFile());
		StructureTransformer transformer = new StructureTransformer("dummyUri", "en,de");
		transformer.transform(datafile, Paths.get("D:\\Temp\\db\\sectionOut.rdf"));
	}

	public  List<BOStructureElement> transform(File dataFile, Path outFile) throws JDOMException, IOException {
		log.info("Starting text structure rdfization for paper: " + dataFile.getName());
		SAXBuilder builder = new SAXBuilder();
		builder.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false);
	    builder.setFeature("http://xml.org/sax/features/validation", false);
	    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		InputSource is;
		is = new InputSource(dataFile.getAbsolutePath());
		is.setEncoding("UTF-8");
		Document document = (Document) builder.build(is);
		Element rootNode = document.getRootElement();
		StructureParser sectionParser = new StructureParser(languages);
		sectionParser.parse(rootNode, topLevelElements);
		StructureRdfizer structureRdfizer = new StructureRdfizer(baseUri, topLevelElements,
				document, sectionParser.getInternalCitationCounts());
		Model model = structureRdfizer.rdfize();
	    FileOutputStream os = new FileOutputStream(outFile.toFile());
	    try {
			model.writeTo(os);
		} catch (ModelRuntimeException e) {
			throw new FileNotFoundException("ModelRuntimeException " + e.getMessage());
		} catch (IOException e) {
			throw new FileNotFoundException("IOException " + e.getMessage());
		}
	    model.close();
 		os.close();
		log.info("Finished text structure rdfization for paper: " + dataFile.getName());
		return topLevelElements;
	}


}
