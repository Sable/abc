package abc.weaving.aspectinfo;

import java.util.*;
import polyglot.util.Position;
import soot.*;


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

}
