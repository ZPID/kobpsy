package org.zpid.se4ojs.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;
import org.zpid.se4ojs.app.SE4OJSAccessHelper;

public class TestJats2SparTransformation extends AnnotationTester {

    private static final String CROSSREF_PROP = "crossrefApi.links";

    private static final String PUBPSYCH_LINKS = "pubpsych.links";

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
         config.setPropValue(CROSSREF_PROP, "true");
         config.setPropValue(PUBPSYCH_LINKS, "false");
         SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
         se4ojsAccessHelper.rdfizeFile(in, outputDir);

         compareTransformationResults(folder,
            "crossrefApiTestXml.rdf", "crossrefApiReferenceXml.rdf");
    }

    @Test
    public void testPubPsychApi() throws JAXBException, SAXException, URISyntaxException, IOException {
    	 TestableConfig config = new TestableConfig();
         config.setPropValue(CROSSREF_PROP, "true");
         config.setPropValue(PUBPSYCH_LINKS, "true");
         inPath = this.getClass().getClassLoader().getResource("pubpsychApiTestXml.xml").getFile();
         inPath = inPath.replaceFirst("^/(.:/)", "$1");
         in = new File(inPath);
         SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
         se4ojsAccessHelper.rdfizeFile(in, outputDir);

         compareTransformationResults(folder,
            "pubpsychApiTestXml.rdf", "pubpsychApiReferenceXml.rdf");
    }

    @Test
    public void testNotPubPsychNotCrossrefApi() throws JAXBException, SAXException, URISyntaxException, IOException {
	   	 TestableConfig config = new TestableConfig();
	     config.setPropValue(CROSSREF_PROP, "false");
	     config.setPropValue(PUBPSYCH_LINKS, "false");
        inPath = this.getClass().getClassLoader().getResource("pubpsychApiTestXml.xml").getFile();
        inPath = inPath.replaceFirst("^/(.:/)", "$1");
        in = new File(inPath);
        SE4OJSAccessHelper se4ojsAccessHelper = new SE4OJSAccessHelper();
        se4ojsAccessHelper.rdfizeFile(in, outputDir);

        compareTransformationResults(folder,
           "pubpsychApiTestXml.rdf", "noApiReferenceXml.rdf");
   }
}


