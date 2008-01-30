/*
 * Created on 20-Sep-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package main;

import model.World;
import model.Cell;
import viewer.Viewer;
import javax.swing.*;
import java.io.File;
/**
 * @author Oege de Moor
 *
 */
public class Main {

    public static void main(String[] args) {
        final World w = new World();
	String redAntArg = null;
	String blackAntArg = null;
	String worldArg = null;
	try {
		for (int n = 0; n < args.length-1; n++) {
			if (args[n].equals("-red"))
				redAntArg = args[n+1];
			if (args[n].equals("-black"))
				blackAntArg = args[n+1];
			if (args[n].equals("-world"))
				worldArg = args[n+1];
		}
	} catch (ArrayIndexOutOfBoundsException i) {
		// print usage message
	}
	final File redAntFile   = redAntArg != null ? new File(redAntArg) : null;
	final File blackAntFile = blackAntArg != null ? new File(blackAntArg) : null;
	final File worldFile    = worldArg != null ? new File(worldArg) : null;
	
	// JFrame.setDefaultLookAndFeelDecorated(true);
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
				   public void run() {
					   Viewer viewer = new Viewer();
					   if (redAntFile!=null) viewer.loadAnt(model.Color.RED, redAntFile);
					   if (blackAntFile!=null) viewer.loadAnt(model.Color.BLACK, blackAntFile);
					   if (worldFile!=null) viewer.loadWorld(worldFile);
				   }
			   });
    }
}
