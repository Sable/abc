package abc.weaving.matching;

import java.util.*;
import soot.*;
import soot.jimple.*;
import abc.weaving.residues.*;

/** A "body" shadow match
 *  @author Ganesh Sittampalam
 */

public abstract class BodyShadowMatch extends ShadowMatch {
    protected BodyShadowMatch(SootMethod container) {
	super(container);
    }

    public ShadowMatch getEnclosing() {
	return this;
    }

    public ContextValue getTargetContextValue() {
	return getThisContextValue();
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	int count=container.getParameterCount();
	Vector ret=new Vector(count);
	ret.setSize(count);
	Iterator stmtsIt=container.getActiveBody().getUnits().iterator();
	// how much is the parameter list offset from the index into the args vector?
	int offset=container.isStatic() ? 0 : 1; 
	while(stmtsIt.hasNext()) {
	    Stmt stmt=(Stmt) stmtsIt.next();
	    if(!(stmt instanceof IdentityStmt)) return ret;
	    IdentityStmt istmt=(IdentityStmt) stmt;
	    Value right=istmt.getRightOp();
	    if(!(right instanceof ParameterRef)) continue;
	    ParameterRef param=(ParameterRef) right;
	    ret.set(param.getIndex(),new JimpleValue(istmt.getLeftOp()));
	}

	return ret;
    }
}
