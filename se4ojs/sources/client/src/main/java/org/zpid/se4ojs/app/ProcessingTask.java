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
	REFERENCE_PREPROCESSING, RDF, STRUCTURE, NCBO_ANNOTATOR, UMLS_ANNOTATOR, ALL, ALL_ANNOTATORS;

	/** The number of Processing Tasks that are of type annotation task. */
	public static final int ANNOTATION_TASK_COUNT = 2;
	
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

	public static SortedSet<ProcessingTask> checkContainsAllAnnotators(
			SortedSet<ProcessingTask> processingTasks) {
		if (processingTasks.contains(ProcessingTask.NCBO_ANNOTATOR)
				&& processingTasks.contains(ProcessingTask.UMLS_ANNOTATOR)) {
			processingTasks.add(ALL_ANNOTATORS);
		}
		return processingTasks;
	}

	public int getAnnotationTaskCount() {
		return ANNOTATION_TASK_COUNT;
	}
	

	
}

