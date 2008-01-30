
package profile;

import model.World;

/* for speed, we don't want to turn over heap space while simulating the
 * ant tournament. This aspect checks that there are no heap allocations
 * within the cflow of play. The one exception is the allocation of string buffers
 * for displaying the current score.
 */
 
aspect NoNewInRound {

	private int allocations;

	before() : call(* World.play(..)) {
		allocations = 0;
	}

	before() : cflow(call(* World.play(..))) && call(*.new(..)) &&
	              !call(java.lang.StringBuffer.new(..)) && !within(NoNewInRound){
		System.err.println("allocation during play: "+ thisJoinPointStaticPart.getSourceLocation() + " "+thisJoinPointStaticPart);
		allocations++;
	}

	after() : call(* World.play(..)) {
		if (allocations > 0)
			System.err.println("allocations per game "+allocations);
	}

}
