package org.zpid.se4ojs.app;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;
import org.zpid.se4ojs.app.ProcessingTask;

public class XMLFileVisitor extends SimpleFileVisitor<Path>{
	
	private SE4OJSRdfizer handler;
	private int count = 0;
	private final int limit;
	private Logger log = Logger.getLogger(XMLFileVisitor.class);
	private final ProcessingTask processingTask;
	
	
	public XMLFileVisitor(SE4OJSRdfizer handler, ProcessingTask task) {
		super();
		this.handler = handler;
		this.limit = SE4OJSRdfizer.getLimit();
		this.processingTask = task;
	}

	/**
     * Invoked for a file in a directory.
     *
     * <p> Unless overridden, this method returns {@link FileVisitResult#CONTINUE
     * CONTINUE}.
     */
    @Override
    public FileVisitResult visitFile(Path inputfilePath, BasicFileAttributes attrs)
        throws IOException
 {
		Path fileName = inputfilePath.getFileName();
		if (fileName.toString().endsWith(
				Config.getInputFileSuffix().toLowerCase())
				|| fileName.toString().endsWith(
						Config.getInputFileSuffix().toUpperCase())) {

			if (processingTask != null && processingTask.equals(ProcessingTask.REFERENCE_PREPROCESSING)) {
				handler.preprocessFile(inputfilePath);
			} else {
				handler.processFile(inputfilePath);

				if (++count % 1000 == 0) {
					System.gc();
				}
				if (limit != -1 && count >= limit) {
					return FileVisitResult.TERMINATE;
				}
			}
    	}
        return FileVisitResult.CONTINUE;
    }

    /**
     * Skips the "sections" - sub-directory if we are doing pre-processing
     * (in this case, this sub-directory is the output dir).
     */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
			throws IOException {
		log.info("Processing directory: " + dir.toString());
		if (dir.endsWith(handler.getPreprocessedXmlDir()) && processingTask != null && 
				processingTask.equals(ProcessingTask.REFERENCE_PREPROCESSING)) {
			return FileVisitResult.SKIP_SUBTREE;
		}
		return super.preVisitDirectory(dir, attrs);
	}
    
}
