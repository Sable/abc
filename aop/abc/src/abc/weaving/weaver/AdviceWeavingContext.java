package abc.weaving.weaver;

import java.util.Vector;
import java.util.Hashtable;
import soot.Local;

/** Keep track of the "weaving context" for
 *  a concrete advice decl
 *  @author Ganesh Sittampalam
 */

public class AdviceWeavingContext extends WeavingContext {
    public Vector/*<Value>*/ arglist;
    public Local aspectinstance;


    // locals get stored in the residue itself
    
    // insert reflective stuff here too

    public AdviceWeavingContext(Vector arglist) {
	this.arglist=arglist;
	this.aspectinstance=null;
    }
}
