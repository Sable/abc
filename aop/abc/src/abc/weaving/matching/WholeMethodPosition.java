package abc.weaving.matching;

import soot.*;

/** The "position" of the whole method
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */
public class WholeMethodPosition extends MethodPosition {
    public WholeMethodPosition(SootMethod container) {
	super(container);
    }
}
