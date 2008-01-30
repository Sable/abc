
package debug;

import java.io.*;

import model.World;
/** debugging output as suggested in the original contest statement */
aspect WorldDumper {
	
	static int roundNumber;
	static int maxRound = 100000;
	
    PrintStream dump;
	
	void setDump(String dumpName) {
		try { 
			dump = new PrintStream(new FileOutputStream(dumpName));
		} catch (IOException e) {
			System.err.println("Could not open "+dumpName+" for dumping worlds.");
			dump = System.err;
		}
	}
	
	public void print() {
		dump.println();
		dump.println("After round "+ roundNumber +"...");
		for (int y=0; y < World.v().getMaxY(); y++) 
		    for (int x=0; x < World.v().getMaxX(); x++) {
				dump.println(World.v().getCell(x,y).toString());
			}
	}
	  
	before (String[] as) : execution(* main.Main.main(..)) && args(as) {
		if (as.length < 2)
			dump = System.err;
		else
			setDump(as[1]); 
		if (as.length >= 3)
			maxRound = (new Integer(as[2])).intValue();
	}
	
	after() : execution (* World.readWorld(..)) {
		roundNumber = 0;
		dump.println("random seed: "+World.v().getSeed());
		print();
		roundNumber++;
	}
	
	after() : call(void World.round()) && if (roundNumber < maxRound){
		print();
		roundNumber++;
	}
}
