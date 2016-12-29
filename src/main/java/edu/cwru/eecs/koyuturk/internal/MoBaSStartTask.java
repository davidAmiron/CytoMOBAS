package edu.cwru.eecs.koyuturk.internal;

import java.util.concurrent.TimeUnit;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class MoBaSStartTask extends AbstractTask{
	
	MoBaSUtilities utils;
	
	public MoBaSStartTask(MoBaSUtilities utils)
	{
		this.utils = utils;
	}
	
	public void run(TaskMonitor monitor) throws Exception
	{
		OptionsPanel panel = new OptionsPanel(utils);
	}
	
}
