package org.zpid.se4ojs.annotation.ncbo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnnotationTester {
	 static final String REST_URL = "http://data.bioontology.org"; 
	 static final ObjectMapper mapper = new ObjectMapper();
	 
	public static void main (String[] args) throws UnsupportedEncodingException {
		 String urlParameters;
		 JsonNode annotations;
		 String textToAnnotate = URLEncoder.encode(
//				 "Melanoma is a malignant tumor of melanocytes which are found predominantly in skin but also in the bowel and the eye.", "ISO-8859-1");
				 "originally used the term #emotional intelligence# in their published work and defined it as:"
				 + " A form of intelligence that involves the ability to monitor one's own and others' feelings and emotions,"
				 + " to discriminate among them and to use this information to guide one's thinking and actions."
				 + " According to , EI is an array of emotional and social abilities."
				 + " It includes five components: intrapersonal, interpersonal, adaptability, stress management, and general mood."
				 + "  model outlines four main constructs of EI: self-awareness, self-management, social awareness and relationship management."
				 + " Within different constructs of EI, there are a set of emotional competencies like emotional self-awareness,"
				 + " accurate self-assessment, self-confidence, trustworthiness, conscientiousness, adaptability, achievement drive,"
				 + " initiative, empathy, service orientation, organizational awareness, developing others, influence, communication,"
				 + " conflict management, leadership, change catalyst, building bonds, teamwork and collaboration.",  "ISO-8859-1");
				 
//				 "Conversion symptoms may also be used to solicit attention and social privileges. For example, by developing hysterical blindness, a 15-year-old girl compelled her parents, "
//				 + "who appeared very busy pursuing their own activities, to spend time with her reading her homework assignments. "
//				 + "Similarly, in a case study of hysterical blindness, the patient succeeded,"
//				 + " through a display of conversion symptoms, to put an end to the constant demands and criticisms of his wife and mother-in-law, "
//				 + "which required him to work nights and weekends and perform various chores under their supervision."
//				  , "ISO-8859-1");
		 // Annotations using POST (necessary for long text)
		urlParameters = "text=" + textToAnnotate;
		annotations = jsonToNode(post(REST_URL + "/annotator", urlParameters));
		printAnnotations(annotations);
	}
    private static void printAnnotations(JsonNode annotations) {
        for (JsonNode annotation : annotations) {
            // Get the details for the class that was found in the annotation and print
            JsonNode classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText()));
            if (classDetails != null) {
	            System.out.println("Class details");
	            System.out.println("\tid: " + classDetails.get("@id").asText());
	            System.out.println("\tprefLabel: " + classDetails.get("prefLabel").asText());
	            System.out.println("\tontology: " + classDetails.get("links").get("ontology").asText());
	            System.out.println("\n");
	
	            JsonNode hierarchy = annotation.get("hierarchy");
	            // If we have hierarchy annotations, print the related class information as well
	            if (hierarchy.isArray() && hierarchy.elements().hasNext()) {
	                System.out.println("\tHierarchy annotations");
	                for (JsonNode hierarchyAnnotation : hierarchy) {
	                    classDetails = jsonToNode(get(hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText()));
	                    System.out.println("\t\tClass details");
	                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
	                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
	                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
	                }
	            }
            }
        }
    }

    private static String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + NcboUtils.API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }
    
    private static String post(String urlToGet, String urlParameters) {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "apikey token=" + NcboUtils.API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            conn.disconnect();

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    
}
