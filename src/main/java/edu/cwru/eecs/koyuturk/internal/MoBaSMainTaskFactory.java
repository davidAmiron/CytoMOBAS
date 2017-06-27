package edu.cwru.eecs.koyuturk.internal;

import java.util.ArrayList;
import java.util.Random;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MoBaSMainTaskFactory extends AbstractTaskFactory{

	// MoBaS Parameters
	private String projectName;
	private NodeScoreMethod nodeScoreMethod;
	private EdgeScoreMethod edgeScoreMethod;
	private int permutations;
	private double connectivity;
	private String nodeScoreAttribute;
	private String backgroundNodeScoreAttribute;
	private AbsentNodeScoreTreatment absentNodeScoreTreatment;
	
	// Utils
	MoBaSUtilities utils;
	
	public MoBaSMainTaskFactory(MoBaSUtilities utils, String projectName,
			NodeScoreMethod nodeScoreMethod, EdgeScoreMethod edgeScoreMethod,
			int permutations, double connectivity, String nodeScoreAttribute,
			String backgroundNodeScoreAttribute, AbsentNodeScoreTreatment absentNodeScoreTreatment)
	{
		this.utils = utils;
		this.projectName = projectName;
		this.nodeScoreMethod = nodeScoreMethod;
		this.edgeScoreMethod = edgeScoreMethod;
		this.permutations = permutations;
		this.connectivity = connectivity;
		this.nodeScoreAttribute = nodeScoreAttribute;
		this.backgroundNodeScoreAttribute = backgroundNodeScoreAttribute;
		this.absentNodeScoreTreatment = absentNodeScoreTreatment;
	}
	
	public TaskIterator createTaskIterator()
	{
		return new TaskIterator(new MoBaSMainTask(this.utils, this.projectName,
				this.nodeScoreMethod, this.edgeScoreMethod,
				this.permutations, this.connectivity,
				this.nodeScoreAttribute, this.backgroundNodeScoreAttribute,
				this.absentNodeScoreTreatment));
	}
	
}
