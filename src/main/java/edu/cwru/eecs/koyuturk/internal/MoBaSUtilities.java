package edu.cwru.eecs.koyuturk.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.select.SelectAllTaskFactory;
import org.cytoscape.work.TaskManager;

public class MoBaSUtilities {
	
	private CyApplicationManager applicationManager;
	private ResultsPanel resultsPanel;
	private CyNetworkManager networkManager;
	private CyNetworkFactory networkFactory;
	private CyRootNetworkManager rootNetworkManager;
	private TaskManager taskManager;
	private CloneNetworkTaskFactory cloneNetworkTaskFactory;
	
	public MoBaSUtilities(	CyApplicationManager applicationManager,
							ResultsPanel resultsPanel,
							CyNetworkManager networkManager,
							CyNetworkFactory networkFactory,
							CyRootNetworkManager rootNetworkManager,
							TaskManager taskManager,
							CloneNetworkTaskFactory cloneNetworkTaskFactory)
	{
		this.applicationManager = applicationManager;
		this.resultsPanel = resultsPanel;
		this.networkManager = networkManager;
		this.networkFactory = networkFactory;
		this.rootNetworkManager = rootNetworkManager;
		this.taskManager = taskManager;
		this.cloneNetworkTaskFactory = cloneNetworkTaskFactory;
	}

	public CyApplicationManager getApplicationManager() {
		return applicationManager;
	}

	public ResultsPanel getResultsPanel()
	{
		return this.resultsPanel;
	}
	
	public CyNetworkManager getNetworkManager()
	{
		return this.networkManager;
	}
	
	public CyNetworkFactory getNetworkFactory()
	{
		return this.networkFactory;
	}
	
	public CyRootNetworkManager getRootNetworkManager()
	{
		return this.rootNetworkManager;
	}
	
	public TaskManager getTaskManager()
	{
		return this.taskManager;
	}
	
	public CloneNetworkTaskFactory getCloneNetworkTaskFactory()
	{
		return this.cloneNetworkTaskFactory;
	}
}
