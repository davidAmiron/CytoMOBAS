package edu.cwru.eecs.koyuturk.internal;

import org.cytoscape.model.subnetwork.CySubNetwork;

public class SubnetData {
	
	private CySubNetwork subnet;
	private double score;
	
	public SubnetData(CySubNetwork subnet, double score)
	{
		this.subnet = subnet;
		this.score = score;
	}
	
	public CySubNetwork getSubnet()
	{
		return this.subnet;
	}
	
	public double getScore()
	{
		return this.score;
	}
	
}
