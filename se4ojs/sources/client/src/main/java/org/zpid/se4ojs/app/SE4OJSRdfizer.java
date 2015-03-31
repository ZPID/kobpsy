package org.zpid.se4ojs.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.zpid.se4ojs.textStructure.bo.StructureElement;
/**
 * @author barth
 *
 */
public class SE4OJSRdfizer {
	private static final String USAGE_INFO = "Usage: -in <xml papers dir> -out <output dir>  -structure "
			+ "\n\n-all or any of (optional, if none present -all) "
			+ "\n -metadata"
			+ "\n\n-references     (optional) \nIf present, paper's unstructured references will be preprocessed, i.e."
			+ "\n                     xml-encoded and stored in a separate folder"
			+ "\n                     within the input folder, called 'structured'."
			+ "\n                     The files contained will be used as input for further processing steps"
			+ "\n                  (experimental feature, not fully developed & tested) \n\n"
			+ "\n\n -structure     (optional) If present, textual structure will be rdfized"
			+ "\n\n -ncboAnnotator (optional;to be used in conjunction with 'structured' "
			+ "\n\n -umlsAnnotator (optional;to be used in conjunction with 'structured' "
			+ "\n                     (precondition: MetaMap must be set up"
			+ "\n\n";

	private static final String PATH_SUFFIX_PREPROCESSED_XML = "structured";

	private static Path preProcessedDir;
	private static String inputDir;
	private static String outputDir;
	private static Integer limit = Integer.valueOf(-1);
	private static SortedSet<ProcessingTask> processingTasks = new TreeSet<>();

	/** The number of threads to be used for rdfizing. */
	private static int poolSize = 1;
	private ExecutorService executor;

	protected final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private Logger logger = Logger.getLogger(SE4OJSRdfizer.class);
	private SE4OJSAccessHelper helper;
	private List<StructureElement> topLevelElements;
	
	/**
	 * Default constructor, it defines an initial pool with 5 threads, a maximum
	 * of 10 threads, and a keepAlive time of 300 seconds.
	 */
	public SE4OJSRdfizer() {
		this(10);
	}

	/**
	 * Constructor with parameters, it enables to define the initial pool size,
	 * maximum pool size, and keep alive time in seconds; it initializes the
	 * ThreadPoolExecutor.
	 * 
	 * @param poolSize
	 *            Thread pool size
	 */
	protected SE4OJSRdfizer(int poolSize) {
		executor = Executors.newFixedThreadPool(poolSize);
		this.helper = new SE4OJSAccessHelper();
	}

	/**
	 * Run a task with the thread pool and modifies the waiting queue list as
	 * needed.
	 * 
	 * @param task
	 */
	protected void runTask(Runnable task) {
		executor.execute(task);
		logger.debug("Task count: " + queue.size());
	}

	/**
	 * Shuts down the ThreadPoolExecutor.
	 */
	public void shutDown() {
		executor.shutdown();
	}

