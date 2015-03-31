/**
 * 
 */
package org.zpid.se4ojs.textStructure.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

/**
 * Collects the available section-types from a given directory of JATS xml files.
 * 
 * @author barth
 *
 */
public class SectionTypeDictionaryBuilder {
	
	private SortedSet<String> sectionTypes = new TreeSet<>();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inputDir = args[0];
		Path dirPath = Paths.get(inputDir);
		if (!Files.isDirectory(dirPath)) {
			System.out.println("Not a directory");
			System.exit(-1);
		} 
		SectionTypeDictionaryBuilder typeCollector = new SectionTypeDictionaryBuilder();
		XMLFileVisitor visitor = typeCollector.new XMLFileVisitor();
		try {
			Files.walkFileTree(dirPath, visitor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String type : typeCollector.sectionTypes) {
			System.out.println(type);
		}
	}

	class XMLFileVisitor extends SimpleFileVisitor<Path> {
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			SAXBuilder builder = new SAXBuilder();
			Document document = null;
			try {
				document = (Document) builder.build(file.toFile());
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			Element rootNode = document.getRootElement();
			IteratorIterable<Element> sections = rootNode.getDescendants(new ElementFilter("sec"));
			for (Element section: sections) {
				
				String type = section.getAttributeValue("sec-type");
				if (type != null) {
					sectionTypes.add(type);			
				}
			}
			return FileVisitResult.CONTINUE;
		}
		
	}
}
