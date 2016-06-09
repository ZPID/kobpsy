package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

	static int uuidCounter = 0;
	
	@Override
	public String generateUuidUri() {
		return String.format("urn:uuid:test%d", ++uuidCounter);
	}
	
}
