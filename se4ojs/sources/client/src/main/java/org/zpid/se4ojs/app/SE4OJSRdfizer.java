package org.zpid.se4ojs.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.zpid.se4ojs.textStructure.bo.BOStructureElement;

/**
 * Main class of the se4ojs tool.
 *
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

	private static final Logger log = LogManager.getLogger();

	private static Path preProcessedDir;
	private static String inputDir;
	private static String outputDir;
	private static Integer limit = Integer.valueOf(-1);
	private static SortedSet<ProcessingTask> processingTasks = new TreeSet<>();

	/** The number of threads to be used for rdfizing. */
	private static int poolSize = 1;
	private ExecutorService executor;

	protected final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private SE4OJSAccessHelper helper;

	private int annotationTaskCount;

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
		log.trace("Task count: " + queue.size());
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
	public static void main(String... args) throws ModelRuntimeException,
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
		processingTasks = ProcessingTask.checkContainsAllAnnotators(processingTasks);
		if ((inputDir == null) || (outputDir == null)) {
			System.out
					.println(USAGE_INFO);
			System.out
					.println("The request cannot be parsed, please check the usage");
			System.exit(0);
		}

		preProcessedDir = Paths.get(inputDir, PATH_SUFFIX_PREPROCESSED_XML);
		//TODO add a property for preProcessedDir in property file (or an additional input parameter) and set the value here.

		log.info("Execution variables: " + "\nInput " + inputDir
				+ "\nOutput " + outputDir
				+ "\nReference preprocessing "
				+ processingTasks.contains(ProcessingTask.REFERENCE_PREPROCESSING));


		log.info(ProcessingTask.RDF.toString() + " "
				+ processingTasks.contains(ProcessingTask.RDF));

		log.info(ProcessingTask.STRUCTURE.toString() + " "
				+ processingTasks.contains(ProcessingTask.STRUCTURE));

		log.info(ProcessingTask.NCBO_ANNOTATOR.toString() + " "
				+ processingTasks.contains(ProcessingTask.NCBO_ANNOTATOR));

		log.info(ProcessingTask.UMLS_ANNOTATOR.toString() + " "
			    + processingTasks.contains(ProcessingTask.UMLS_ANNOTATOR));

		log.info("\nthread pool size: " + poolSize);

		SE4OJSRdfizer handler = new SE4OJSRdfizer(
				poolSize);
		handler.processDirectory();
		handler.shutDown();
		while (!handler.isTerminated())
			; // waiting
		long endTime = System.currentTimeMillis();
		log.info("\nTotal time: " + (endTime - startTime));
	}

	/**
	 * Process n number of xml files in a directory to the given limit, converts
	 * files to RDF.
	 *
	 */
	public void processDirectory() throws IOException {
		Path path = Paths.get(inputDir);
		if (processingTasks.contains(ProcessingTask.REFERENCE_PREPROCESSING)) {
			XMLFileVisitor fileVisitor = new XMLFileVisitor(this, ProcessingTask.REFERENCE_PREPROCESSING, outputDir);
			Files.walkFileTree(path, fileVisitor);
		} else {
			XMLFileVisitor fileVisitor = new XMLFileVisitor(this, null, outputDir);
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
	void doProcess(final Path path, final SE4OJSAccessHelper helper) {
		try {
			CountDownLatch doneSignal = null;
			List<BOStructureElement> topLevelElements = null;
			for (ProcessingTask task : processingTasks) {
				switch (task) {
				case RDF:
					if (!helper.rdfizeFile(path.toFile(), outputDir)) {
						return;
					}
					break;
				case STRUCTURE:
					topLevelElements = helper.rdfizeSections(path.toFile(), outputDir);
					break;
				case ALL_ANNOTATORS:
					if (topLevelElements != null) {
						doneSignal = new CountDownLatch(ProcessingTask.getAnnotationTaskCount());
						ProcessingTask annoTask = null;
						Set<ProcessingTask> annotators = ProcessingTask.getAllAnnotators();
						for (int i = 0; i < ProcessingTask.getAnnotationTaskCount(); i++) {
							++annotationTaskCount;
							if (i == 0) {
								if (annotationTaskCount % 2 == 0) {
									annoTask = ProcessingTask.UMLS_ANNOTATOR;
								} else {
									annoTask = ProcessingTask.NCBO_ANNOTATOR;
								}
								annotators.remove(annoTask);
							} else {
								annoTask = annotators.iterator().next();
							}
						    annotate(helper, path.toFile(), outputDir, topLevelElements, annoTask, doneSignal);
						}
					    doneSignal.await();
					} else {
						log.error("Unable to annotate input file - please process the sections, too");
					}
					break;
				case NCBO_ANNOTATOR:
				case UMLS_ANNOTATOR:
					doneSignal = new CountDownLatch(1);
					if (!processingTasks.contains(ProcessingTask.ALL_ANNOTATORS)) {
						if (topLevelElements != null) {
							annotate(helper, path.toFile(), outputDir, topLevelElements, task, doneSignal);
							doneSignal.await();
						} else {
							log.error("Unable to annotate input file - please process the sections, too");
						}
					}
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			log.error(path.getFileName()
					+ " FILE could not be processed: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void annotate(SE4OJSAccessHelper helper, File file,
			String outputDir, List<BOStructureElement> topLevelElements,
			ProcessingTask task, CountDownLatch doneSignal) {

		AnnotationTask annotationTask =
				new AnnotationTask(helper, file, outputDir, topLevelElements, task, doneSignal);
		annotationTask.run();
	}

	public Path getPreprocessedXmlDir() {
		return preProcessedDir;
	}

	public static Integer getLimit() {
		return limit;
	}

}

class AnnotationTask implements Runnable {

	SE4OJSAccessHelper helper;
	File paper;
	private String outputDir;
	private List<BOStructureElement> structureElements;
	private Exception exception;
	private ProcessingTask processingTask;
	private CountDownLatch doneSignal;

	public AnnotationTask(SE4OJSAccessHelper helper, File file,
			String outputDir, List<BOStructureElement> structureElements,
			ProcessingTask processingTask, CountDownLatch doneSignal) {

		this.helper = helper;
		this.paper = file;
		this.outputDir = outputDir;
		this.structureElements = structureElements;
		this.processingTask = processingTask;
		this.doneSignal = doneSignal;
	}

	@Override
	public void run() {
		try {
			if (processingTask.equals(ProcessingTask.NCBO_ANNOTATOR)) {
				helper.annotateFileWithNCBOAnnotator(paper, structureElements, outputDir);
			}
			doneSignal.countDown();
		} catch (IOException e) {
			exception = new Exception(paper.getName()
					+ "could not be fully annotated: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Exception getException() {
		return exception;
	}

}
