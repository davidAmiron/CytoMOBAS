package edu.cwru.eecs.koyuturk.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

public class NodeInfo implements Comparable{
	
	private CyRow row;
	private CyNode node;
	private boolean added;
	private String nodeScoreAttribute;
	
	public NodeInfo(CyRow row, CyNetwork network, String nodeScoreAttribute)
	{
		this.row = row;
		this.node = network.getNode(row.get("SUID", Long.class));
		this.added = false;
		this.nodeScoreAttribute = nodeScoreAttribute;
	}
	
	public CyRow getRow()
	{
		return this.row;
	}
	
	public CyNode getNode()
	{
		return this.node;
	}
	
	public boolean getAdded()
	{
		return this.added;
	}
	
	public void setAdded()
	{
		this.added = true;
	}

	@Override
	public int compareTo(Object o) {
		NodeInfo nodeInfo2 = (NodeInfo) o;
		Double thisScore = this.row.get(nodeScoreAttribute, Double.class);
		Double otherScore = nodeInfo2.getRow().get(nodeScoreAttribute, Double.class);
		if(thisScore < otherScore)
			return -1;
		else if(thisScore > otherScore)
			return 1;
		else
			return 0;
	}
	
}
