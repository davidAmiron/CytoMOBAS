package edu.cwru.eecs.koyuturk.internal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskManager;

public class OptionsPanel extends JFrame implements ActionListener{
	
	private MoBaSUtilities utils;
	private MoBaSMainTaskFactory mainTaskFactory;
	
	// Project name
	private JPanel pnlProjectName;
	private JLabel lblProjectName;
	private JTextField tfProjectName;
	
	// Node score options
	private JPanel pnlNodeScore;
	private JLabel lblNodeScore;
	private ButtonGroup btngrpNodeScore;
	private JRadioButton rdbtnPValues;
	private JRadioButton rdbtnFoldChange;
	
	// Edge score options
	private JPanel pnlEdgeScore;
	private JLabel lblEdgeScore;
	private ButtonGroup btngrpEdgeScore;
	private JRadioButton rdbtnMultiplication;
	private JRadioButton rdbtnMinimum;
	private JRadioButton rdbtnCorrelation;
	
	// Connectivity
	private JPanel pnlConnectivity;
	private JLabel lblConnectivity;
	private JSlider sldrConnectivity;
	private JTextField tfConnectivity;
	
	// Node score attribute name
	private JPanel pnlNodeScoreAttribute;
	private JLabel lblNodeScoreAttribute;
	private JComboBox<String> cbbNodeScoreAttribute;
	
	// Background node score attribute name
	private JPanel pnlBackgroundNodeScoreAttribute;
	private JLabel lblBackgroundNodeScoreAttribute;
	private JComboBox cbbBackgroundNodeScoreAttribute;
	
	// Number of permutations
	private JPanel pnlPermutations;
	private JLabel lblPermutations;
	private JTextField tfPermutations;
	
	// Absent Node Score
	private JPanel pnlAbsentNodeScore;
	private JLabel lblAbsentNodeScore;
	private ButtonGroup btngrpAbsentNodeScore;
	private JRadioButton rdbtnIgnore;
	private JRadioButton rdbtnAsAverage;
	
	// Submit
	JButton btnSubmit;
	
	// Error
	private JPanel pnlError;
	private JLabel lblError;
	
