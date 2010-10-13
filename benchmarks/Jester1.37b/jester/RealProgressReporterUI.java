package jester;

import java.awt.*;

import javax.swing.*;

public class RealProgressReporterUI extends JFrame {

	JProgressBar fProgressBar;
	JTextArea fTextArea;

    public static void main(String[] args)
	{
	  JFrame f = new RealProgressReporterUI();
	  f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);		
	}
	

	public RealProgressReporterUI() {
		super();
		setupFrame();
	}

	private void setupFrame() {
		setTitle("Jester Report");
		fProgressBar = new JProgressBar();
		fTextArea = new JTextArea();
		getContentPane().setLayout(new GridBagLayout());
		add(new JLabel("Progress"), 0,0,1,1,GridBagConstraints.EAST,GridBagConstraints.NONE);
		add(fProgressBar, 1,0,1,1,GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL);
		add(new JScrollPane(fTextArea), 0,1,2,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH);
		fTextArea.setEditable(false);
		pack();
		setBounds(100, 100, 600, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		setVisible(true);
	}

	private void add(Component comp, int x, int y, int w, int h, int orientation, int fill)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.gridheight = h;
		gbc.ipadx = 10;
		gbc.ipady = 5;
		
		if (fill == GridBagConstraints.BOTH)
		{
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
		}
		else if (fill == GridBagConstraints.HORIZONTAL)
		{
			gbc.weightx = 1.0;
		}
		if (fill == GridBagConstraints.VERTICAL)
		{
			gbc.weighty = 1.0;
		}
		gbc.anchor = orientation;
		gbc.fill = fill;
	    ((GridBagLayout)getContentPane().getLayout()).setConstraints(comp, gbc);
	    getContentPane().add(comp);
	}
		

}
