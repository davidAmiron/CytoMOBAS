package edu.cwru.eecs.koyuturk.internal;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.work.TaskManager;

public class ControlPanel extends JPanel implements CytoPanelComponent{

	
	private static final long serialVersionUID = -5522955287287153743L;
	
	// Title of window
	private final String TITLE = "MoBaS";
	
	// Node score options
	private JPanel pnlNodeScore;
	private JLabel lblNodeScore;
	private JRadioButton rdbtnPValues;
	private JRadioButton rdbtnFoldChange;
	
	// Edge score options
	private JPanel pnlEdgeScore;
	private JLabel lblEdgeScore;
	private JRadioButton rdbtnMultiplication;
	private JRadioButton rdbtnMinimum;
	private JRadioButton rdbtnCorrelation;
	
	// Number of permutations
	private JPanel pnlPermutations;
	private JLabel lblPermutationsNum;
	private JFormattedTextField ftfPermutations;
	
	// Connectivity
	private JPanel pnlConnectivity;
	private JLabel lblConnectivity;
	private JSlider sldrConnectivity;
	
	
	public ControlPanel()
	{
		
	}
	
	public String getTitle()
	{
		return this.TITLE;
	}
	
	public CytoPanelName getCytoPanelName()
	{
		return CytoPanelName.WEST;
	}
	
	public Icon getIcon()
	{
		return null;
	}
	
	public Component getComponent()
	{
		return this;
	}
}
