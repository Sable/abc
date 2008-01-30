//Listing 9.3 Test.java: the conventional implementation

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class Test {
    public static void main(String[] args) {
	final JFrame appFrame = new JFrame();
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	final DefaultTableModel tableModel = new DefaultTableModel(4,2);
	final JTable table = new JTable(tableModel);
	appFrame.getContentPane().add(table);
	appFrame.pack();
	appFrame.setVisible(true);
	final String value = "[0,0]";
	EventQueue.invokeLater(new Runnable() {
		public void run() {
		    tableModel.setValueAt(value, 0, 0);
		}
	    });

	try {
	    EventQueue.invokeAndWait(new Runnable() {
		    public void run() {
			JOptionPane.
			    showMessageDialog(appFrame,
					      "Press OK to continue");
		    }
		});
	} catch (Exception ex) {
	    // ignore...
	}

	final int[] rowCountValueArray = new int[1];
	try {
	    EventQueue.invokeAndWait(new Runnable() {
		    public void run() {
			rowCountValueArray[0] = tableModel.getRowCount();
		    }
		});
	} catch (Exception ex) {
	    // ignore...
	}

	int rowCount = rowCountValueArray[0];
	System.out.println("Row count = " + rowCount);
	final Color[] gridColorValueArray = new Color[1];
	try {
	    EventQueue.invokeAndWait(new Runnable() {
		    public void run() {
			gridColorValueArray[0] = table.getGridColor();
		    }
		});
	} catch (Exception ex) {
	    // ignore...
	}
	Color gridColor = gridColorValueArray[0];
	System.out.println("Grid color = " + gridColor);
    }
}
