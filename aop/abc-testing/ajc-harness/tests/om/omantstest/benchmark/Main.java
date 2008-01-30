/*
 * Created on 20-Sep-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package benchmark;

import model.World;
import model.Color;
import java.io.*;

/**
 * @author Oege de Moor
 *
 */
public class Main {

    public static void main(String[] args) {
        final World w = new World();
        try {
	w.loadWorld(new File("../sample/worlds/sample4.world"));
	w.loadAnt(Color.BLACK,new File("../sample/ants/dunkosmiloolump-1.ant"));
	w.loadAnt(Color.RED,new File("../sample/ants/dunkosmiloolump-2.ant")); }
        catch (IOException e) { System.err.println("error: failed to open input"); }
	w.play();
    }
}
