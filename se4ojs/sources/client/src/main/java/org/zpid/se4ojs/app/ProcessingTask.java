package org.zpid.se4ojs.app;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

/**
 * @author barth
 *
 */
public enum ProcessingTask {
	REFERENCE_PREPROCESSING, RDF, STRUCTURE, NCBO_ANNOTATOR, UMLS_ANNOTATOR, ALL, 
	/** Special "marker"-processing task, if all annotators are to be used. */
	ALL_ANNOTATORS;

	@Override
	public String toString() {
		switch (this) {
		case REFERENCE_PREPROCESSING:
			return " reference pre-processing";
		case RDF:
			return " rdf";
		case STRUCTURE:
			return " structure";
		case NCBO_ANNOTATOR:
			return " NCBO annotator";
		case UMLS_ANNOTATOR:
			return " UMLS annotator";
		case ALL:
			return " all";
		case ALL_ANNOTATORS:
			return " all annotators";
		default:
			return StringUtils.EMPTY;
		}
	}

	public static SortedSet<ProcessingTask> getAllTasks() {
		List<ProcessingTask> pts = Arrays.asList(ProcessingTask.values());
		SortedSet<ProcessingTask> tasks = new TreeSet<>(pts);
		tasks.remove(ProcessingTask.ALL);
		return tasks;
	}

	/**
	 * Checks whether the passed in set of {@link ProcessingTask}s contains
	 * all tasks identified as "Annotators". 
	 * If it does, a "marker" task, "ALL_ANNOTATORS", is added to the set and 
	 * the modified set is returned.
	 * 
	 * @param processingTasks the set of tasks to check
	 * @return the set of passed in tasks, or the set plus task "All_annotators" if the check has been successful
	 */
	public static SortedSet<ProcessingTask> checkContainsAllAnnotators(
			SortedSet<ProcessingTask> processingTasks) {
		if (processingTasks.containsAll(getAllAnnotators())) {
			processingTasks.add(ALL_ANNOTATORS);
		}
		return processingTasks;
	}

	public static int getAnnotationTaskCount() {
		return getAllAnnotators().size();
	}

	/**
	 * Returns the tasks marked as "annotators".
	 * 
	 * @return a set containing all annotators
	 */
	public static SortedSet<ProcessingTask> getAllAnnotators() {
		ProcessingTask[] annotators = new ProcessingTask[] {ProcessingTask.NCBO_ANNOTATOR, ProcessingTask.UMLS_ANNOTATOR};
		return new TreeSet<ProcessingTask>(Arrays.asList(annotators));
	}
	

	
}

