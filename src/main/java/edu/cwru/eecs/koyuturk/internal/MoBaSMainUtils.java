package edu.cwru.eecs.koyuturk.internal;

import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

public class MoBaSMainUtils {
	
	private CyNetwork network;
	private List<CyRow> nodeRows;
	private HashMap<Long, Boolean> added;
	
	/**
	 * Constructor
	 * @param network The network being worked on
	 * @param nodeRows List<CyRow> of node rows (must already be sorted best to worst)
	 */
	public MoBaSMainUtils(CyNetwork network, List<CyRow> nodeRows)
	{
		this.network = network;
		this.nodeRows = nodeRows;
		this.added = new HashMap<Long, Boolean>();
		for(int i = 0; i < nodeRows.size(); i++)
		{
			added.put(getNode(nodeRows.get(i)).getSUID(), false);
		}
	}
	
	public boolean nodeLeft()
	{
		for(int i = 0; i < nodeRows.size(); i++)
			if(!isAdded(getNode(nodeRows.get(i))))
				return true;
		return false;
	}
	
	
	public CyNode getBestRemainingNode()
	{
		for(int i = 0; i < nodeRows.size(); i++)
			if(!isAdded(getNode(nodeRows.get(i))))
				return getNode(nodeRows.get(i));
		return null;
	}
	
	public void setAdded(CyNode node)
	{
		added.put(node.getSUID(), true);
	}
	
	public boolean isAdded(CyNode node)
	{
		return added.get(node.getSUID());
	}
	
	private CyNode getNode(CyRow row)
	{
		return network.getNode(row.get("SUID", Long.class));
	}
	
	
	
}
