package edu.cwru.eecs.koyuturk.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class MoBaSMainTask extends AbstractTask {
	
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
	private MoBaSUtilities utils;
	private CyApplicationManager manager;
	private Random rand;
	private DataWriter dataWriter;
	
	
	// Input
	private CyNetwork networkMain;
	private CyRootNetwork networkMainRootNet;
	private CySubNetwork networkSubMain;
	private List<CyEdge> networkMainOrigionalEdges;
	
	
	// Other info
	private double scoresMean;
	private Class nodeScoreAttributeType;
	private Class backgroundNodeScoreAttributeType;
	private double defaultNodeScore;
	
	
	// Results
	private ArrayList<SubnetData> networkMainSubnets;
	private ArrayList<ArrayList<Double>> permutedNetworksScores;
	
	private Logger logger = Logger.getLogger(MoBaSMainTask.class.getName());
	
	public MoBaSMainTask(MoBaSUtilities utils, String projectName,
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
		this.rand = new Random();
		this.manager = utils.getApplicationManager();
		this.networkMainSubnets = new ArrayList<SubnetData>();
		this.permutedNetworksScores = new ArrayList<ArrayList<Double>>();
		try{
			logger.addHandler(new FileHandler("/Users/davidmiron/testLogs/cytolog.txt"));
			logger.setLevel(Level.INFO);
		} catch (IOException e) {}
		this.networkMain = manager.getCurrentNetwork();
		this.networkMainOrigionalEdges = networkMain.getEdgeList();
		this.nodeScoreAttributeType = networkMain.getDefaultNodeTable().getColumn(nodeScoreAttribute).getType();
		if(!this.backgroundNodeScoreAttribute.equals("None"))
			this.backgroundNodeScoreAttributeType = networkMain.getDefaultNodeTable().getColumn(backgroundNodeScoreAttribute).getType();
		this.absentNodeScoreTreatment = absentNodeScoreTreatment;
		
		scoresMean = getScoresMean();
		
		if (absentNodeScoreTreatment == AbsentNodeScoreTreatment.IGNORE)
			if (nodeScoreMethod == NodeScoreMethod.P_VALUES)
				defaultNodeScore = 1; // Set to 1 so that if non-existent data, log(1) = 0 so it is ignored
			else
				defaultNodeScore = 0; // Set to 0 so that if non-existent data, multiplying by 0 ignores the node
		else
			defaultNodeScore = scoresMean;
			
	}
	
	/**
	 * Main method
	 */
	public void run(TaskMonitor tm)
	{
		tm.setTitle("MoBaS");
		tm.setProgress(-1);
		dataWriter = new DataWriter(projectName);

		// Initialize subnetwork to work on
		networkMainRootNet = utils.getRootNetworkManager().getRootNetwork(networkMain);
		networkSubMain = networkMainRootNet.addSubNetwork(networkMain.getNodeList(), networkMain.getEdgeList());
		
		// Get main subnetworks
		tm.setStatusMessage("Getting main subnets");
		networkMainSubnets = getSubnets(networkSubMain);
		
		// Write main subnetworks
		tm.setStatusMessage("Writing main subnets data");
		dataWriter.writeSubnets(networkMainSubnets);
		dataWriter.writeMainSubnetScores(networkMainSubnets);
		
		// Clear main subnetworks
		removeSubnetsFromRootNetwork(networkMainSubnets);
		
		// Get permuted subnetwork scores
		for(int i = 0; i < permutations; i++)
		{
			//-- First check if user has cancelled
			if (cancelled)
			{
				dataWriter.writePermutedScores(permutedNetworksScores);
				if (i > 0)
					tm.showMessage(TaskMonitor.Level.WARN, "Cancelled, wrote main networks and scores and permutation data up through permutation " + i);
				else if (i == 0)
					tm.showMessage(TaskMonitor.Level.WARN, "Cancelled, wrote main networks and scores");
				return;
			}
			
			//-- Get subnetwork to work with
			networkSubMain = networkMainRootNet.addSubNetwork(networkMain.getNodeList(), networkMain.getEdgeList());
			
			//-- Permute network
			tm.setStatusMessage("Permuting " + (i + 1));
			permuteNetwork(networkSubMain);
			
			//-- Get subnetwork scores
			tm.setStatusMessage("Getting subnet scores " + (i + 1));
			permutedNetworksScores.add(getSubnetScores(networkSubMain));
			
			//-- Remove subnetwork
			networkMainRootNet.removeSubNetwork(networkSubMain);
		}
		
		//utils.getResultsPanel().showResultsActions();
		tm.setStatusMessage("Writing permutation scores");
		dataWriter.writePermutedScores(permutedNetworksScores);
		
		//-- Set project name in results panel
		utils.getResultsPanel().setProjectName(projectName);
		utils.getResultsPanel().generateGraph();
	}
	
	/**
	 * Permute a network
	 * @param network The network to permute
	 */
	public void permuteNetwork(CySubNetwork network)
	{
		//long startTime = System.nanoTime();
		// Permute
		int edge1index;
		int edge2index;
		CyEdge edge1;
		CyEdge edge2;
		
		List<CyEdge> edges = network.getEdgeList();
		int edgesNum = network.getEdgeCount();
		ArrayList<CyEdge> edgesToRemove = new ArrayList<CyEdge>(edgesNum * 2);
		int [] removedEdges = new int [edgesNum * 3];
		for(int i = 0; i < removedEdges.length; i++)
		{
			removedEdges[i] = 0;
		}
		for(int i = 0; i < edgesNum; i++)
		{
			// Get two random edges
			edge1index = rand.nextInt(edges.size());
			edge2index = rand.nextInt(edges.size());
			
			//-- Make sure they have not been removed yet
			while(removedEdges[edge1index] == 1)
				edge1index = rand.nextInt(edges.size());
			while(removedEdges[edge2index] == 1)
				edge2index = rand.nextInt(edges.size());
			
			edge1 = edges.get(edge1index);
			edge2 = edges.get(edge2index);
			
			// Check to avoid double edges and self loops
			if( !(network.containsEdge(edge1.getSource(), edge2.getTarget()) ||
				  network.containsEdge(edge2.getSource(), edge1.getTarget()) ||
				  edge1.getSource().equals(edge2.getTarget())                ||
				  edge2.getSource().equals(edge1.getTarget())))
			{
				
				// Add edges to be removed afterwards
				edgesToRemove.add(edge1);
				edgesToRemove.add(edge2);
				removedEdges[edge1index] = 1;
				removedEdges[edge2index] = 1;
				
				// Add new edges
				edges.add(network.addEdge(edge1.getSource(), edge2.getTarget(), false));
				edges.add(network.addEdge(edge2.getSource(), edge1.getTarget(), false));
			}	
		}
		network.removeEdges(edgesToRemove);
		long startR = System.nanoTime();
		networkMainRootNet.removeEdges(edges.subList(edgesNum, edges.size()));
		long endR = System.nanoTime();
		logger.info("Removing edges: " + (endR - startR) / 1000000000);
		//long endTime = System.nanoTime();
		//new Dump("Time: " + ((endTime - startTime) / 1000000000));
	}
	
	
	
	
	/**
	 * Gets the subnetworks of a network
	 * @param network The network to analyze
	 * @return ArrayList of SubnetData objects with subnetwork and score
	 */
	public ArrayList<SubnetData> getSubnets(CyNetwork network)
	{
		///
		// Initialize things
		///
		
		// The subnetworks to return
		ArrayList<SubnetData> subnets = new ArrayList<SubnetData>();
		
		
		// The root network (subclass of CyNetwork, used for generating and organizing subnetworks)
		CyRootNetwork rootNet = utils.getRootNetworkManager().getRootNetwork(network);
		
		
		// The default node table
		CyTable nodeTable = network.getDefaultNodeTable();
		
		
		// The list of node rows, organized by score according to which node score method is used (p-values or fold change)
		List<CyRow> nodeRows = nodeTable.getAllRows();
		
		//-- Sort nodeRows, first is the best (p-values want to be small, fold change large)
		NodeRowsComparatorByDoubleColumn comparer = new NodeRowsComparatorByDoubleColumn(nodeScoreAttribute);
		if(nodeScoreMethod == NodeScoreMethod.FOLD_CHANGE)
		{
			Collections.sort(nodeRows, comparer);
			Collections.reverse(nodeRows);
		}
		else if(nodeScoreMethod == NodeScoreMethod.P_VALUES)
		{
			Collections.sort(nodeRows, comparer);
		}
		

		MoBaSMainUtils mUtils = new MoBaSMainUtils(network, nodeRows);
		
		
		///
		// Start
		///
		
		
		CySubNetwork subnet;
		List<CyNode> subnetNodes;
		List<CyNode> neighborNodes = new ArrayList<CyNode>();
		List<CyNode> subnetNodeNeighborsToCheck;
		double neighborScore;
		CyNode neighborNode = null;
		double bestNeighborScore;
		CyNode bestNeighborNode = null;
		boolean nodeToAdd;
		double subnetScore;
		
		//•• While all nodes are not yet found
		while (mUtils.nodeLeft())
		{
			//-- Create the subnetwork
			subnet = rootNet.addSubNetwork();
			subnetScore = 0;

			//-- Get the best node not yet added to a subnetwork
			CyNode startNode = mUtils.getBestRemainingNode();
			
			//-- Add it to the subnetwork
			subnet.addNode(startNode);
			mUtils.setAdded(startNode);

			//•• While it is possible to add a node and increase the subnetwork score
			nodeToAdd = true;
			while(nodeToAdd)
			{
				//-- Get list of nodes in the subnetwork
				subnetNodes = subnet.getNodeList();

				neighborNodes.clear();
				//-- Get neighboring nodes of all nodes in subnet if not already seen
				for(int i = 0; i < subnetNodes.size(); i++)
				{
					subnetNodeNeighborsToCheck = network.getNeighborList(subnetNodes.get(i), CyEdge.Type.ANY);
					for(int j = 0; j < subnetNodeNeighborsToCheck.size(); j++)
					{
						if(!mUtils.isAdded(subnetNodeNeighborsToCheck.get(j)))
							neighborNodes.add(subnetNodeNeighborsToCheck.get(j));
					}
				}
				
				//-- Score each
				bestNeighborScore = -Double.MAX_VALUE;
				for(int i = 0; i < neighborNodes.size(); i++)
				{
					neighborNode = neighborNodes.get(i);
					neighborScore = scoreNewNode(neighborNode, subnet);    //<--- FINISH THE SCORING METHOD PLEASE
					if(neighborScore > bestNeighborScore)
					{
						bestNeighborScore = neighborScore;
						bestNeighborNode = neighborNode;
					}
				}
				
				//-- Add the largest positive score, and finish if all are negative
				if(bestNeighborScore > 0)
				{
					subnet.addNode(bestNeighborNode);
					subnetScore += bestNeighborScore;
					for(CyEdge edge: network.getAdjacentEdgeList(bestNeighborNode, CyEdge.Type.ANY))
					{
						if(subnet.containsNode(edge.getSource()) && subnet.containsNode(edge.getTarget()))
							subnet.addEdge(edge);
					}
					mUtils.setAdded(bestNeighborNode);
				}
				else
				{
					nodeToAdd = false;
				}
			}
			
			
			//-- Add the subnetwork to subnets
			subnets.add(new SubnetData(subnet, subnetScore));
			
		}
		subnets.sort(new SubnetDataComparator());
		return subnets;
	}
	
	
	/**
	 * Get the scores for the subnetworks (used for permutations)
	 * @param network
	 * @return
	 */
	public ArrayList<Double> getSubnetScores(CyNetwork network)
	{int c = 0; long startTime = System.nanoTime();
		///
		// Initialize things
		///
		
		// The subnetwork scores
		ArrayList<Double> subnetScores = new ArrayList<Double>();
		
		
		// The root network (subclass of CyNetwork, used for generating and organizing subnetworks)
		CyRootNetwork rootNet = utils.getRootNetworkManager().getRootNetwork(network);
		
		
		// The default node table
		CyTable nodeTable = network.getDefaultNodeTable();
		
		
		// The list of node rows, organized by score according to which node score method is used (p-values or fold change)
		List<CyRow> nodeRows = nodeTable.getAllRows();
		
		
		//-- Sort nodeRows, first is the best (p-values want to be small, fold change large)
		NodeRowsComparatorByDoubleColumn comparer = new NodeRowsComparatorByDoubleColumn(nodeScoreAttribute);
		if(nodeScoreMethod == NodeScoreMethod.FOLD_CHANGE)
		{
			Collections.sort(nodeRows, comparer);
			Collections.reverse(nodeRows);
		}
		else if(nodeScoreMethod == NodeScoreMethod.P_VALUES)
		{
			Collections.sort(nodeRows, comparer);
		}

		MoBaSMainUtils mUtils = new MoBaSMainUtils(network, nodeRows);
		
		
		///
		// Something here
		///
		
		
		//CySubNetwork subnet;
		CySubNetwork subnet = rootNet.addSubNetwork();
		List<CyNode> subnetNodes;
		List<CyNode> neighborNodes = new ArrayList<CyNode>();
		List<CyNode> subnetNodeNeighborsToCheck;
		double neighborScore;
		CyNode neighborNode = null;
		double bestNeighborScore;
		CyNode bestNeighborNode = null;
		boolean nodeToAdd;
		double subnetScore;
		
		//•• While all nodes are not yet found
		while (mUtils.nodeLeft())
		{
			//-- Create the subnetwork
			//subnet = rootNet.addSubNetwork();
			subnet.removeEdges(subnet.getEdgeList());
			subnet.removeNodes(subnet.getNodeList());
			subnetScore = 0;

			//-- Get the best node not yet added to a subnetwork
			CyNode startNode = mUtils.getBestRemainingNode();
			
			//-- Add it to the subnetwork
			subnet.addNode(startNode);
			mUtils.setAdded(startNode);

			//•• While it is possible to add a node and increase the subnetwork score
			nodeToAdd = true;
			while(nodeToAdd)
			{
				//-- Get list of nodes in the subnetwork
				subnetNodes = subnet.getNodeList();

				neighborNodes.clear();
				//-- Get neighboring nodes of all nodes in subnet if not already seen
				for(int i = 0; i < subnetNodes.size(); i++)
				{
					subnetNodeNeighborsToCheck = network.getNeighborList(subnetNodes.get(i), CyEdge.Type.ANY);
					for(int j = 0; j < subnetNodeNeighborsToCheck.size(); j++)
					{
						if(!mUtils.isAdded(subnetNodeNeighborsToCheck.get(j)))
							neighborNodes.add(subnetNodeNeighborsToCheck.get(j));
					}
				}
				
				//-- Score each
				bestNeighborScore = -Double.MAX_VALUE;
				for(int i = 0; i < neighborNodes.size(); i++)
				{
					neighborNode = neighborNodes.get(i);
					neighborScore = scoreNewNode(neighborNode, subnet);c++;    //<--- FINISH THE SCORING METHOD PLEASE
					if(neighborScore > bestNeighborScore)
					{
						bestNeighborScore = neighborScore;
						bestNeighborNode = neighborNode;
					}
				}
				
				//-- Add the largest positive score, and finish if all are negative
				if(bestNeighborScore > 0)
				{
					subnet.addNode(bestNeighborNode);
					subnetScore += bestNeighborScore;
					for(CyEdge edge: network.getAdjacentEdgeList(bestNeighborNode, CyEdge.Type.ANY))
					{
						if(subnet.containsNode(edge.getSource()) && subnet.containsNode(edge.getTarget()))
							subnet.addEdge(edge);
					}
					mUtils.setAdded(bestNeighborNode);
				}
				else
				{
					nodeToAdd = false;
				}
			}
				
			//-- Add the subnetwork score to subnetScores
			subnetScores.add(subnetScore);
			//-- Remove the subnetwork
			//rootNet.removeSubNetwork(subnet);
		}
		subnetScores.sort(new SubnetScoresComparator());
		long endTime = System.nanoTime();logger.info(((endTime - startTime) / 100000000) + ":" + c + "");
		
		return subnetScores;
	}
	
	
	/**
	 * Scores a node based on what it would add to a subnetwork
	 * @param newNode the node being added
	 * @param subnet The current subnetwork
	 * @return the score of the node for that subnetwork
	 */
	private Double scoreNewNode(CyNode newNode, CySubNetwork subnet)
	{
		double newNodeIndividualScore = getDoubleScoreValue(newNode, defaultNodeScore);

		
		
		double addNodeScore = 0;
		List<CyNode> subnetNodes = subnet.getNodeList();
		CyNode subnetNode;
		double subnetNodeIndividualScore;
		if (nodeScoreMethod == NodeScoreMethod.P_VALUES)
		{
			
			//•• For each node already a part of the subnet
			for(int i = 0; i < subnetNodes.size(); i++)
			{
				subnetNode = subnetNodes.get(i);
				subnetNodeIndividualScore = getDoubleScoreValue(subnetNode, defaultNodeScore);
						/////////subnet.getRow(subnetNode).get(nodeScoreAttribute, Double.class);
				//-- If there is a connection
				if(subnet.getRootNetwork().containsEdge(subnetNode, newNode) || subnet.getRootNetwork().containsEdge(newNode, subnetNode))
				{
					
					if(edgeScoreMethod == EdgeScoreMethod.MULTIPLICATION)
					{
						addNodeScore += Math.log(newNodeIndividualScore) * Math.log(subnetNodeIndividualScore);
					}
					else if(edgeScoreMethod == EdgeScoreMethod.MINIMUM)
					{
						addNodeScore += Math.log(Math.min(newNodeIndividualScore, subnetNodeIndividualScore));
					}
					else if(edgeScoreMethod == EdgeScoreMethod.CORRELATION)
					{
						// TODO: CORRELATION SCORING
					}
				}
				
				//-- Penalize, whether or not there is a connection
				if (backgroundNodeScoreAttribute.equals("None"))
					addNodeScore -= connectivity * Math.log(scoresMean) * Math.log(scoresMean);
				else
					addNodeScore -= connectivity * Math.log(getDoubleBackgroundScore(subnetNode, defaultNodeScore)) * Math.log(getDoubleBackgroundScore(newNode, defaultNodeScore));
				
			}
			
		}
		else if(nodeScoreMethod == NodeScoreMethod.FOLD_CHANGE)
		{
			
			//•• For each node already added to the subnet
			for(int i = 0; i < subnetNodes.size(); i++)
			{
				subnetNode = subnetNodes.get(i);
				subnetNodeIndividualScore = getDoubleScoreValue(subnetNode, defaultNodeScore);
						//subnet.getRow(subnetNode).get(nodeScoreAttribute, Double.class);
				
				//-- If there is a connection
				if(subnet.containsEdge(subnetNode, newNode) || subnet.containsEdge(newNode, subnetNode))
				{
					
					if(edgeScoreMethod == EdgeScoreMethod.MULTIPLICATION)
					{
						addNodeScore += newNodeIndividualScore * subnetNodeIndividualScore;
					}
					else if(edgeScoreMethod == EdgeScoreMethod.MINIMUM)
					{
						addNodeScore += Math.min(newNodeIndividualScore, subnetNodeIndividualScore);
					}
					else if(edgeScoreMethod == EdgeScoreMethod.CORRELATION)
					{
						// TODO: CORRELATION SCORING
					}
				}
				//-- Penalize, whether or not there is a connection
				if (backgroundNodeScoreAttribute.equals("None"))
					addNodeScore -= connectivity * Math.log(scoresMean) * Math.log(scoresMean);
				else
					addNodeScore -= connectivity * Math.log(getDoubleBackgroundScore(subnetNode, defaultNodeScore)) * Math.log(getDoubleBackgroundScore(newNode, defaultNodeScore));
			}
		}
		return addNodeScore;
	}
	
	/**
	 * Class used to organize rows
	 */
	private class NodeRowsComparatorByDoubleColumn implements Comparator<CyRow>
	{
		private String column;
		
		public NodeRowsComparatorByDoubleColumn(String column)
		{
			this.column = column;
		}

		public int compare(CyRow row1, CyRow row2) {
			double score1 = Math.abs(row1.get(column, Double.class, defaultNodeScore));
			double score2 = Math.abs(row2.get(column, Double.class, defaultNodeScore));
			if(score1 < score2)
				return -1;
			else if(score1 > score2)
				return 1;
			else
				return 0;
			
		}
	}
	
	
	private void removeSubnetsFromRootNetwork(ArrayList<SubnetData> subnetDatas)
	{
		for(SubnetData sd: subnetDatas)
		{
			sd.getSubnet().getRootNetwork().removeSubNetwork(sd.getSubnet());
		}
	}
	
	/**
	 * Get the score value for a node as a double
	 * @param node The node to score
	 * @return The score as a double
	 */
	private double getDoubleScoreValue(CyNode node, double defaultValue)
	{
		if(nodeScoreAttributeType.equals(Integer.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(nodeScoreAttribute, Integer.class, defaultValue).doubleValue());
			} catch (Exception e) {
				new Dump("A");
				throw new RuntimeException(e.getMessage());
			}
		}
		else if (nodeScoreAttributeType.equals(Long.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(nodeScoreAttribute, Long.class, defaultValue).doubleValue());
			} catch (Exception e) {
				new Dump("B");
				throw new RuntimeException(e.getMessage());
			}
		}
		else if (nodeScoreAttributeType.equals(Double.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(nodeScoreAttribute, Double.class, defaultValue));
			} catch (Exception e) {
				new Dump(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Invalid node score attribute type");
		}
	}
	
	/**
	 * Get the background score of a node as a double
	 * @param node
	 * @return The score as a double
	 */
	private double getDoubleBackgroundScore(CyNode node, double defaultValue)
	{
		if(backgroundNodeScoreAttributeType.equals(Integer.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(backgroundNodeScoreAttribute, Integer.class, defaultValue).doubleValue());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		else if (backgroundNodeScoreAttributeType.equals(Long.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(backgroundNodeScoreAttribute, Long.class, defaultValue).doubleValue());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		else if (backgroundNodeScoreAttributeType.equals(Double.class))
		{
			try {
				return Math.abs(networkMain.getRow(node).get(backgroundNodeScoreAttribute, Double.class, defaultValue));
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		else
		{
			throw new RuntimeException("Invalid background node score attribute type");
		}
	}
	
	
		
	/**
	 * Get the mean of all the scores, ignoring data that is not present
	 * @return The mean
	 */
	private double getScoresMean()
	{
		double mean = 0;
		int count = 0;
		List<CyNode> nodes = networkMain.getNodeList();
		for(CyNode node: nodes)
		{
			double scoreValue = getDoubleScoreValue(node, Double.MAX_VALUE);
			if (scoreValue != Double.MAX_VALUE) {
				mean += scoreValue;
				count++;
			}
		}
		mean /= count;
		return mean;
	}

	/**
	 * Class used to compare to SubnetData objects by their score
	 */
	private class SubnetDataComparator implements Comparator<SubnetData>
	{
		public int compare(SubnetData subnet1, SubnetData subnet2)
		{
			double score1 = subnet1.getScore();
			double score2 = subnet2.getScore();
			if(score1 > score2)
				return -1;
			else if(score1 < score2)
				return 1;
			else
				return 0;
		}
	}
	
	/**
	 * Class to compare two scores
	 */
	private class SubnetScoresComparator implements Comparator<Double>
	{
		public int compare(Double score1, Double score2)
		{
			if(score1 > score2)
				return -1;
			else if(score1 < score2)
				return 1;
			else
				return 0;
		}
	}
}