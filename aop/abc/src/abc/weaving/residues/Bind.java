package abc.weaving.residues;

import java.util.Vector;
import soot.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Bind a context value to a local or argument
 *  @author Ganesh Sittampalam
 *  @date 30-Apr-04
 */ 

public class Bind extends Residue {
    public ContextValue value;
    public WeavingVar variable;

    Bind(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }

    public static Residue construct(ContextValue value,Type type,WeavingVar variable) {
	if(variable.getType().equals(Scene.v().getSootClass("java.lang.Object").getType())) {
	    PolyLocalVar temp=new PolyLocalVar("box");
	    return AndResidue.construct
		(new Bind(value,temp),
		 new Box(temp,variable));
	}
	else return AndResidue.construct
		 (new CheckType(value,type),
		  new Bind(value,variable));
    }

    public String toString() {
	return "bind("+value+","+variable+")";
    }
	public Stmt codeGen(
		SootMethod method,
		LocalGeneratorEx localgen,
		Chain units,
		Stmt begin,
		Stmt fail,
		WeavingContext wc) {
	
		Value val=value.getSootValue(method,localgen);
		if(!variable.hasType() || val.getType() instanceof PrimType)
		    return variable.set(localgen,units,begin,wc,val);
		else {
		    Type type=variable.getType();
		    return variable.set
			(localgen,units,begin,wc,Jimple.v().newCastExpr(val,type));
		}
	}

}
