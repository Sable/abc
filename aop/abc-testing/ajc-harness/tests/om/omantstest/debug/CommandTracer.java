
package debug;

import command.*;
import model.Ant;
import java.io.*;

/** trace the execution of individual commands */
aspect CommandTracer {

	PrintStream dump;
	
	void setDump(String dumpName) {
		try { 
			dump = new PrintStream(new FileOutputStream(dumpName));
		} catch (IOException e) {
			System.err.println("Could not open "+dumpName+" for tracing output.");
			dump = System.err;
		}
	}
	
	before (String[] as) : execution(* main.Main.main(..)) && args(as) {
		if (as.length == 0)
			dump = System.err;
		else
			setDump(as[0]); 
	}
	
	after(Command c, Ant a) : call(void Command.step(Ant)) && target(c) && args(a) {
		dump.println(c);
		dump.println("   " + a);
	}
	
}
