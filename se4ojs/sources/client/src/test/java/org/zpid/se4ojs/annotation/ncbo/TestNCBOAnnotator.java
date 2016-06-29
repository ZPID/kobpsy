package org.zpid.se4ojs.annotation.ncbo;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;


public class TestNCBOAnnotator {

	private static final String NUMBER_EXISTING_IN_NCIT = "70000";
	private static final String TEXT = "cognitive disorders may cause anxiety";
	private static final String STOPWORD_FROM_DEFAULT_LIST = "above";
	private static final String UNIGRAM_EXISTING_IN_SNOMED = "C";
	private static final String BIGRAM_EXISTING_IN_SNOMED = "Xi";
	private static final String TRIGRAM_EXISTING_IN_SNOMED = "HIV";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	@Ignore //This test is only meant for explorative manual investigation of the NCBO annotator's properties only
	public void test() throws UnsupportedEncodingException {
		NcboAnnotator annotator = new NcboAnnotator("NCIT,SNOMEDCT,MESH,APAONTO");
		JsonNode results = null;
		results = annotator.callAnnotator(TEXT);
		assertFalse("Some results have should have been returned", results.size()  == 0);
		showResults(annotator, results);
		
//		Check if numbers are filtered. 
		results = annotator.callAnnotator(NUMBER_EXISTING_IN_NCIT);
		assertTrue("Numbers are not excluded from query results", results.size() == 0);

//		Check if default stopwords are filtered. 
		results = annotator.callAnnotator(STOPWORD_FROM_DEFAULT_LIST);
		assertTrue("Bioportal's default stopwords are not excluded from query results", results.size() == 0);
		
//		Check if numbers are filtered. 
		results = annotator.callAnnotator(STOPWORD_FROM_DEFAULT_LIST);
		assertTrue("Bioportal's default stopwords are not excluded from query results", results.size() == 0);
		
//		Check if minimum length option is adhered to
		results = annotator.callAnnotator(UNIGRAM_EXISTING_IN_SNOMED);
		assertTrue("Unigrams are not excluded from query results", results.size() == 0);
		results = annotator.callAnnotator(BIGRAM_EXISTING_IN_SNOMED);
		assertTrue("Bigrams are not excluded from query results", results.size() == 0);
		results = annotator.callAnnotator(TRIGRAM_EXISTING_IN_SNOMED);
		assertFalse("Trigrams are excluded from query results", results.size() == 0);
		showResults(annotator, results);
	}

	protected void showResults(NcboAnnotator annotator, JsonNode results) {
		for (JsonNode result : results) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = 
            		NcboAnnotator.jsonToNode(NcboAnnotator.get(
            				result.get("annotatedClass").get("links").get("self").asText()));
			JsonNode annotationInfo = result.get("annotations");
            if (classDetails != null) {
                String conceptId = annotator.getClassDetail(classDetails, "@id");
                String prefLabel = annotator.getClassDetail(classDetails, "prefLabel");
                String conceptBrowserUrl = annotator.getClassDetail(classDetails, "links" , "ui");
                String ontology = classDetails.get("links").get("ontology").asText();

            }
		}
	}

}
