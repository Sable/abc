package abc.weaving.weaver;

import java.util.Vector;
import java.util.Hashtable;
import soot.Local;

/** Keep track of the "weaving context"
 *  - all the things that need to be filled in
 *  before the advice call can be weaved
 *  @author Ganesh Sittampalam
 */

public class WeavingContext {
    public Vector/*<Value>*/ arglist;

    // locals get stored in the residue itself
    
    // insert reflective stuff here too

    public WeavingContext(Vector arglist) {
	this.arglist=arglist;
    }
}
