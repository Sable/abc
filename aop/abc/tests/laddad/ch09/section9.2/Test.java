//Listing 9.1 A test program showing incorrect usage of the UI update call

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class Test {
    public static void main(String[] args) {
	JFrame appFrame = new JFrame();
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	DefaultTableModel tableModel = new DefaultTableModel(4,2);
	JTable table = new JTable(tableModel);
	appFrame.getContentPane().add(table);
	appFrame.pack();
	appFrame.setVisible(true);
	String value = "[0,0]";
	tableModel.setValueAt(value, 0, 0);
	JOptionPane.showMessageDialog(appFrame,
				      "Press OK to continue");
	int rowCount = tableModel.getRowCount();
	System.out.println("Row count = " + rowCount);
	Color gridColor = table.getGridColor();
	System.out.println("Grid color = " + gridColor);
    }
}

