package client;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.Prefix;
import org.zpid.se4ojs.annotation.ncbo.NcboAnnotator;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.app.SE4OJSAccessHelper;
import org.zpid.se4ojs.textStructure.bo.StructureElement;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * <p>
 * This is a kind of integration test for the Open Annotation annotation data model used by se4ojs.
 * </p>
 * <p>
 * The test runs se4ojs tool and stores the results in a temporary file.
 * The results are then compared to a reference annotation (stored in the resources folder of this project). 
 * </p>
 * 
 * @author barth
 *
 */
public class TestOa_Annotation_Model {

	private static final String TEST_ONTOLOGY = "CHEBI";
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testNcboAnnotation() throws ModelRuntimeException, IOException, JDOMException {
		 String inPath = this.getClass().getClassLoader().getResource("ncboAnnotatorTestXml.xml").getFile();
		 inPath = inPath.replaceFirst("^/(.:/)", "$1");
		 File in = new File(inPath);
		 String outputDir = folder.getRoot().toString();
		 // get the structure elements of the input text
		 SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		 List<StructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		 //annotate the file
		 NcboAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_ONTOLOGY);
		 ncboAnnotator.annotate(Config.getBaseURI(), in, structureElements, Paths.get(outputDir));
		 
		 String resPath = Paths.get(folder.getRoot().toString(), "ncboAnnotatorTestXml-ncboAnnotations.rdf").toString();
		BufferedReader br = new BufferedReader(
				 new FileReader(resPath));
		 String refPath = this.getClass().getClassLoader().getResource("ncboAnnotatorReferenceAnnotation.rdf").getFile();
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
				 ++ lineNo;
				 lineRes = br.readLine();
				 if (lineRes != null) {
					 linesRes.put(lineRes.trim(), "");
				 }
				 lineRef = brRef.readLine();
				 if (lineRef != null) {
					 linesRef.put(lineRef.trim(), "");
				 }
			 }
			 if (lineRef == null && lineRes != null) {
				 fail(String.format("Reference annotation is shorter than result annotation.\n"
				 		+ "Current line read in result annotation: %s. Line no: %d", lineRes, lineNo));
			 }
		 }
		 if (lineRef != null) {
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

class TestableNCBOAnnotator extends NcboAnnotator {


	private static final int DAY = 30;
	private static final int MONTH = 5;
	private static final int YEAR = 2015;

	public TestableNCBOAnnotator(String ontologies) {
		super(ontologies);
		setAnnotationUtils(new TestableAnnotationUtils());
	}

	@Override
	protected void createDateTriple(org.ontoware.rdf2go.model.Model model,
			String annotationId) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		getAnnotationUtils().createLiteralTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_CREATION_DATE),
				dateFormat.format(getFixedSampleDate()),XSD.date, model);
	}

	private Date getFixedSampleDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(YEAR, MONTH, DAY);
		return cal.getTime();
	}
	
}

class TestableAnnotationUtils extends AnnotationUtils {

	static int uuidCounter = 0;
	
	@Override
	public String generateUuidUri() {
		return String.format("urn:uuid:test%d", ++uuidCounter);
	}
	
}
