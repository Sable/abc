package abc.weaving.matching;

import soot.*;

/** Specifies matching at a particular exception trap
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */

public class TrapMethodPosition implements MethodPosition {
    private Trap trap;
    
    public TrapMethodPosition(Trap trap) {
	this.trap=trap;
    }

    public Trap getTrap() {
	return trap;
    }
}
