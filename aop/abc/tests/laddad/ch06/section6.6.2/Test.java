//Listing 6.7 Test code violating the policy

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class Test extends JFrame {
    public static void main(String[] args) {
	Test appFrame = new Test();
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	DefaultTableModel tableModel = new DefaultTableModel(4,2);
	JTable table = new JTable(tableModel);
	appFrame.getContentPane().add(table);
	appFrame.pack();
	appFrame.setVisible(true);
	System.out.println("Frame is now visible");
	tableModel.setValueAt("[0,0]", 0, 0);
	tableModel.removeRow(2);
    }
}



