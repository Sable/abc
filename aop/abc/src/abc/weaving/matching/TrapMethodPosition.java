package abc.weaving.matching;

import soot.*;

/** Specifies matching at a particular exception trap
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class TrapMethodPosition extends MethodPosition {
    private Trap trap;
     
    public TrapMethodPosition(SootMethod container,Trap trap) {
	super(container);
	this.trap=trap;
    }

    public Trap getTrap() {
	return trap;
    }
}
