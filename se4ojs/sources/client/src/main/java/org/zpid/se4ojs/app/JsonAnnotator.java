/**
 * 
 */
package org.zpid.se4ojs.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author barth 
 * 
 * For each jsonFile found in the jsonInputDirectory, the
 *         corresponding xmlFile in the xmlDirectory is looked up. Then the
 *         text-structure rdf is processed and output to the output directory.
 *         After this, the Json handler generates the ncbo-annotation.rdf and
 *         outputs it to the output directory.
 * 
 * @param args
 *            - xmlDirectory jsonDirectory outputDirectory (optional)
 *            jatsProcessingTask
 * @throws IOException
 */
public class JsonAnnotator {

	
	/**
	 * For each jsonFile found in the jsonInputDirectory,
	 * the corresponding xmlFile in the xmlDirectory is looked up.
	 * Then the text-structure rdf is processed and output to the output directory.
	 * After this, the Json handler generates the ncbo-annotation.rdf and outputs it to the output directory.
	 *  
	 * @param args - xmlDirectory jsonDirectory outputDirectory (optional) jatsProcessingTask
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		String xmlDirectory = args[0];
		String jsonDirectory = args[1];
		String outputDirectory = args[2];
		//TODO jatsProcessingTask
		JsonFileVisitor jsonVisitor = new JsonFileVisitor(xmlDirectory, outputDirectory);
		Files.walkFileTree(Paths.get(jsonDirectory), 
				jsonVisitor);

	}
}

