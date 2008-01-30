//Listing 9.12 TestResponsiveness.java

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TestResponsiveness {
    public static void main(String[] args) {
	JFrame appFrame = new JFrame();
	appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	JButton sendEmailButton = new JButton("Send Emails");
	sendEmailButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    sendEmails();
		}
	    });
	appFrame.getContentPane().add(sendEmailButton);
	appFrame.pack();
	appFrame.setVisible(true);
    }

    private static void sendEmails() {
	try {
	    // simulate long execution...
	    Thread.sleep(20000);
	} catch (InterruptedException ex) {
	}
    }
}
