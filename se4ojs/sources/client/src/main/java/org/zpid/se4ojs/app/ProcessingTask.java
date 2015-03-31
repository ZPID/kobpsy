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
	REFERENCE_PREPROCESSING, RDF, STRUCTURE, NCBO_ANNOTATOR, UMLS_ANNOTATOR, ALL;

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
	
}

