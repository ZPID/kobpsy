package org.zpid.se4ojs.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.junit.Ignore;
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
	private static final String PROP_JSON_ANNOTATION = "ncbo.annotator.json.serialize";
	private static final String PROP_EXPAND_MAPPINGS = "ncbo.annotator.expandMappings";
	private static final String PROP_SEMANTIC_TYPE = "ncbo.annotator.semanticType";
	private static final String PROP_EXCLUDE_SYNONYMS = "ncbo.annotator.excludeSynonyms";
	private static final String PROP_INCLUDE_CUI = "ncbo.annotator.cui";

	static final String TEST_SEMANTIC_TYPE_ONTOLOGY = "SNOMEDCT";

	private String ncboRdfFileName = "jsonHandlerTest-ncboAnnotations.rdf";
	private byte[] minimumRdfAnnotation;


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
		TestableConfig config = new TestableConfig();
		config.setPropValue(PROP_JSON_ANNOTATION, "false");
		config.setPropValue(PROP_EXCLUDE_SYNONYMS, "true");
		config.setPropValue(PROP_EXPAND_MAPPINGS, "false");
		config.setPropValue(PROP_INCLUDE_CUI, "false");
		config.setPropValue(PROP_SEMANTIC_TYPE, "false");
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
	 * Currently, this test only checks whether the semantic type rdf file is stored.
	 *
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void testSemanticTypeAnnotation() throws ModelRuntimeException, IOException, JDOMException {
		TestableConfig config = new TestableConfig();
		config.setPropValue(PROP_JSON_ANNOTATION, "true");
		config.setPropValue(PROP_EXCLUDE_SYNONYMS, "true");
		config.setPropValue(PROP_EXPAND_MAPPINGS, "false");
		config.setPropValue(PROP_INCLUDE_CUI, "true");
		config.setPropValue(PROP_SEMANTIC_TYPE, "true");
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
	}

	/**
	 * First runs the NCBO Annotation with the minimum amount of information (no mappings, no synonyms,
	 * no semanticTypes, no cuis) and stores it both as rdf and in Json format.
	 * The generated rdf is copied as a reference annotation.
	 * Then the Json deserializer is used (with the same settings) to generate a second rdf file.
	 * The test passes if both rdf files are
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void testJsonHandler() throws ModelRuntimeException, IOException, JDOMException {
		//set the required properties to annotate with json:
		TestableConfig config = new TestableConfig();
		config.setPropValue(PROP_JSON_ANNOTATION, "true");
		config.setPropValue(PROP_EXCLUDE_SYNONYMS, "true");
		config.setPropValue(PROP_EXPAND_MAPPINGS, "false");
		config.setPropValue(PROP_INCLUDE_CUI, "false");
		config.setPropValue(PROP_SEMANTIC_TYPE, "false");

		String inPath = this.getClass().getClassLoader().getResource("jsonHandlerTest.xml").getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		File in = new File(inPath);
		String outputDir = folder.getRoot().toString();
		// get the structure elements of the input text
		SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		List<BOStructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		//annotate the file
		TestableNCBOAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_SEMANTIC_TYPE_ONTOLOGY);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));

		Path jsonPath = Paths.get(outputDir, "jsonHandlerTest-ncboAnnotations.json");
		assertTrue("The json output file  does not exist",
				jsonPath.toFile().exists());
		//copy the file (otherwise it will be overridden with the current settings)
		String originalNcboRdfFileName = ncboRdfFileName +"-original";
		Files.copy(Paths.get(outputDir, ncboRdfFileName), Paths.get(outputDir, originalNcboRdfFileName),
				LinkOption.NOFOLLOW_LINKS);
		Files.delete(Paths.get(outputDir, ncboRdfFileName));
		//read the json content

		String xmlDirectory = Paths.get(inPath).getParent().toString();
		String jsonDirectory = jsonPath.getParent().toString();
		String outputDirectory = outputDir;
		//TODO jatsProcessingTask
		TestableJsonFileVisitor jsonVisitor = new TestableJsonFileVisitor(xmlDirectory, outputDirectory);
		Files.walkFileTree(Paths.get(jsonDirectory),
				jsonVisitor);
		minimumRdfAnnotation = Files.readAllBytes(Paths.get(outputDir, originalNcboRdfFileName));
		byte[] jsonFileContent = Files.readAllBytes(Paths.get(outputDir, ncboRdfFileName));
		assertTrue("File contents differ", Arrays.equals(minimumRdfAnnotation, jsonFileContent));

	}


	/**
	 * Similar to {@link #testJsonHandler()}, but
	 * first runs the NCBO Annotation with the maximum amount of information (no mappings, but synonyms,
	 * semanticTypes and cuis) and stores it both as rdf and in Json format.
	 *
	 * Then the Json deserializer is used (with the minimum settings from the previous test)
	 * to generate a second rdf file.
	 * The test passes if this second rdf file matches the directly generated rdf file from the previous test.
	 *
	 * @throws ModelRuntimeException
	 * @throws IOException
	 * @throws JDOMException
	 */
	@Test
	public void testJsonHandler_DifferentPropertySettings() throws ModelRuntimeException, IOException, JDOMException {
		String outputDir = folder.getRoot().toString();
		if (minimumRdfAnnotation == null) {
			testJsonHandler();
			Files.delete(Paths.get(outputDir, ncboRdfFileName));
			Files.delete(Paths.get(outputDir, ncboRdfFileName +"-original"));
		}
		//set the required properties to annotate with json:
		TestableConfig config = new TestableConfig();
		config.setPropValue(PROP_JSON_ANNOTATION, "true");
		config.setPropValue(PROP_EXCLUDE_SYNONYMS, "false");
		config.setPropValue(PROP_EXPAND_MAPPINGS, "false");
		config.setPropValue(PROP_INCLUDE_CUI, "true");
		config.setPropValue(PROP_SEMANTIC_TYPE, "true");

		String inPath = this.getClass().getClassLoader().getResource("jsonHandlerTest.xml").getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		File in = new File(inPath);
		// get the structure elements of the input text
		SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		List<BOStructureElement> structureElements = se4ojsAccessHelper.rdfizeSections(in, outputDir);
		//annotate the file
		TestableNCBOAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_SEMANTIC_TYPE_ONTOLOGY);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in, structureElements, Paths.get(outputDir));

		Path jsonPath = Paths.get(outputDir, "jsonHandlerTest-ncboAnnotations.json");
		assertTrue("The json output file  does not exist",
				jsonPath.toFile().exists());

		//read the json content
		config.setPropValue(PROP_EXCLUDE_SYNONYMS, "true");
		config.setPropValue(PROP_EXPAND_MAPPINGS, "false");
		config.setPropValue(PROP_INCLUDE_CUI, "false");
		config.setPropValue(PROP_SEMANTIC_TYPE, "false");

		String xmlDirectory = Paths.get(inPath).getParent().toString();
		String jsonDirectory = jsonPath.getParent().toString();
		String outputDirectory = outputDir;
		TestableJsonFileVisitor jsonVisitor = new TestableJsonFileVisitor(xmlDirectory, outputDirectory);
		Files.walkFileTree(Paths.get(jsonDirectory),
				jsonVisitor);
		byte[] jsonFileContent = Files.readAllBytes(Paths.get(outputDir, ncboRdfFileName));

//		File originalRdf = Paths.get(outputDir, ncboRdfFileName +"-original").toFile();
//		FileUtils.writeByteArrayToFile(originalRdf, minimumRdfAnnotation);

		assertTrue("File contents differ", Arrays.equals(minimumRdfAnnotation, jsonFileContent));

	}

//	This is a test to do manual inspection for files that are not part of the test resources. Thus, it
//	is ignored by default.
	@Ignore
	@Test
	public void testJsonContent() throws FileNotFoundException, IOException {
		String fileName = "psyct.v5i1.4-ncboAnnotations.rdf";
		String inOld = "E:\\KOBPSY3\\PSYCT_out_NoSyns_old";
		String inNew = "E:\\KOBPSY3\\PSYCT_out_NoSyns";
		super.compareTransformationResults(Paths.get(inOld, fileName).toString(), Paths.get(inNew, fileName).toString());

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