	/**
	 * Informs whether or not the threads have finished all pending executions.
	 * 
	 * @return
	 */
	public boolean isTerminated() {
		// this.handler.getLogger().debug("Task count: " + queue.size());
		return this.executor.isTerminated();
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ModelRuntimeException
	 */
	public static void main(String[] args) throws ModelRuntimeException,
			IOException {
		long startTime = System.currentTimeMillis();

		if (args == null) {
			System.out
					.println(USAGE_INFO);
			System.exit(0);
		}

		for (int i = 0; i < args.length; i++) {
			String str = args[i];
			if (str.equalsIgnoreCase("-in")) {
				inputDir = args[++i];
			} else if (str.equalsIgnoreCase("-out")) {
				outputDir = args[++i];
			} else if (str.equalsIgnoreCase("-references")) {
				processingTasks.add(ProcessingTask.REFERENCE_PREPROCESSING);
			} else if (str.equalsIgnoreCase("-structure")) {
				processingTasks.add(ProcessingTask.STRUCTURE);
			} else if (str.equalsIgnoreCase("-all")) {
				processingTasks = ProcessingTask.getAllTasks();
				break;
			} else if (str.equalsIgnoreCase("-ncboAnnotator")) {
				processingTasks.add(ProcessingTask.NCBO_ANNOTATOR);
			} else if (str.equalsIgnoreCase("-umlsAnnotator")) {
				processingTasks.add(ProcessingTask.UMLS_ANNOTATOR);
			} else if (str.equalsIgnoreCase("-metadata")) {
				processingTasks.add(ProcessingTask.RDF);
			} else if (str.equalsIgnoreCase("-poolSize")) {
				poolSize = Integer.parseInt(args[++i]);
			} 
		}

		if (processingTasks.isEmpty()
				|| processingTasks.contains(ProcessingTask.ALL)) {
			processingTasks = ProcessingTask.getAllTasks();
		}
		if ((inputDir == null) || (outputDir == null)) {
			System.out
					.println(USAGE_INFO);
			System.out
					.println("The request cannot be parsed, please check the usage");
			System.exit(0);
		}
		
		preProcessedDir = Paths.get(inputDir, PATH_SUFFIX_PREPROCESSED_XML);
		//TODO add a property for preProcessedDir in property file (or an additional input parameter) and set the value here.

		System.out.println("Execution variables: " + "\nInput " + inputDir
				+ "\nOutput " + outputDir
				+ "\nReference preprocessing "
				+ processingTasks.contains(ProcessingTask.REFERENCE_PREPROCESSING));
		

		System.out.println(ProcessingTask.RDF.toString() + " "
				+ processingTasks.contains(ProcessingTask.RDF));

		System.out.println(ProcessingTask.STRUCTURE.toString() + " "
				+ processingTasks.contains(ProcessingTask.STRUCTURE));

		System.out.println(ProcessingTask.NCBO_ANNOTATOR.toString() + " "
				+ processingTasks.contains(ProcessingTask.NCBO_ANNOTATOR));

		System.out.println(ProcessingTask.UMLS_ANNOTATOR.toString() + " "
			    + processingTasks.contains(ProcessingTask.UMLS_ANNOTATOR));

		System.out.println("\nthread pool size: " + poolSize);

		SE4OJSRdfizer handler = new SE4OJSRdfizer(
				poolSize);
		handler.processDirectory();
		handler.shutDown();
		while (!handler.isTerminated())
			; // waiting
		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time: " + (endTime - startTime));
	}

	/**
	 * Process n number of xml files in a directory to the given limit, converts
	 * files to RDF.
	 * 
	 */
	public void processDirectory() throws IOException {
		Path path = Paths.get(inputDir);
		if (processingTasks.contains(ProcessingTask.REFERENCE_PREPROCESSING)) {
			XMLFileVisitor fileVisitor = new XMLFileVisitor(this, ProcessingTask.REFERENCE_PREPROCESSING);	
			Files.walkFileTree(path, fileVisitor);
		} else {
			XMLFileVisitor fileVisitor = new XMLFileVisitor(this, null);
			Files.walkFileTree(path, fileVisitor);			
		}
	}

	public void preprocessFile(final Path path) {
		runTask(new Runnable() {
			public void run() {
				helper.structureReferences(path, preProcessedDir);
				doProcess(Paths.get(preProcessedDir.toString(), path.getFileName().toString()), helper);
			}
		});
	}
	
	public void processFile(final Path path) {
		runTask(new Runnable() {
			public void run() {
				doProcess(path, helper);
			}});
	}

	/**
	 * Executes the processing tasks.
	 * 
	 * @param path
	 * @param helper
	 * @return true, if the file could be processed, false otherwise
	 */
	boolean doProcess(Path path, SE4OJSAccessHelper helper) {
		try {
			for (ProcessingTask task : processingTasks) {
				switch (task) {
				case RDF:
					if (!helper.rdfizeFile(path.toFile(), outputDir)) {
						return false;
					}
					break;
				case STRUCTURE:
					topLevelElements = helper.rdfizeSections(path.toFile(), outputDir);
					break;
				case NCBO_ANNOTATOR:
					helper.annotateFileWithNCBOAnnotator(path.toFile(), topLevelElements, outputDir);
					break;
				case UMLS_ANNOTATOR:
					if (topLevelElements != null) {
						helper.annotateFileWithUmlsAnnotator(path.toFile(), topLevelElements, outputDir);						
					} else {
						logger.error("Unable to annotate input file - please process the sections, too");
					}

					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			logger.error(path.getFileName()
					+ " FILE could not be processed: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Path getPreprocessedXmlDir() {
		return preProcessedDir;
	}

	public static Integer getLimit() {
		return limit;
	}

}
