package client;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.OaAnnotator;
import org.zpid.se4ojs.annotation.ncbo.NcboAnnotator;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.app.JsonFileVisitor;
import org.zpid.se4ojs.app.SE4OJSAccessHelper;
import org.zpid.se4ojs.sparql.Prefix;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

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
public class TestOa_Annotation_Model extends AnnotationTester {

	private static final String TEST_ONTOLOGY = "CHEBI";
	static final String TEST_SEMANTIC_TYPE_ONTOLOGY = "SNOMEDCT";
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	/**
	 * Compares the expected annotation outcome with the actual outcome on the basis of a short document.
	 * 
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void testNcboAnnotation() throws ModelRuntimeException, IOException, JDOMException {
		 String inPath = this.getClass().getClassLoader().getResource("ncboAnnotatorTestXml.xml").getFile();
		 inPath = inPath.replaceFirst("^/(.:/)", "$1");
		 File in = new File(inPath);
		 String outputDir = folder.getRoot().toString();
		 // get the structure elements of the input text
		 SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		 List<BOStructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		 //annotate the file
		 NcboAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_ONTOLOGY);
		 ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));
		 
		 super.compareTransformationResults(folder, 
				 "ncboAnnotatorTestXml-ncboAnnotations.rdf", "ncboAnnotatorReferenceAnnotation.rdf");
	}
	
	/**
	 * Check the semanticType Model.
	 * Two annotation rounds (of two different articles) are performed in order to test whether
	 * - the rdf file is correctly written
	 * - the rdf file can be re-opened and whether the new triples are appended.
	 *@TODO check loading the file into a SPARQL endpoint
	 * 
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void testSemanticTypeAnnotation() throws ModelRuntimeException, IOException, JDOMException {
		String testXmlFileName = "semTypeAnnotatorTest.xml";
		String inPath = this.getClass().getClassLoader().getResource(testXmlFileName).getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		File in = new File(inPath);
		String outputDir = folder.getRoot().toString();
		// get the structure elements of the input text
		SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		List<BOStructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		//annotate the file
		NcboAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_SEMANTIC_TYPE_ONTOLOGY);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));
		
		Path rdfPath = Paths.get(outputDir, testXmlFileName.replace(".xml", "") + "-" + OaAnnotator.SEM_TYPE_RDF_FILENAME);
		assertTrue("The rdf output file for semantic type annotation does not exist", 
				rdfPath.toFile().exists());
//		long rdfFileSizeAfterFirstAnnotation = Files.size(rdfPath);
		String testXmlFileName2 = "semTypeAnnotatorTest2.xml";
		inPath = this.getClass().getClassLoader().getResource(testXmlFileName2).getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		in = new File(inPath);
		//annotate the file
		ncboAnnotator = new TestableNCBOAnnotator(TEST_SEMANTIC_TYPE_ONTOLOGY);
		structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));
		rdfPath = Paths.get(outputDir, testXmlFileName2.replace(".xml", "") + "-" +  OaAnnotator.SEM_TYPE_RDF_FILENAME);
		assertTrue("The rdf output file for semantic type annotation does not exist", 
				rdfPath.toFile().exists());
//		long rdfFileSizeAfterSecondAnnotation = Files.size(rdfPath);
//		assertTrue("The additional triples from the second input file have not been appended",
//				rdfFileSizeAfterFirstAnnotation < rdfFileSizeAfterSecondAnnotation);

	}

	@Test
	public void testJsonHandler() throws ModelRuntimeException, IOException, JDOMException {
		String inPath = this.getClass().getClassLoader().getResource("jsonHandlerTest.xml").getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		File in = new File(inPath);
		String outputDir = folder.getRoot().toString();
		// get the structure elements of the input text
		SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		List<BOStructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		//annotate the file
		NcboAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_SEMANTIC_TYPE_ONTOLOGY);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));
		
		Path jsonPath = Paths.get(outputDir, "jsonHandlerTest-ncboAnnotations.json");
		assertTrue("The json output file  does not exist", 
				jsonPath.toFile().exists());
		//copy the file (otherwise it will be overridden with the current settings)
		String ncboRdfFileName = "jsonHandlerTest-ncboAnnotations.rdf";
		String originalNcboRdfFileName = ncboRdfFileName +"-original";
		Files.copy(Paths.get(outputDir, ncboRdfFileName), Paths.get(outputDir, originalNcboRdfFileName),
				LinkOption.NOFOLLOW_LINKS);
		//read the json content

		String xmlDirectory = Paths.get(inPath).getParent().toString();
		String jsonDirectory = jsonPath.getParent().toString();
		String outputDirectory = outputDir;
		//TODO jatsProcessingTask
		TestableJsonFileVisitor jsonVisitor = new TestableJsonFileVisitor(xmlDirectory, outputDirectory);
		Files.walkFileTree(Paths.get(jsonDirectory), 
				jsonVisitor);
		byte[] originalFileContent = Files.readAllBytes(Paths.get(outputDir, originalNcboRdfFileName));
		byte[] jsonFileContent = Files.readAllBytes(Paths.get(outputDir, ncboRdfFileName));
		assertTrue("File contents differ", Arrays.equals(originalFileContent, jsonFileContent));
		
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

	public TestableNCBOAnnotator(String ontologies, boolean b) {
		super(ontologies, true);
		setAnnotationUtils(new TestableAnnotationUtils());
	}

	@Override
	protected void createDateTriple(org.ontoware.rdf2go.model.Model model,
			String annotationId) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		getAnnotationUtils().createLiteralTriple(annotationId,
				AnnotationUtils.createPropertyString(Prefix.OA, OA_ANNOTATED_AT),
				dateFormat.format(getFixedSampleDate()),XSD.date, model);
	}

	private Date getFixedSampleDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(YEAR, MONTH, DAY);
		return cal.getTime();
	}
	
}

class TestableAnnotationUtils extends AnnotationUtils {

	int uuidCounter = 0;
	
	@Override
	public String generateUuidUri() {
		return String.format("urn:uuid:test%d", ++uuidCounter);
	}
	
}

class TestableJsonFileVisitor extends JsonFileVisitor {

	public TestableJsonFileVisitor(String xmlDirectory, String outputDirectory) {
		super(xmlDirectory, outputDirectory);
	}

	@Override
	public NcboAnnotator getNcboAnnotator() {
		return new TestableNCBOAnnotator(TestOa_Annotation_Model.TEST_SEMANTIC_TYPE_ONTOLOGY, true);
	}
	
	
	
}
