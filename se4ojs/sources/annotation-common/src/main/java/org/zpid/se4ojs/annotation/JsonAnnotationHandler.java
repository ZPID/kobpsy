package org.zpid.se4ojs.annotation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zpid.se4ojs.annotation.util.AnnotationEvent;
import org.zpid.se4ojs.annotation.util.AnnotationListener;
import org.zpid.se4ojs.annotation.util.AnnotationResultsAvailableEvent;
import org.zpid.se4ojs.annotation.util.JsonResultEvent;
import org.zpid.se4ojs.annotation.util.Observable;
import org.zpid.se4ojs.annotation.util.PaperAnnotationFinishedEvent;
import org.zpid.se4ojs.annotation.util.PaperAnnotationStartEvent;
import org.zpid.se4ojs.annotation.util.ParagraphAnnotationStartEvent;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This Handler contains methods for storing annotations in json format and for reading
 * the generated json files.
 * 
 * The intention is that one handler per file is created when writing the json files.
 * 
 * @author barth
 *
 */
public class JsonAnnotationHandler implements AnnotationListener, Observable {

	private JsonGenerator jsonGenerator;
	private String jsonPath;
	private static Logger logger = LogManager.getLogger();
	
	/* Reading mode means we are reading persisted json Results with the Json Parser */
	private boolean isInReadingMode = false;
	
	private List<AnnotationListener> listeners = new ArrayList<>();
	  private ObjectMapper docMapper;
       private JsonNode docNode;
       
       
	public JsonAnnotationHandler(String jsonPath, boolean isInReadingMode) {
		super();
		this.jsonPath = jsonPath;
		this.isInReadingMode = isInReadingMode;
		if (!isInReadingMode) {
			JsonFactory jFactory = new JsonFactory();
			try {
				jsonGenerator =
						jFactory.createGenerator(new File(jsonPath), JsonEncoding.UTF8);
				jsonGenerator.writeStartArray();
	
			} catch (IOException e) {
				logger.error("Unable to create Json Generator for file: ", jsonPath);
				e.printStackTrace();
			}
	
			jsonGenerator.setCodec(new ObjectMapper());
		} else {
			initParsing();
		}
	}

	
	public void save(JsonNode results, String subElementUri) {
		if (!isInReadingMode) {
			if (jsonGenerator == null) {
				throw new IllegalStateException("The json Generator has not been initialized");
			}
			try {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeObjectField(subElementUri, results);
				jsonGenerator.writeEndObject();

			} catch (IOException e) {
				logger.error("Unable to write json result to file");
				e.printStackTrace();
			}
		}
	}
	
	public void saveMapping(String conceptId, JsonNode results) {
		if (!isInReadingMode) {
			if (jsonGenerator == null) {
				throw new IllegalStateException("The json Generator has not been initialized");
			}
			try {
				jsonGenerator.writeStartObject();
				jsonGenerator.writeObjectField(conceptId, results);
				jsonGenerator.writeEndObject();

			} catch (IOException e) {
				logger.error("Unable to write json result to file");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void handlePaperAnnotationFinished() {
		try {
			if(!isInReadingMode) {
				jsonGenerator.writeEndArray();
				jsonGenerator.close();
			} 
		} catch (IOException e) {
			logger.error("Unable to close Json Generator for file: ", jsonPath);
			e.printStackTrace();
			e.printStackTrace();
		}
	}

	@Override
	public void update(AnnotationEvent event) {
		if (event instanceof AnnotationResultsAvailableEvent) {
			AnnotationResultsAvailableEvent annotationResultEvent = (AnnotationResultsAvailableEvent) event;
			save(annotationResultEvent.getAnnotationResults(), annotationResultEvent.getSubElementUri());
		}
		if (event instanceof PaperAnnotationFinishedEvent) {
			handlePaperAnnotationFinished();
		}
		if (event instanceof ParagraphAnnotationStartEvent) {
			ParagraphAnnotationStartEvent startEvent = (ParagraphAnnotationStartEvent) event;
			handleParagraphAnnotationStartEvent(startEvent.getSubElementUri());
		}
		if (isInReadingMode && event instanceof PaperAnnotationStartEvent) {
			listeners.add(((PaperAnnotationStartEvent) event).getJsonResultListener());
		}
		
	}

	public void handleParagraphAnnotationStartEvent(String subElementUri) {
		if (isInReadingMode) {
			JsonNode locatedNode = docNode.findValue(subElementUri);
			if (locatedNode != null) {
				notifyListeners(listeners, new JsonResultEvent(locatedNode));
			}
		}
	}
	
	public void initParsing() {
		isInReadingMode  = true;
		try {
			createParser(jsonPath);
		} catch (IOException e) {
			logger.error("Unable to create jsonParser for: " + jsonPath);
			e.printStackTrace();
		}
	};
	
	public void createParser(String jsonPath) throws JsonParseException, IOException {
        docMapper = new ObjectMapper();
        docNode = docMapper.readTree(new File(jsonPath));
	}


	@Override
	public void addListener(AnnotationListener listener) {
		listeners.add(listener);
	}
	
}
