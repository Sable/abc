package abc.weaving.matching;

import soot.*;

/** The "position" of the whole method
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */
public class WholeMethodPosition implements MethodPosition {

    /** Specify the actual method etc we are in, needed to construct
     *  the reflective information
     */
    public SootMethod container;
    public WholeMethodPosition(SootMethod container) {
	this.container=container;
    }
}
