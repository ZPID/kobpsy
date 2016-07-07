package client;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TemporaryFolder;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class AnnotationTester {

	protected void compareTransformationResults(String resName, String refName) throws FileNotFoundException, IOException {
		doCompare(resName, refName, false, false);
	}

	protected void compareTransformationResults(TemporaryFolder folder, String resName, String refName) throws FileNotFoundException, IOException {
		 String resPath = Paths.get(folder.getRoot().toString(), resName).toString();
		 String refPath = this.getClass().getClassLoader().getResource(refName).getFile();
		 refPath = refPath.replaceFirst("^/(.:/)", "$1");

		 doCompare(resPath, refPath, true, true);
	}
	protected void doCompare(String resPath, String refPath, boolean compareModels, boolean failOnNotMatching) throws FileNotFoundException, IOException {
		 BufferedReader br = new BufferedReader(
				 new FileReader(resPath));
		 BufferedReader brRef = new BufferedReader(
				 new FileReader(refPath));
		 Map<String, String> linesRes = new HashMap<>();
		 Map<String, String> linesRef = new HashMap<>();
		 String lineRes = "";
		 String lineRef = "";
		 while (lineRes != null) {
				 lineRes = prepareLine(br, linesRes, "linesRes");
		 }
		 while (lineRef != null) {
			 lineRef = prepareLine(brRef, linesRef, "linesRef");
		 }

		 findMatchingLine(linesRes, linesRef, "result annotation", failOnNotMatching);
		 findMatchingLine(linesRef, linesRes, "reference annotation", failOnNotMatching);

		 int linesRefSize = linesRef.keySet().size();
		 int linesResSize = linesRes.keySet().size();
		 if (linesRefSize != linesResSize) {
			 if (linesRefSize > linesResSize) {
				 fail(String.format("Reference annotation is longer than result annotation.\n"));
			 } else {
				 fail(String.format("Result annotation is longer than reference annotation.\n"));
			 }
		 }
		 br.close();
		 brRef.close();
		 if (compareModels) {
	        try {
	            Model m1 = ModelFactory.createDefaultModel();
	            Model m2 = ModelFactory.createDefaultModel();

	            read(m1, refPath, "RDF/XML");
	            read(m2, resPath, "RDF/XML");

	            if (m1.isIsomorphicWith(m2)) {
	                System.out.println("models are equal");
	                System.out.println();
	            } else {
	                fail("models are unequal");
	                System.out.println();
	            }
	        } catch (Exception e) {
	            System.err.println("    " + e.toString());
	            fail("Unhandled exception:");
	        }
		 }
	}

	protected void findMatchingLine(Map<String, String> linesRes,
			Map<String, String> linesRef, String name, boolean failOnNotMatching) {
		for (String lref : linesRef.keySet()) {
			 //skip uuids
			 if (!lref.contains("urn:uuid")) {
				 if (failOnNotMatching) {
					 assertTrue(String.format("%s not found in " + name, lref), linesRes.containsKey(lref));
				 } else {
					 if (!linesRes.containsKey(lref)) {
						 System.out.println(String.format("Not found in " + name + ":") + lref );
					 }
				 }
			 }
		 }
	}

	protected String prepareLine(BufferedReader br, Map<String, String> lines, String brName)
			throws IOException {
		String line;
		line = br.readLine();
		 if (!StringUtils.isEmpty(line)) {
			 System.out.println(brName);
			 System.out.println(line.trim());
			 if (line.contains("xmlns")) {
				 line = line.replace(">", "");
			 }
			 //replace dates by a default date
			 if (line.contains("<oa:annotatedAt rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">)")) {
				 line = "<oa:annotatedAt rdf:datatype=\"http://www.w3.org/2001/XMLSchema#date\">2016-07-05</oa:annotatedAt>";
			 }
			 lines.put(line.trim(), "");
		 } else {
			 System.out.println("lineRes is null");
		 }
		return line;
	}

	static void read(Model model, String in, String lang)
			throws java.io.FileNotFoundException {
		try {
			new URL(in);
			model.read(in, lang);
		} catch (java.net.MalformedURLException e) {
			model.read(new FileInputStream(in), "", lang);
		}
	}


}
