package org.zpid.se4ojs.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.zpid.se4ojs.annotation.AnnotationUtils;
import org.zpid.se4ojs.annotation.ncbo.NcboAnnotator;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.app.SE4OJSAccessHelper;
import org.zpid.se4ojs.sparql.Prefix;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

import com.fasterxml.jackson.databind.JsonNode;
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

	private static final String TEST_ONTOLOGY_1 = "CHEBI";
	private static final String TEST_ONTOLOGY_2 = "SNOMEDCT";
	private static final String PROP_ONTOLOGIES = "ncbo.annotator.ontologies";
	private static final String PROP_JSON_ANNOTATION = "ncbo.annotator.json.serialize";
	private static final String PROP_EXPAND_MAPPINGS = "ncbo.annotator.expandMappings";
	private static final String PROP_SEMANTIC_TYPE = "ncbo.annotator.semanticType";
	private static final String PROP_EXCLUDE_SYNONYMS = "ncbo.annotator.excludeSynonyms";
	private static final String PROP_INCLUDE_CUI = "ncbo.annotator.cui";

	static final String TEST_SEMANTIC_TYPE_ONTOLOGY = "SNOMEDCT";


	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * Compares the expected annotation outcome with the actual outcome on the basis of a short document.
	 * Checks that a separate file for each ontology is generated.
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
		config.setPropValue(PROP_ONTOLOGIES, TEST_ONTOLOGY_1 +"," + TEST_ONTOLOGY_2);
		String inPath = this.getClass().getClassLoader()
				.getResource("ncboAnnotatorTestXml.xml").getFile();
		inPath = inPath.replaceFirst("^/(.:/)", "$1");
		File in = new File(inPath);
		String outputDir = folder.getRoot().toString();
		// get the structure elements of the input text
		SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		List<BOStructureElement> structureElements = se4ojsAccessHelper
				.rdfizeSections(in, outputDir);
		// annotate the file
		NcboAnnotator ncboAnnotator = new TestableNCBOAnnotator(TEST_ONTOLOGY_2
				+ "," + TEST_ONTOLOGY_1);
		ncboAnnotator.annotate(Config.getInstitutionUrl(), in,
				structureElements, Paths.get(outputDir));

		 super.compareTransformationResults(folder,
				 "ncboAnnotatorTestXml-ncboAnnotations_chebi.rdf", "ncboAnnotatorReferenceAnnotation.rdf");
		 super.compareTransformationResults(folder,
				 "ncboAnnotatorTestXml-ncboAnnotations_snomedct.rdf", "ncboAnnotatorReferenceAnnotation_Snomed.rdf");
	}


class TestableNCBOAnnotator extends NcboAnnotator {


	private static final int DAY = 30;
	private static final int MONTH = 5;
	private static final int YEAR = 2015;

	public TestableNCBOAnnotator(String ontologies) {
		super(ontologies);
		setAnnotationUtils(new TestableAnnotationUtils());
		((TestableAnnotationUtils) getAnnotationUtils()).initOntologyUUidCounter(
				Config.getNcboOntologiesAsSet());
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

	@Override
	protected String extractOntologyName(JsonNode classDetails) {
		String ontology = super.extractOntologyName(classDetails);
		String ontologyName	= ontology.subSequence(ontology.lastIndexOf("/") + 1, ontology.length()).toString();
		int uuidCounter =
				((TestableAnnotationUtils)getAnnotationUtils()).updateOntologyUuidCounter(ontologyName);
		((TestableAnnotationUtils)getAnnotationUtils()).setUuidCounter(uuidCounter);

		return ontology;

	}

}

class TestableAnnotationUtils extends AnnotationUtils {

	Map<String, Integer> ontologyUuidCounter = new HashMap<>();

	int uuidCounter = 0;
	String currentOntolotgy = StringUtils.EMPTY;

	public void initOntologyUUidCounter(Set<String> ontologyNames) {
		for (String ontology : ontologyNames) {
			ontologyUuidCounter.put(ontology, 0);
		}
	}

	@Override
	public String generateUuidUri() {
		++uuidCounter;
		ontologyUuidCounter.put(currentOntolotgy, uuidCounter);
		return String.format("urn:uuid:test%d", uuidCounter);
	}

	public void setUuidCounter(int uuidCounter) {
		this.uuidCounter = uuidCounter;

	}

	/**
	 * called by {@link TestableNCBOAnnotator} whenever the
	 * ontologies uuid count is incremented.
	 * @return the
	 */
	public int updateOntologyUuidCounter(String ontologyName) {
		currentOntolotgy = ontologyName;
		return ontologyUuidCounter.get(ontologyName);
	}

}



}
