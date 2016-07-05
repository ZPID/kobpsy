package org.zpid.se4ojs.app;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.zpid.se4ojs.annotation.JsonAnnotationHandler;
import org.zpid.se4ojs.annotation.ncbo.NcboAnnotator;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

public class JsonFileVisitor extends SimpleFileVisitor<Path> {

	private static final String ontologies = Config.getNcboOntologiesAsString();
	private final String xmlDirectory;
	private final String outputDirectory;
	private static final Logger logger = LogManager.getLogger();
	private static final SE4OJSAccessHelper helper = new SE4OJSAccessHelper();
	
	public JsonFileVisitor(String xmlDirectory, String outputDirectory) {
		this.xmlDirectory = xmlDirectory;
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Visits each json file on the jsonPath and looks for the corresponding xml file.
	 * If no xml file is found, the visitor continues with the next file and logs the error.
	 */
	@Override
	public FileVisitResult visitFile(Path jsonPath, BasicFileAttributes attrs) {
		String jsonFileName = jsonPath.getFileName().toString();
		if (jsonFileName.endsWith(".json")) {
			//look for corresponding xml file
			String xmlFileName = jsonFileName.replace("-ncboAnnotations.json", ".xml");
			Path xmlFilePath = Paths.get(xmlDirectory, xmlFileName);
			if (!Files.exists(xmlFilePath, LinkOption.NOFOLLOW_LINKS)) {
				logger.error("No json annotation possible for: " + jsonPath.toString()
						+ "No corresponding xml file exists");
				return FileVisitResult.CONTINUE;
			}
			//TODO perform the jats xml if the option has been set. (add the option)
			
			//perform the text-structure processing
			List<BOStructureElement> structureElements = null;
			try {
				structureElements = helper.rdfizeSections(xmlFilePath.toFile(), outputDirectory);
			} catch (IOException | JDOMException e) {
				logger.error("No json annotation possible for: " + jsonPath.toString()
						+ "Unable to produce structure elements" + e.getLocalizedMessage());
				e.printStackTrace();
				return FileVisitResult.CONTINUE;
			}
			
			//perform the ncboAnnotation
			NcboAnnotator ncboAnnotator = getNcboAnnotator();
			JsonAnnotationHandler jsonHandler = new JsonAnnotationHandler(jsonPath.toString(), true);
			ncboAnnotator.addListener(jsonHandler);
			try {
				ncboAnnotator.annotate(Config.getInstitutionUrl(), xmlFilePath.toFile(),
						structureElements, Paths.get(outputDirectory));
			} catch (IOException e) {
				logger.error("No json annotation possible for: " + jsonPath.toString()
						+ "Unable to produce ncbo Annotation");
				e.printStackTrace();
				return FileVisitResult.CONTINUE;
			}
			
		}

		return FileVisitResult.CONTINUE;
	}

	public NcboAnnotator getNcboAnnotator() {
		return new NcboAnnotator(ontologies, true);
	}
	
}