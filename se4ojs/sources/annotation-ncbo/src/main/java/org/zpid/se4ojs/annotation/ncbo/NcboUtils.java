package org.zpid.se4ojs.annotation.ncbo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.zpid.se4ojs.app.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NcboUtils {
	
	 public static final String API_KEY = Config.getNCBOAPIKey();
	 static final String REST_URL = Config.getNCBOServiceURL();
	 static final ObjectMapper mapper = new ObjectMapper(); 

	    
	   static String get(String urlToGet) {
	        URL url;
	        HttpURLConnection conn;
	        BufferedReader rd;
	        String line;
	        String result = "";
	        try {
	            url = new URL(urlToGet);
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");
	            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
//	            conn.setRequestProperty("apikey" , API_KEY);
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
	     static JsonNode jsonToNode(String json) {
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
	    
}
