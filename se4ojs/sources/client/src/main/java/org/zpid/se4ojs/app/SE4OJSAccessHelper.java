package org.zpid.se4ojs.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;
import org.zpid.se4ojs.annotation.ncbo.NcboAnnotator;
import org.zpid.se4ojs.refStructuring.ReferenceStructurer;
import org.zpid.se4ojs.spar.Jats2Spar;
import org.zpid.se4ojs.textStructure.StructureTransformer;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;
/**
 * Helper class for the rdfization of PsychOpen articles.
 * 
 * @author barth
 *
 */
public class SE4OJSAccessHelper {

	private Logger logger = LogManager.getLogger(SE4OJSAccessHelper.class);

	private Path directory;
	private final Object ncboLock = new Object();

	//The system file separator. 
	static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/** Constant to specify that the UMLS annotator should be used. */
	private static final String UMLS = "umls";
	/** Constant to specify that the UMLS annotator should be used. */
	private static final String NCBO = "ncbo";

//FIXME needed?	private static final String TEXT_PROPERTY = ClassesAndProperties.CNT_PROP_CONTENT.getURLValue();

	/**
	 * Preprocesses the bibliographic references of an XML input file. If they are not structured,
	 * i.e. if items such as author name, title of the work cited etc. are not encoded with the 
	 * appropriate JATs-xml tags, but are free, unstructured text, the attempt is made to encode them.
	 * The result is stored in a separate file to prevent the original input from being manipulated.
	 */
	public Path structureReferences(Path paperPath, Path outputDir) {
		Path outPaper = null;
		try{
			directory = Files.createDirectories(outputDir);
			outPaper = Paths.get(directory.toString(), paperPath.getFileName().toString());
			File inPaper = paperPath.toFile();
			logger.info("=== Structuring References of file: " + paperPath.toString() + "===");
			ReferenceStructurer refStructurer = 
					new ReferenceStructurer(inPaper, outPaper.toFile(), Config.getInstitutionUrl());
			refStructurer.extractReferences();
		} catch (IOException e) {
			String msgDirConstrError = "Unable to create directory for papers with structured references";
			logger.error(msgDirConstrError);
			throw new RuntimeException(msgDirConstrError + e.getMessage());
		}
		return outPaper;
	}

	/**
	 * <p>
	 * RDFizes the article metadata of a JATS-file.
	 * </p>
	 * <p>
	 * Prior to conversion, the Jats2Spar converter checks
	 * the article language. If the language is supported, i.e. specified in the configuration properties,
	 * the article will be processed and this method returns true. 
	 * </p>
	 * 
	 * @param paper File to rdfize
	 * @param outputDir Output dir for generated RDF
	 * @return true, if the article language is supported, false otherwise
	 * 
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws SAXException 
	 * @throws URISyntaxException 
	 */
	public boolean rdfizeFile(File paper, String outputDir) throws
	        JAXBException, FileNotFoundException, UnsupportedEncodingException, SAXException, URISyntaxException {
		logger.info("===RDFize " + paper.getName() + "===");
		Jats2Spar jats2Spar = new Jats2Spar();
		String out = paper.toPath().getFileName().toString().replace(".xml", ".rdf");
		out = out.replace(".XML", ".rdf");
		File outputFile = Paths.get(outputDir, out).toFile();
		return jats2Spar.transform(paper, outputFile, Config.getInstitutionUrl(), Config.getLanguages());
//		FIXME return handler.createRDFFromXML(paper, Paths.get(outputDir, paper.getName()).toString());
	}
	
	/**
	 * Annotates a file with NCBOAnnotator.
	 * 
	 * @param topLevelElements the structure elements extracted by the section rdfization process
	 * @param outRDF File to be annotated
	 * @param outputDir Output dir for the annotated RDF (output located at /AO_annotations subfolder)
	 * @param pmc Article (file) id
	 * @throws IOException 
	 * @throws Exception
	 */
	public void annotateFileWithNCBOAnnotator(File paper, List<BOStructureElement> topLevelElements, String outputDir) throws IOException {
		    synchronized(ncboLock) { 
				logger.info("Starting annotation with NCBO Annotator for paper: " + paper);
				String ontologyProperties = getOntologyProperties(NCBO);
				logger.info("Ontologies used for annotation: \n\t" + ontologyProperties);
				NcboAnnotator ncboAnnotator = new NcboAnnotator(ontologyProperties);
//				ncboAnnotator.annotateWithApaClusters(paper, topLevelElements, outputDir);
				ncboAnnotator.annotate(Config.getInstitutionUrl(), paper, topLevelElements, Paths.get(outputDir));
		    } 
		}

	public List<BOStructureElement> rdfizeSections(File paper, String outputDir) throws JDOMException, IOException {
		StructureTransformer structureTransformer = new StructureTransformer(Config.getInstitutionUrl(), Config.getLanguages());
		String out = paper.toPath().getFileName().toString().replace(".xml", "-textStructure.rdf");
		out = out.replace(".XML", "-textStructure.rdf");
		return structureTransformer.transform(paper, Paths.get(outputDir, out));
	}

	/* Currently unsupported. 
	
	    synchronized(umlsLock) { 
			logger.info("Starting annotation with UMLS Annotator for paper: " + paper);
			String ontologyProperties = getOntologyProperties(UMLS);
			logger.info("Ontologies used for annotation: \n\t" + ontologyProperties);
			UmlsAnnotator umlsAnnotator = new UmlsAnnotator(ontologyProperties);
		    umlsAnnotator.annotate(Config.getBaseURI(), paper, topLevelElements, outputDir);
	    }
	} */

	protected String getOntologyProperties(String annotator) {
		
		String ontos = null;
		if (annotator.equals(UMLS)) {
			ontos = Config.getUmlsOntologiesAsString();
		} else if (annotator.equals(NCBO)) {
			ontos = Config.getNcboOntologiesAsString();
		}

		if (StringUtils.isEmpty(ontos)) {
			logger.error("Unable to annotate files. No ontologies have been specified."
					+ "Please check properties file:"
					+ " config.properties");
			throw new RuntimeException(
					"Unable to annotate files. No ontologies have been specified."
							+ "Please check properties file:"
							+ " config.properties");
		}
		return ontos;
	}
}
