package org.zpid.se4ojs.annotation.ncbo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * @author barth
 *
 */
public class Recommender {

		private Logger log = Logger.getLogger(Recommender.class);
//		private static Map<String, Double> ontologyScores = new HashMap<String, Double>();
		
		public static void main( String[] args ) throws IOException {
//			testRecommender();
			Path inputDir = Paths.get(args[0]);
			Path outFile = Paths.get(args[1]);
			recommendForFiles(inputDir, outFile);
		}

		private static void recommendForFiles(Path inputDir, Path outFile) throws IOException {
			Recommender recommender = new Recommender();
			RecommendationVisitor visitor = recommender.new RecommendationVisitor(new HashMap<String, Double>());
			Files.walkFileTree(inputDir, visitor);
			calculateFinalScores(outFile, visitor.getScores());
		}

		@SuppressWarnings("unused")
		private static void testRecommender() {
			Map<String, Double> ontologyScores = new HashMap<String, Double>();
			try {
				String text = "Malignoma in the bowel and the eye";
				Recommender recommender = new Recommender();
				recommender.addScores(text, ontologyScores);
				recommender.addScores(text, ontologyScores);
				Recommender.calculateFinalScores(Paths.get("D:\\temp\\ontScores.txt"), ontologyScores);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	public Map<String, Double> recommendByAbstract(File paper, Map<String, Double> scores)
			throws JDOMException, IOException {
		
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new DummyEntityResolver());
		InputSource is = new InputSource(paper.getAbsolutePath());
		is.setEncoding("UTF-8");
		
		Document document = (Document) builder.build(is);
		Element rootNode = document.getRootElement();
		Iterator<Element> itAbstr = rootNode.getDescendants(new ElementFilter("abstract"));

		if (itAbstr.hasNext()) {
			Element abstractEl = itAbstr.next();
			List<Element> paras = abstractEl.getChildren("p");
			for (Element p : paras) {
				scores = addScores(p.getText(), scores);
			}			
		}
		return scores;
	}
	
	public Map<String, Double> recommendByParagraph(File paper, Map<String, Double> scores)
			throws JDOMException, IOException {
		
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new DummyEntityResolver());
		InputSource is = new InputSource(paper.getAbsolutePath());
		is.setEncoding("UTF-8");
		
		Document document = (Document) builder.build(is);
		Element rootNode = document.getRootElement();
		Iterator<Element> it = rootNode.getDescendants(new ElementFilter("p"));

		while (it.hasNext()) {
			Element paragraph = it.next();
				scores = addScores(paragraph.getText(), scores);
		}
		return scores;
	}
	
	public Map<String, Double> addScores(String text, Map<String, Double> scores) {
		try {
			log.debug("text:" + text + "\n");
			String resourcesString = NcboUtils.get(NcboUtils.REST_URL
					+ String.format("/recommender?text=%s",
							URLEncoder.encode(text, "UTF-8")));
			JsonNode resources = NcboUtils.jsonToNode(resourcesString);
//			TODO check the response
			List<JsonNode> scoreNodes = resources.findValues("score");
			List<JsonNode> ontologyNodes = resources.findValues("ontology");
			assert (scoreNodes.size() == ontologyNodes.size());
			Iterator<JsonNode> iterator = ontologyNodes.iterator();
			for (JsonNode score : scoreNodes) {
				log.debug(String.format("score: %s", score));
				Double currScore = score.doubleValue();
				JsonNode ontology = iterator.next();
				String ontologyId = ontology.get("@id").asText();
				Double prevScore = scores.get(ontologyId);
				if (prevScore != null) {
					currScore += prevScore; 
				}
				scores.put(ontologyId, currScore);

				log.debug(String.format("ont id: %s",
						ontology.get("@id").asText()));
			}
		} catch (Exception e) {
			log.error("error: " + e);
			e.printStackTrace();
		} 
		return scores;
	}
		
		public static Map<String, Double> calculateFinalScores(Path outfile, Map<String, Double> scores)
				throws IOException {
			
			FileWriter fileWriter = new FileWriter(outfile.toFile());
			Map<String, Double> sortedOntologyScores = Recommender.sortByValue(scores);
			for (Entry<String, Double> e : sortedOntologyScores.entrySet()) {
				fileWriter.write(e.getValue().toString());
				fileWriter.write("\t");
				fileWriter.write(e.getKey() + "\n");
			}
			fileWriter.close();
			return scores;
		}
		
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map) {
		
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	class RecommendationVisitor extends SimpleFileVisitor<Path> {

		private Map<String, Double> scores;
		
		public RecommendationVisitor(Map<String, Double> scores) {
			super();
			this.scores = scores;
		}


		public Map<String, Double> getScores() {
			return scores;
		}


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			try {
				log.debug("File: " + file.getFileName().toString());
				scores = recommendByParagraph(file.toFile(), scores);
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			return super.visitFile(file, attrs);
		}
		
	}
	
	class DummyEntityResolver implements EntityResolver {
	    public InputSource resolveEntity(String publicID, String systemID)
	        throws SAXException {
	        
	        return new InputSource(new StringReader(""));
	    }
	}
	
}

