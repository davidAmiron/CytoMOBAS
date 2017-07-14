package edu.cwru.eecs.koyuturk.internal;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

public class ResultsPanel extends JPanel implements CytoPanelComponent{
	
	private static final long serialVersionUID = 159897218259823197L;
	
	private static final String MOBAS_DIR = System.getProperty("user.home") + "/CytoscapeConfiguration/app-data/MoBaS";
	
	private final String TITLE = "MoBaS Results";
	
	public static int TABLE_ROWS_NUM = 100;

	
	// Project identifyer
	private JPanel pnlIdentifyer;
	private JLabel lblIdentifyer;
	private JTextField tfIdentifyer;
	
	// Number of subnets to show
	private JPanel pnlSubnetsNum;
	private JLabel lblSubnetsNum;
	private JTextField tfSubnetsNum;
	
	// Button to generate chart
	private JButton btnGenerateChart;
	
	// Button to save the chart to a file
	private JButton btnSavePlot;
	
	// Area for notices
	private JTextArea taInfo;
	
	// ScrollPane for the table
	private JScrollPane scrlTable;
	
	// Used to get current network
	private CyApplicationManager appManager;
	
	
	public ResultsPanel(CyApplicationManager appManager)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Project identifyer
		pnlIdentifyer = new JPanel(new FlowLayout());
		
		lblIdentifyer = new JLabel("Project name: ");
		pnlIdentifyer.add(lblIdentifyer);
		
		tfIdentifyer = new JTextField();
		tfIdentifyer.setColumns(15);
		pnlIdentifyer.add(tfIdentifyer);
		
		add(pnlIdentifyer);
		
		// Number of subnets to show
		pnlSubnetsNum = new JPanel(new FlowLayout());
		
		lblSubnetsNum = new JLabel("Number of subnetworks to show: ");
		pnlSubnetsNum.add(lblSubnetsNum);
		
		tfSubnetsNum = new JTextField("5");
		tfSubnetsNum.setColumns(5);
		pnlSubnetsNum.add(tfSubnetsNum);
		
		add(pnlSubnetsNum);
		
		// Button to generate chart
		btnGenerateChart = new JButton("Generate Plot");
		btnGenerateChart.addActionListener(new GeneratePlotListener());
		
		add(btnGenerateChart);
		
		// Button to save the chart to a file
		btnSavePlot = new JButton("Save Plot");
		btnSavePlot.addActionListener(new SavePlotListener());
		
		add(btnSavePlot);
		
		// Area for notices
		taInfo = new JTextArea("");
		taInfo.setEnabled(false);
		taInfo.setLineWrap(true);
		taInfo.setBackground(getBackground());
		taInfo.setAlignmentY(CENTER_ALIGNMENT);		
		
		add(taInfo);
		
		// The ScrollPane table
		scrlTable = new JScrollPane();
		add(scrlTable);
				
