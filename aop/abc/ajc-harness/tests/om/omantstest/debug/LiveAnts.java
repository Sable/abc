
package debug;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import model.World;
import model.Ant;

/** check that the live ants list in World is correct */
privileged aspect LiveAnts {
	
	private void World.allLiveAnts() {
		List result = new LinkedList();
		for (int y= 0; y < maxY; y++)
				 for (int x=0; x < maxX; x++) {
					 if (grid[x][y].hasAnt())
						 if (!World.v().ants.contains(grid[x][y].getAnt()))
						 	System.out.println("missing ant in live list");
			 }
	}
	
	private void World.noDeadAnts() {
		for (Iterator antit = World.v().ants.iterator(); antit.hasNext(); ) {
			Ant ant = (Ant) antit.next();
			if (!ant.getDead())
				if ((!ant.getPosition().hasAnt())
				     ||
				     (ant.getPosition().getAnt() != ant))
				   System.out.println("dead ant in live list");
		}
	}
	
	after() : call(* World.round(..)) {
		World.v().allLiveAnts();
		World.v().noDeadAnts();
	}

}