	public OptionsPanel(MoBaSUtilities utils)
	{
		this.utils = utils;
		
		
		// Get node attribute columns
		Object[] attributesRaw = utils.getApplicationManager().getCurrentNetwork().getDefaultNodeTable().getColumns().toArray();
		String[] attributes = new String[attributesRaw.length];
		for(int i = 0; i < attributes.length; i++)
		{
			attributes[i] = ((CyColumn)attributesRaw[i]).getName();
		}
		
		// Get edge attribute columns
		Object[] edgeAttributesRaw = utils.getApplicationManager().getCurrentNetwork().getDefaultEdgeTable().getColumns().toArray();
		String[] edgeAttributes = new String[edgeAttributesRaw.length];
		for(int i = 0; i < edgeAttributes.length; i++)
		{
			edgeAttributes[i] = ((CyColumn)edgeAttributesRaw[i]).getName();
		}
		
		// Project identifier
		pnlProjectName = new JPanel(new FlowLayout());
		
		lblProjectName = new JLabel("Project name: ");
		pnlProjectName.add(lblProjectName);
		
		tfProjectName = new JTextField();
		tfProjectName.setColumns(10);
		pnlProjectName.add(tfProjectName);
		
		// Node score options
		pnlNodeScore = new JPanel(new FlowLayout());
		
		lblNodeScore = new JLabel("Node Score Method:");
		pnlNodeScore.add(lblNodeScore);
		
		btngrpNodeScore = new ButtonGroup();
		
		rdbtnPValues = new JRadioButton("P-Values", true);
		rdbtnPValues.setActionCommand("P_VALUES");
		btngrpNodeScore.add(rdbtnPValues);
		pnlNodeScore.add(rdbtnPValues);
		
		rdbtnFoldChange = new JRadioButton("Fold Change");
		rdbtnFoldChange.setActionCommand("FOLD_CHANGE");
		btngrpNodeScore.add(rdbtnFoldChange);
		pnlNodeScore.add(rdbtnFoldChange);
		
		
		// Edge score options
		pnlEdgeScore = new JPanel(new FlowLayout());
		
		lblEdgeScore = new JLabel("Edge Score Method:");
		pnlEdgeScore.add(lblEdgeScore);
		
		btngrpEdgeScore = new ButtonGroup();
		
		rdbtnMultiplication = new JRadioButton("Multiplication", true);
		rdbtnMultiplication.setActionCommand("MULTIPLICATION");
		btngrpEdgeScore.add(rdbtnMultiplication);
		pnlEdgeScore.add(rdbtnMultiplication);
		
		rdbtnMinimum = new JRadioButton("Minimum");
		rdbtnMinimum.setActionCommand("MINIMUM");
		btngrpEdgeScore.add(rdbtnMinimum);
		pnlEdgeScore.add(rdbtnMinimum);
		
		rdbtnCorrelation = new JRadioButton("Correlation");
		rdbtnCorrelation.setActionCommand("CORRELATION");
		btngrpEdgeScore.add(rdbtnCorrelation);   //////////// Uncomment these two lines to bring back
		pnlEdgeScore.add(rdbtnCorrelation);      //////////// the correlation option.
		
		
		// Connectivity
		pnlConnectivity = new JPanel(new FlowLayout());
		
		lblConnectivity = new JLabel("Connectivity");
		pnlConnectivity.add(lblConnectivity);
		
		sldrConnectivity = new JSlider(1, 100);
		sldrConnectivity.setPaintTicks(true);
		sldrConnectivity.setOrientation(SwingConstants.HORIZONTAL);
		sldrConnectivity.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent evt)
			{
				tfConnectivity.setText((((JSlider)evt.getSource()).getValue() / 100.0) + "");
			}
			
		});
		pnlConnectivity.add(sldrConnectivity);
		
		tfConnectivity = new JTextField((sldrConnectivity.getValue() / 100.0) + "");
		tfConnectivity.setColumns(10);
		tfConnectivity.setEditable(false);
		pnlConnectivity.add(tfConnectivity);
		
		
		// Node score attribute name
		pnlNodeScoreAttribute = new JPanel(new FlowLayout());
		
		lblNodeScoreAttribute = new JLabel("Node Score Attribute:");
		pnlNodeScoreAttribute.add(lblNodeScoreAttribute);
		
		cbbNodeScoreAttribute = new JComboBox<String>(attributes);
		pnlNodeScoreAttribute.add(cbbNodeScoreAttribute);
		
		
		// Background node score attribute name
		pnlBackgroundNodeScoreAttribute = new JPanel(new FlowLayout());
		
		lblBackgroundNodeScoreAttribute = new JLabel("Background Node Score Attribute:");
		pnlBackgroundNodeScoreAttribute.add(lblBackgroundNodeScoreAttribute);
		
		String[] attributesWithNoneOption = new String[attributes.length + 1];
		System.arraycopy(attributes, 0, attributesWithNoneOption, 1, attributes.length);
		attributesWithNoneOption[0] = "None";
		
		cbbBackgroundNodeScoreAttribute = new JComboBox(attributesWithNoneOption);
		pnlBackgroundNodeScoreAttribute.add(cbbBackgroundNodeScoreAttribute);
		
		
		// Number of permutations
		pnlPermutations = new JPanel(new FlowLayout());
		
		lblPermutations = new JLabel("Number of permutations:");
		pnlPermutations.add(lblPermutations);
		
		
		tfPermutations = new JTextField();
		tfPermutations.setColumns(10);
		tfPermutations.setText("10");
		pnlPermutations.add(tfPermutations);
		
		lblPermutations.setLabelFor(tfPermutations);
		
		
		// Absent Node Score
		pnlAbsentNodeScore = new JPanel(new FlowLayout());
		
		lblAbsentNodeScore = new JLabel("Missing Node Score:");
		pnlAbsentNodeScore.add(lblAbsentNodeScore);
		
		btngrpAbsentNodeScore = new ButtonGroup();
		
		rdbtnIgnore = new JRadioButton("Ignore", true);
		rdbtnIgnore.setActionCommand("IGNORE");
		btngrpAbsentNodeScore.add(rdbtnIgnore);
		pnlAbsentNodeScore.add(rdbtnIgnore);
		
		rdbtnAsAverage = new JRadioButton("Treat as Average", false);
		rdbtnAsAverage.setActionCommand("TREAT_AS_AVERAGE");
		btngrpAbsentNodeScore.add(rdbtnAsAverage);
		pnlAbsentNodeScore.add(rdbtnAsAverage);
		
		
		// Submit
		btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(this);
		
		
		// Error
		pnlError = new JPanel(new FlowLayout());
		
		lblError = new JLabel();
		pnlError.add(lblError);
		
		
		// Add panels
		setLayout(new GridLayout(6, 1));
		
		JPanel nodeScoreOptions = new JPanel();
		JPanel edgeScoreOptions = new JPanel();
		
		add(pnlProjectName);
		add(pnlNodeScore);
		add(pnlEdgeScore);
		add(pnlConnectivity);
		add(pnlNodeScoreAttribute);
		add(pnlBackgroundNodeScoreAttribute);
		add(pnlPermutations);
		add(pnlAbsentNodeScore);
		add(btnSubmit);
		add(pnlError);
		
		setTitle("MoBaS Options");
		setMinimumSize(new Dimension(400, 200));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		// Get all parameters, and validate them
		
		String projectName = "";
		NodeScoreMethod nodeScoreMethod = null;
		EdgeScoreMethod edgeScoreMethod = null;
		int permutations = -1;
		double connectivity = 0.5;
		String nodeScoreAttribute = "";
		String backgroundNodeScoreAttribute = "";
		AbsentNodeScoreTreatment absentNodeScoreTreatment = null;
		
		boolean error = false;
		
		projectName = tfProjectName.getText();
		if(projectName.equals(""))
		{
			error = true;
			lblError.setText("Please enter a project name");
		}
		if(!error && !verifyIdentity(projectName))
		{
			error = true;
			lblError.setText("Project name already used");
		}
		
		String nodeScoreMethodRaw = btngrpNodeScore.getSelection().getActionCommand();
		switch (nodeScoreMethodRaw) {
		case "P_VALUES": nodeScoreMethod = NodeScoreMethod.P_VALUES; break;
		case "FOLD_CHANGE": nodeScoreMethod = NodeScoreMethod.FOLD_CHANGE; break;
		}
		
		String edgeScoreMethodRaw = btngrpEdgeScore.getSelection().getActionCommand();
		switch (edgeScoreMethodRaw) {
		case "MULTIPLICATION": edgeScoreMethod = EdgeScoreMethod.MULTIPLICATION; break;
		case "MINIMUM": edgeScoreMethod = EdgeScoreMethod.MINIMUM; break;
		case "CORRELATION": edgeScoreMethod = EdgeScoreMethod.CORRELATION; break;
		}
		
		String absentNodeScoreRaw = btngrpAbsentNodeScore.getSelection().getActionCommand();
		switch (absentNodeScoreRaw) {
		case "IGNORE": absentNodeScoreTreatment = AbsentNodeScoreTreatment.IGNORE; break;
		case "TREAT_AS_AVERAGE": absentNodeScoreTreatment = AbsentNodeScoreTreatment.AVERAGE; break;
		}

		
		
		connectivity = Double.parseDouble(tfConnectivity.getText());
		nodeScoreAttribute = (String)cbbNodeScoreAttribute.getSelectedItem();
		backgroundNodeScoreAttribute = (String)cbbBackgroundNodeScoreAttribute.getSelectedItem();
		
		
		String permutationsRaw = tfPermutations.getText();
		try {
			permutations = Integer.parseInt(permutationsRaw);
		}
		catch(NumberFormatException e)
		{
			error = true;
			lblError.setText("Permutations: Please enter an integer");
		}
		
		if (!error && permutations <= 0)
		{
			error = true;
			lblError.setText("Permutations: Please enter an integer greater than 0");
		}
		
		if (!error)
		{
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			mainTaskFactory = new MoBaSMainTaskFactory(utils, projectName, nodeScoreMethod, edgeScoreMethod, permutations, connectivity, nodeScoreAttribute, backgroundNodeScoreAttribute, absentNodeScoreTreatment);
			utils.getTaskManager().execute(mainTaskFactory.createTaskIterator());
		}
	}
	
	private boolean verifyIdentity(String projectName)
	{
		File mainDir = new File(System.getProperty("user.home") + "/CytoscapeConfiguration/app-data/MoBaS");
		if (mainDir.isDirectory())
		{
			String[] names = mainDir.list();
			for(int i = 0; i < names.length; i++)
			{
				if(names[i].equalsIgnoreCase(projectName))
					return false;
			}
		}
		return true;
	}
	
}
