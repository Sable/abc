package abc.weaving.weaver;

import java.util.Vector;

/** Keep track of the "weaving context"
 *  - all the things that need to be filled in
 *  before the advice call can be weaved
 *  @author Ganesh Sittampalam
 */

public class WeavingContext {
    public Vector/*<Value>*/ arglist;
    
    // insert pointcut locals and reflective stuff here too


    public WeavingContext(Vector arglist) {
	this.arglist=arglist;
    }
}
