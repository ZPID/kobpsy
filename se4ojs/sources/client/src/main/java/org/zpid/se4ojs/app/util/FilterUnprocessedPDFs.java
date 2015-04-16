package org.zpid.se4ojs.app.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * <p>
 * This script copies all PDF-files in a given folder-structure that have
 * been processed to the output destination.
 * Files that have not been processed, because the article language is not supported according
 * to the specification in the config.properties file, are filtered and not copied.
 * </p>

 * <p>
 * Arguments:
 * <li>Input folder:
 * The script assumes that the given input folder or it sub-folders contain the
 * pdf-files. Please provide a writable folder - The unprocessed PDF-files identified will be deleted.
 * It is expected that the pdfs' names are equivalent to the xmls' file names.
 * </li>
 * <li>
 * Log-file extract: The scripts expects a file that contains a log-message excerpt.
 * It contains lines consisting of log-messages like this:
 * </br>
 * Line 1709: WARN  2015-04-02 09:51:05,296 [pool-1-thread-2] (Jats2Spar.java:129) - Unsupported article language: bg. Processing of article D:\data\tmp_rest_Alle_Journals-XML+PDF-Store\structured\psyct.v7i2.119.xmlaborted.
 * </li>
 * </p>
 * 
 * @author barth
 *
 */
public class FilterUnprocessedPDFs {

	private static final String LOG_MSG_END_OF_FILENAME = ".xml";
	private static final String LOG_MSG_START = "Line";
	private static Logger log = Logger.getLogger(FilterUnprocessedPDFs.class);
	
	/**
	 * @param input folder with all PDFs. The unprocessed ones will be deleted
	 * @param log-file excerpts with a "unsupported article language"-message per line
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Path inFolderPath = Paths.get(args[0]);
		Path outFolderPath = Paths.get(args[1]);
		Path logMsgsPath = Paths.get(args[2]);
		
		Set<String> filesToFilter = extractFileNamesToFilter(logMsgsPath);
		PDFFileVisitor visitor = new PDFFileVisitor(filesToFilter, outFolderPath);
		Files.walkFileTree(inFolderPath, visitor);

	}

	/**
	 * Extracts the paths of the files to delete.
	 * 
	 * @param logMsgsPath
	 * @return
	 * @throws IOException 
	 */
	private static Set<String> extractFileNamesToFilter(Path logMsgsPath) throws IOException {
		Set<String> fileNames = new HashSet<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(logMsgsPath.toFile())));
		int lineNo = 0;
		String line = reader.readLine();
		while (line != null) {
			if (line.trim().startsWith(LOG_MSG_START)) {
				int posBeforeFileName = line.lastIndexOf("\\");
				if (posBeforeFileName != -1 && line != null) {
					int posAfterFileName = line.lastIndexOf(LOG_MSG_END_OF_FILENAME);
					String fileName = line.substring(posBeforeFileName + 1, posAfterFileName);
					fileNames.add(fileName);
					
				} else if (line != null) {
					log.error("Expected file name. Could not find one in line: " + lineNo);
				}			
			}
			line = reader.readLine();
		}
		reader.close();
		return fileNames;
	}


	/**
	 * Copies the processed pdf-files to the given output destination.
	 * If the output folder does not exist, it is created.
	 * 
	 * @author barth
	 *
	 */
	static class PDFFileVisitor extends SimpleFileVisitor<Path> {

		private static final String PDF_FILE_TYPE = ".pdf";
		private Set<String> filesToDelete;
		private Path outFolderPath;
		
		public PDFFileVisitor(Set<String> filesToDelete, Path outFolderPath) throws IOException {
			super();
			this.filesToDelete = filesToDelete;
			this.outFolderPath = outFolderPath;
			Files.createDirectories(outFolderPath);
		}


		@Override
		public FileVisitResult visitFile(Path pdfPath, BasicFileAttributes attrs)
				throws IOException {
			String fileName = pdfPath.getFileName().toString();
			if (fileName.endsWith(PDF_FILE_TYPE)) {
				String fileName_typeStripped = null;
				try {
					fileName_typeStripped = fileName.substring(0, fileName.lastIndexOf(PDF_FILE_TYPE));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!filesToDelete.contains(fileName_typeStripped)) {
					Files.copy(pdfPath, Paths.get(outFolderPath.toString(), fileName));
				} else {
					log.info("Filtered file: " + pdfPath.toString());				
				}				
			}
			return FileVisitResult.CONTINUE;
		}
	}
}

