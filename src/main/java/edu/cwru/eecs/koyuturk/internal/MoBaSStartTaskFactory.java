package edu.cwru.eecs.koyuturk.internal;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MoBaSStartTaskFactory extends AbstractTaskFactory{
	
	MoBaSUtilities utils;
	
	public MoBaSStartTaskFactory(MoBaSUtilities utils)
	{
		this.utils = utils;
	}
	
	public TaskIterator createTaskIterator()
	{
		return new TaskIterator(new MoBaSStartTask(utils));
	}
	
}
