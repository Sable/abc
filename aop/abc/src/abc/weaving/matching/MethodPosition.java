package abc.weaving.matching;

import soot.SootMethod;

/** Used to specify that we are currently looking at the "whole" method
 *  during matching
 *  @author Ganesh Sittampalam
 *  @date 29-Apr-04                                  
 */
public abstract class MethodPosition {
    private SootMethod container;

    public MethodPosition(SootMethod container) {
	this.container=container;
    }

    public SootMethod getContainer() {
	return container;
    }
}
