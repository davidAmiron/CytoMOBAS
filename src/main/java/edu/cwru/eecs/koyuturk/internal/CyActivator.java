package edu.cwru.eecs.koyuturk.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.select.SelectAllTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}
	
	public void start(BundleContext bc)
	{
		
		CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		CyRootNetworkManager rootNetworkManager = getService(bc, CyRootNetworkManager.class);
		TaskManager taskManager = getService(bc, TaskManager.class);
		CloneNetworkTaskFactory cloneNetworkTaskFactory = getService(bc, CloneNetworkTaskFactory.class);
		
		Properties props = new Properties();
		props.setProperty("title", "MoBaS");
		props.setProperty("preferredMenu", "Apps");
		
		ResultsPanel resultsPanel = new ResultsPanel(appManager);
		
		MoBaSUtilities utils = new MoBaSUtilities(appManager, resultsPanel, networkManager, networkFactory, rootNetworkManager, taskManager, cloneNetworkTaskFactory);
		
		MoBaSStartTaskFactory mobasFactory = new MoBaSStartTaskFactory(utils);
		
		registerService(bc, mobasFactory, TaskFactory.class, props);
		registerService(bc, resultsPanel, CytoPanelComponent.class, new Properties());
	}
	
}