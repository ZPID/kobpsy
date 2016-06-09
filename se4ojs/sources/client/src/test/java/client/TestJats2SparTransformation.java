package client;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;
import org.zpid.se4ojs.app.Config;
import org.zpid.se4ojs.app.SE4OJSAccessHelper;

public class TestJats2SparTransformation extends AnnotationTester {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private String inPath;
	private File in;
	private String outputDir;
	
	@Before
	public void setUp() throws Exception {
		 inPath = this.getClass().getClassLoader().getResource("crossrefApiTestXml.xml").getFile();
		 inPath = inPath.replaceFirst("^/(.:/)", "$1");
		 in = new File(inPath);
		 outputDir = folder.getRoot().toString();
	}

	@Test
	public void testCrossrefApi() throws JAXBException, SAXException, URISyntaxException, IOException {
		 TestableConfig config = new TestableConfig();
		 config.setPropValue("true");
		 SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
		 se4ojsAccessHelper.rdfizeFile(in, outputDir);

		 compareTransformationResults(folder, 
			"crossrefApiTestXml.rdf", "crossrefApiReferenceXml.rdf");
	}
}

class TestableConfig extends Config {
	
	private static final Object CROSSREF_PROP = "crossrefApi.links.pdf";
	private String propValue;
	
	@Override
	protected String getProperty(String prop) {
		if (prop.equals(CROSSREF_PROP)) {
			return propValue;
		}
		return super.getProperty(prop);
	}
	
	
	protected String getPropValue() {
		return propValue;
	}

	protected void setPropValue(String propValue) {
		this.propValue = propValue;
	}


	public TestableConfig() {
		super();
		INSTANCE = this;
	}
	
}
