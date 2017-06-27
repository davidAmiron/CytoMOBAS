package edu.cwru.eecs.koyuturk.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;

public class DataWriter {
	
	// Project name
	private String projectName;
	
	// Information directories
	private String projectDir;
	private String subnetsDir;
	private String scoresDir;
	
	// Main MoBaS directory
	private static final String MOBAS_DIR = System.getProperty("user.home") + "/CytoscapeConfiguration/app-data/MoBaS";
	
	public DataWriter(String projectName)
	{
		this.projectName = projectName;
		
		projectDir = MOBAS_DIR + "/" + this.projectName;
		subnetsDir = projectDir + "/subnets";
		scoresDir = projectDir + "/score-data";
		
		// Check if project directory exists
		File identityDirCheck = new File(projectDir);
		if(!identityDirCheck.exists())
			identityDirCheck.mkdirs();
		
		// Check if subnets Directory exists
		File subnetsDirCheck = new File(subnetsDir);
		if(!subnetsDirCheck.exists())
			subnetsDirCheck.mkdirs();
		
		// Check if scores Directory exists
		File scoresDirCheck = new File(scoresDir);
		if(!scoresDirCheck.exists())
			scoresDirCheck.mkdirs();
	}
	
	/**
	 * Write the main subnetworks as a tab delimited edge list
	 * @param subnetDatas List of subnetdata objects to write
	 */
	public void writeSubnets(List<SubnetData> subnetDatas)
	{
		String subnetFile;
		int subnetNum = 1;
		
		// For each subnetwork write it to a file, if it has more than 2 nodes
		for(SubnetData data: subnetDatas)
		{
			subnetFile = subnetsDir + "/subnet_" + subnetNum + ".txt";
			if(data.getSubnet().getNodeCount() > 2)
				writeNetworkToFile(data.getSubnet(), subnetFile);
			subnetNum++;
		}
	}
	
	/**
	 * Write a network to a file as a tab delimited edge list
	 * @param network The network to write
	 * @param filePath The file to write to
	 */
	private static void writeNetworkToFile(CyNetwork network, String filePath)
	{
		List<CyEdge> edges = network.getEdgeList();
		File file = new File(filePath);
		try {
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			for(CyEdge edge: edges)
			{
				fileWriter.write(network.getRow(edge.getSource()).get("name", String.class));
				fileWriter.write("\t");
				fileWriter.write(network.getRow(edge.getTarget()).get("name", String.class));
				fileWriter.write(System.getProperty("line.separator"));
			}
			fileWriter.close();
			
		} catch (IOException e){}
	}
	
	/**
	 * Write the main subnetworks to a file
	 * @param subnetDatas List of subnetdata objects to write
	 */
	public void writeMainSubnetScores(List<SubnetData> subnetDatas)
	{
		String fileName = scoresDir + "/mainScores.txt";
		File file = new File(fileName);
		try
		{
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			
			int subnetNum = 1;
			for(SubnetData data: subnetDatas)
			{
				fileWriter.write("Subnet_" + subnetNum);
				fileWriter.write("\t");
				fileWriter.write(data.getSubnet().getNodeCount() + "");
				fileWriter.write("\t");
				fileWriter.write(data.getScore() + "");
				fileWriter.write(System.getProperty("line.separator"));
				subnetNum++;
			}
			fileWriter.close();
			
		} catch (IOException e) {}
	}
	
	/**
	 * Write the permuted network scores to a file
	 * @param permutedNetworksScores 2D ArrayList of doubles to write
	 */
	public void writePermutedScores(ArrayList<ArrayList<Double>> permutedNetworksScores)
	{
		String fileName = scoresDir + "/permutationScores.txt";
		File file = new File(fileName);
		try
		{
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);
			
			for(ArrayList<Double> networkScores: permutedNetworksScores)
			{
				for(Double score: networkScores.subList(0, networkScores.size() - 1))
				{
					fileWriter.write(score + "\t");
				}
				fileWriter.write(networkScores.get(networkScores.size() - 1) + "");
				fileWriter.write(System.getProperty("line.separator"));
			}
			fileWriter.close();
		} catch (IOException e) {}
	}
	
}
