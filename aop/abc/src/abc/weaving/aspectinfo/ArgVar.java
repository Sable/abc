package abc.weaving.aspectinfo;

import java.util.*;

import polyglot.util.Position;
import soot.*;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.*;

/** An argument pattern denoting a pointcut variable. */
public class ArgVar extends ArgAny {
    private Var var;

    public ArgVar(Var var, Position pos) {
	super(pos);
	this.var = var;
    }

    public Var getVar() {
	return var;
    }

    public String toString() {
	return var.toString();
    }

    public Residue matchesAt(WeavingEnv we,ContextValue cv) {
	return Bind.construct
	    (cv,we.getAbcType(var).getSootType(),we.getWeavingVar(var));
    }

    public Var substituteForPointcutFormal
	(Hashtable/*<String,Var>*/ renameEnv,
	 Hashtable/*<String,AbcType>*/ typeEnv,
	 Formal formal,
	 List/*<Formal>*/ newLocals,
	 List/*<CastPointcutVar>*/ newCasts,
	 Position pos) {

	Var oldvar=this.var.rename(renameEnv);
    
	AbcType actualType=(AbcType) typeEnv.get(var.getName());
	
	if(actualType==null) throw new RuntimeException(var.getName());

	if(actualType.getSootType().equals
	   (formal.getType().getSootType())) {

	    return oldvar;
	}

	String name=Pointcut.freshVar();
	Var newvar=new Var(name,pos);
	
	newLocals.add(new Formal(formal.getType(),name,pos));
	newCasts.add(new CastPointcutVar(newvar,oldvar,pos));

	return newvar;
    }

    public void getFreeVars(Set/*<String>*/ result) {
	result.add(var.getName());
    }

	/* (non-Javadoc)
	 * @see abc.weaving.aspectinfo.ArgPattern#equivalent(abc.weaving.aspectinfo.ArgPattern, java.util.Hashtable)
	 */
	public boolean equivalent(ArgPattern p, Hashtable renaming) {
		if (p instanceof ArgVar) {
			Var othervar = ((ArgVar)p).getVar(); 
			return (var.canRenameTo(othervar, renaming));
		} else return false;
	}

}