		this.appManager = appManager;
	}
	
	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	public Icon getIcon() {
		return null;
	}

	public String getTitle() {
		return this.TITLE;
	}

	private class GeneratePlotListener implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JFrame plotWindow = new JFrame();
			JPanel plot = generatePlot();
			plotWindow.setContentPane(plot);
			
			plotWindow.setTitle(getProjectName() + " Results");
			plotWindow.setMinimumSize(new Dimension(400, 200));
			plotWindow.pack();
			plotWindow.setLocationRelativeTo(null);
			plotWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			plotWindow.setVisible(true);
			plotWindow.toFront();
			
			updateSubnetsTable();
		}
	}
	
	public void setProjectName(String projectName) {
		this.tfIdentifyer.setText(projectName);
	}
	
	public void generateGraph() {
		this.btnGenerateChart.doClick();
	}
	
	private class SavePlotListener implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			File outputFile = new File(MOBAS_DIR + "/" + getProjectName() + "/plot.png");
			if(verifyProjectName(getProjectName()))
			{
				JPanel plot = generatePlot();
				plot.setSize(700, 450);
				BufferedImage buffedImg = new BufferedImage(plot.getWidth(), plot.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = buffedImg.createGraphics();
				plot.paint(g2d);
				g2d.dispose();
				try
				{
					ImageIO.write(buffedImg, "PNG", outputFile);
					taInfo.setText("File saved to " + outputFile.getPath());
				} catch (IOException e) {}
			}
			else
			{
				taInfo.setText("Invalid project name");
			}
		}
	}
	
	private class RowSelectedListener implements ListSelectionListener {
		
		private JTable table;
		
		public RowSelectedListener(JTable table) {
			this.table = table;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent event) {
			selectNodes(table.getSelectedRow() + 1);
		}
	}
	
	private JPanel generatePlot()
	{
		// Main subnet scores setup
		CategoryDataset mainSubnetScores = getMainSubnetScores();
		
		// The main chart object
		JFreeChart chart = ChartFactory.createLineChart(getProjectName(), "Rank", "Score", mainSubnetScores, PlotOrientation.VERTICAL, true, false, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		LineAndShapeRenderer mainSubnetScoresRenderer = (LineAndShapeRenderer)plot.getRenderer();
		mainSubnetScoresRenderer.setBaseShapesVisible(true);
		
		
		// Permuted subnet scores
		DefaultBoxAndWhiskerCategoryDataset permutedSubnetScores = getPermutedSubnetScores();
		BoxAndWhiskerRenderer permutedSubnetRenderer = new BoxAndWhiskerRenderer();
		permutedSubnetRenderer.setMeanVisible(false);
		
		plot.setDataset(1, permutedSubnetScores);
		plot.setRenderer(1, permutedSubnetRenderer);
		
		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}
	
	private DefaultCategoryDataset getMainSubnetScores()
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String projectName = getProjectName();
		String mainSubnetScoresFile = MOBAS_DIR + "/" + projectName + "/score-data/mainScores.txt";
		File file = new File(mainSubnetScoresFile);
		String line;
		int rank = 1;
		int count = 0;
		int numToDisplay = Integer.parseInt(tfSubnetsNum.getText());
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null && count < numToDisplay)
			{
				dataset.addValue(Double.parseDouble(line.split("\t")[2]), "Subnetworks in Origional Network", rank + "");
				rank++;
				count++;
			}
			reader.close();
			
		} catch (IOException e) {}
		return dataset;
	}
	
	private DefaultBoxAndWhiskerCategoryDataset getPermutedSubnetScores()
	{
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		String identifyer = getProjectName();
		String permutedSubnetScoresFile = MOBAS_DIR + "/" + identifyer + "/score-data/permutationScores.txt";
		File file = new File(permutedSubnetScoresFile);
		int numToDisplay = getNumToDisplay();
		String line;
		int lineCounter = 0;
		ArrayList<ArrayList<Double>> scores = new ArrayList<ArrayList<Double>>();
		String [] allScores;

		// Read in data from file
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null)
			{
				allScores = line.split("\t");
				scores.add(new ArrayList<Double>());
				for(int i = 0; i < numToDisplay; i++)
				{
					scores.get(lineCounter).add(Double.parseDouble(allScores[i + 1])); // i + 1 to account for "Permuted Network #" before score data
				}
				lineCounter++;
			}
			reader.close();
		} catch (IOException e) {}

		// Organize into graph data
		ArrayList<Double> subnetRankData = new ArrayList<Double>();
		int rank = 1;
		for(int i = 0; i < numToDisplay; i++)
		{
			subnetRankData.clear();
			for(int j = 0; j < scores.size(); j++)
			{
				subnetRankData.add(scores.get(j).get(i));
			}
			dataset.add(BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(subnetRankData), "Subnetworks in Permuted Network", rank + "");
			rank++;
		}
		
		
		return dataset;
	}
	
	private boolean verifyProjectName(String projectName)
	{
		if(projectName.equals(""))
			return false;
		File mainDir = new File(MOBAS_DIR);
		if (mainDir.isDirectory())
		{
			String[] names = mainDir.list();
			for(int i = 0; i < names.length; i++)
			{
				if(names[i].equalsIgnoreCase(projectName))
					return true;
			}
		}
		return false;
	}
	
	private String getProjectName()
	{
		return tfIdentifyer.getText();
	}
	
	private int getNumToDisplay()
	{
		return Integer.parseInt(tfSubnetsNum.getText());
	}
	
	
	private void updateSubnetsTable()
	{
		// The column names for the output table
		String[] columnNames = {"Rank", "Size", "Score", "Q Value"};
		
		// The data to pass into the table
		Object[][] data = new Object[TABLE_ROWS_NUM][4];
		
		String projectName = getProjectName();
		String mainSubnetScoresFile = MOBAS_DIR + "/" + projectName + "/score-data/mainScores.txt";
		File file = new File(mainSubnetScoresFile);
		String line;
		int count = 0;
		
		// Get all Q Scores
		ArrayList<ArrayList<Double>> permutationData = readPermutationData();
		
		int rank;
		int size;
		double score;
		double qValue;
		if (file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while((line = reader.readLine()) != null && count < TABLE_ROWS_NUM)
				{
					// For each subnetwork
					rank = count + 1;
					size = Integer.parseInt(line.split("\t")[1]);
					score = Double.parseDouble(line.split("\t")[2]);
					qValue = getQScore(score, rank, permutationData);
					
					data[count][0] = rank;
					data[count][1] = size;
					data[count][2] = score;
					data[count][3] = qValue;
					
					count++;
				}
				reader.close();
				
				JTable table = new JTable(data, columnNames);
				//table.sizeColumnsToFit(3);
				TableColumn tableColumn = new TableColumn();
				tableColumn.setWidth(30);
				table.getTableHeader().setResizingColumn(tableColumn);
				table.doLayout();
				table.getSelectionModel().addListSelectionListener(new RowSelectedListener(table));
				
				//resetPanelViewWithTable(table);
				scrlTable.getViewport().setView(table);
				taInfo.setText("");
				
			} catch (IOException e) {
				
				// Return null if read fails
				taInfo.setText("The results file could not be read.");
			}
		}
		else
		{
			// The file does not exist
			taInfo.setText("Enter a valid project name.");
		}
		
	}
	
	private void selectNodes(int rank) {

		// Read subnet file for that rank
		String subnetFile = MOBAS_DIR + "/" + getProjectName() + "/subnets/subnet_" + rank + ".txt";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(subnetFile));
			HashSet<String> nodeNames = new HashSet<String>();
			
			String line;
			while((line = reader.readLine()) != null) {
				String[] split = line.split("\t");
				nodeNames.add(split[0]);
				nodeNames.add(split[1]);
			}
			
			CyTable nodeTable = appManager.getCurrentNetwork().getDefaultNodeTable();
			for (String node: nodeNames)
				nodeTable.getMatchingRows("name", node).iterator().next().set("selected", true);
			
			reader.close();
		} catch (IOException e) {
			new Dump("Error: cannot read file.\n" + subnetFile);
		}
		
	}
	
	private ArrayList<ArrayList<Double>> readPermutationData() {

		String permutationScoresFile = MOBAS_DIR + "/" + getProjectName() + "/score-data/permutationScores.txt";
		ArrayList<ArrayList<Double>> permutationData = new ArrayList<ArrayList<Double>>();
		
		try {

			BufferedReader reader = new BufferedReader(new FileReader(permutationScoresFile));
			
			for (int i = 0; i < TABLE_ROWS_NUM; i++)
				permutationData.add(new ArrayList<Double>());
			
			// Add in the data
			String line;
			String[] split;
			int readNum;
			int missing;
			while ((line = reader.readLine()) != null) {
				split = line.split("\t");
				if (split.length - 1 < TABLE_ROWS_NUM) {
					readNum = split.length - 1;
					missing = TABLE_ROWS_NUM - readNum;
				} else {
					readNum = TABLE_ROWS_NUM;
					missing = 0;
				}
				for (int i = 1; i < readNum + 1; i++) {
					permutationData.get(i - 1).add(Double.parseDouble(split[i]));
					for (int j = 0; j < missing; j++)
						permutationData.get(i - 1).add(null);
				}
			}
			
			return permutationData;
			
		} catch (IOException e) {
			new Dump("Unable to read file.\n" + permutationScoresFile);
			return null;
		}
		
	}
	
	private double getQScore(double score, int rank, ArrayList<ArrayList<Double>> permutationData) {
		
		int numOfScoresAbove = 0;
		for (int i = 0; i < rank; i++) {
			for (Double permutationScore: permutationData.get(i))
				if (permutationScore == null)
					continue;
				else if (permutationScore >= score)
					numOfScoresAbove++;
		}
		return (double)numOfScoresAbove / (rank * permutationData.size());
	}
	
}
