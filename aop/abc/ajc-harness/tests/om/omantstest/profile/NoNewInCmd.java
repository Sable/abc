
package profile;

import model.World;
import command.Command;

/* for speed, we don't want to turn over heap space while simulating the
 * ant tournament. This aspect checks that there are no heap allocations
 * within the cflow of play. The one exception is the allocation of string buffers
 * for displaying the current score.
 */
 
aspect NoNewInCmd {
	
	    before(Command c) : cflow(call(* Command.step(..)) && target(c)) 
                               && call(*.new(..)) && !call(java.lang.StringBuffer.new(..))
                               && !within(NoNewInCmd){
		System.err.println("allocation during command: "+ c +" at " + thisJoinPointStaticPart.getSourceLocation() + " "+thisJoinPointStaticPart);
	}


}
