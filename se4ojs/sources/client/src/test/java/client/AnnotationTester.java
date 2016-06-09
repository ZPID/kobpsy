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

	protected void compareTransformationResults(TemporaryFolder folder, String resName, String refName) throws FileNotFoundException, IOException {
		 String resPath = Paths.get(folder.getRoot().toString(), resName).toString();
		 BufferedReader br = new BufferedReader(
				 new FileReader(resPath));
		 String refPath = this.getClass().getClassLoader().getResource(refName).getFile();
		 refPath = refPath.replaceFirst("^/(.:/)", "$1");
		 
		 BufferedReader brRef = new BufferedReader(
				 new FileReader(refPath));
		 Map<String, String> linesRes = new HashMap<>();
		 Map<String, String> linesRef = new HashMap<>();
		 String lineRes = "";
		 String lineRef = "";
		 int lineNo = 0;
		 while (lineRes != null) {
			 while (lineRef != null) {
				 System.out.println("\n");
				 ++ lineNo;
				 lineRes = br.readLine();
				 if (!StringUtils.isEmpty(lineRes)) {
					 System.out.println("lineRes");
					 System.out.println(lineRes.trim());
					 if (lineRes.contains("xmlns")) {
						 lineRes = lineRes.replace(">", "");
					 }
					 linesRes.put(lineRes.trim(), "");
				 } else {
					 System.out.println("lineRes is null");
				 }
				 lineRef = brRef.readLine();
				 if (!StringUtils.isEmpty(lineRef)) {
					 System.out.println("lineRef");
					 System.out.println(lineRef.trim());
					 if (lineRef.contains("xmlns")) {
						 lineRef = lineRef.replace(">", "");
					 }
					 linesRef.put(lineRef.trim(), "");
				 } else {
					 System.out.println("lineRef is null");
				 }
			 }
			 if (StringUtils.isEmpty(lineRef) && !StringUtils.isEmpty(lineRes)) {
				 fail(String.format("Reference annotation is shorter than result annotation.\n"
				 		+ "Current line read in result annotation: %s. Line no: %d", lineRes, lineNo));
			 }
		 }
		 if (!StringUtils.isEmpty(lineRef)) {
			 fail(String.format("Reference annotation is longer than result annotation.\n"
					 + "Current line read in reference annotation: %s. Line no: %d", lineRef, lineNo));
		 }
		 for (String lres : linesRes.keySet()) {
			 System.out.println(lres);
			 assertTrue(String.format("%s not found in reference annotation", lres), linesRef.containsKey(lres));
		 }
		 for (String lref : linesRef.keySet()) {
			 System.out.println(lref);
			 assertTrue(String.format("%s not found in result annotation", lref), linesRes.containsKey(lref));
		 }
		 br.close();
		 brRef.close();
		 
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
