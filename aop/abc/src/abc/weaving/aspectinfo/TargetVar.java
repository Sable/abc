package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** Handler for <code>target</code> condition pointcut with a variable argument. */
public class TargetVar extends TargetAny {
    private Var var;

    public TargetVar(Var var,Position pos) {
	super(pos);
	this.var = var;
    }

    /** Get the pointcut variable that is bound by this
     *  <code>target</code> pointcut.
     */
    public Var getVar() {
	return var;
    }

    public String toString() {
	return "target("+var+")";
    }

    protected Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),we.getWeavingVar(var));
    }

    protected Pointcut inline(Hashtable renameEnv,
			      Hashtable typeEnv,
			      Aspect context) {
	Var var=this.var.rename(renameEnv);

	if(var==this.var) return this;
	else return new TargetVar(var,getPosition());
    }

    public void getFreeVars(Set/*<String>*/ result) {
	result.add(var.getName());
    }

    public boolean equivalent(Pointcut otherpc) {
	if (otherpc instanceof TargetVar) {
	    othervar = ((TargetVar)otherpc).getVar();
	    return (othervar.equals(var));
	} else return false;
    }

}
