package edu.cwru.eecs.koyuturk.internal;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Class to print an object to a window, used displaying simple messages.
 * @author davidmiron
 *
 */
public class Dump extends JFrame {

	private static int dumpNum = 0;
	
	public Dump (Object obj)
	{
		dumpNum++;
		setLayout(new FlowLayout());
		
		add(new JLabel(obj.toString()));
		setMinimumSize(new Dimension(400, 200));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		
	}
	
	public Dump(JPanel panel)
	{
		setContentPane(panel);
		
		setTitle("A JPanel");
		setMinimumSize(new Dimension(400, 200));
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	
	
}
