package abc.weaving.matching;

import abc.weaving.aspectinfo.*;
import abc.weaving.residues.*;

/** Declare a new local pointcut variable
 *  @author Ganesh Sittampalam
 *  @date 04-May-04
 */

public class DeclareLocal implements WeavingEnv {
    private WeavingEnv parent;
    private String name;
    private AbcType type;

    public DeclareLocal(WeavingEnv parent,String name,AbcType type) {
	this.parent=parent;
	this.name=name;
	this.type=type;
    }

    public WeavingVar getWeavingVar(Var v) {
	if(v.getName().equals(this.name)) {
	    // FIXME when we have a local declaration in residues
	    return null;
	} else {
	    return parent.getWeavingVar(v);
	}
    }

    public AbcType getAbcType(Var v) {
	if(v.getName().equals(this.name)) {
	    return type;
	} else {
	    return parent.getAbcType(v);
	}
    }
}
