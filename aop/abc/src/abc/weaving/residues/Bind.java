package abc.weaving.residues;

import java.util.Vector;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.residues.Residue.Bindings;
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

    // FIXME : restructure WeavingVars and delegate this all to that. In fact,
    // redesign ContextValue/WeavingVar structure so it's all uniform.
    // I *think* the type parameter is redundant except in the case of CflowSetup
    // where it will be the primitive type, but the variable will have the boxed type,
    // and mustBox will be true. In other boxing situations they will also differ, but
    // we don't currently inspect the type anyway.
    public static Residue construct(ContextValue value,Type type,WeavingVar variable) {
	if(variable.mustBox()) {
	    if(!value.getSootType().equals(type)) return NeverMatch.v; 
	    PolyLocalVar temp=new PolyLocalVar("box");
	    PolyLocalVar temp2=new PolyLocalVar("boxed");
	    return AndResidue.construct
		(AndResidue.construct
		 (new Bind(value,temp),
		  new Box(temp,temp2)),
		 new Copy(temp2,variable));
	}
	if(variable.maybeBox()) { // && value.getSootType() instanceof PrimType) {
	    PolyLocalVar temp=new PolyLocalVar("box");
	    PolyLocalVar temp2=new PolyLocalVar("boxed");
	    return AndResidue.construct
		(AndResidue.construct
		 (new Bind(value,temp),
		  new Box(temp,temp2)),
		 new Copy(temp2,variable));
	}
	else return AndResidue.construct
		 (CheckType.construct(value,type),
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
	
		Value val=value.getSootValue();
		if(!variable.hasType())
		    // PolyLocalVar
		    return variable.set(localgen,units,begin,wc,val);

		Type to=variable.getType();
		Type from=val.getType();

		if(from.equals(to))
		    return variable.set(localgen,units,begin,wc,val);

		return variable.set
		    (localgen,units,begin,wc,Jimple.v().newCastExpr(val,to));
	}

	/**
	 * If this Bind binds an advice-formal,
	 * add the binding to the Bindings object
	 */
	public void getAdviceFormalBindings(Bindings bindings) {
		if (variable instanceof AdviceFormal) {
			AdviceFormal formal = (AdviceFormal) variable;
			Value val = value.getSootValue();
			if (val instanceof Local) {
				Local local = (Local) val;
				//debug(" Binding: " + local.getName() + " => " + formal.pos);
				
				bindings.set(formal.pos, local);
			} else {
				throw new InternalError(
				"Expecting bound values to be of type Local: "
					+ val
					+ " (came from: "
					+ this
					+ ")");
			}
		} else {
		//	throw new InternalError("Expecting bound variables to be of type adviceFormal: " + bind.variable );
		}
	}
	
	/**
	 * Replace this Bind with a BindMaskResidue containing this Bind 
	 * if appropriate.
	 */
	public Residue restructureToCreateBindingsMask(soot.Local bindingsMaskLocal, Bindings bindings) {
		if (variable instanceof AdviceFormal) {
			AdviceFormal formal = (AdviceFormal) variable;
			Value val = value.getSootValue();
			//if (val instanceof Local) {
			Local local = (Local) val;
			int index=bindings.lastIndexOf(local);
			if (bindings.getMaskBits(index)>0) {
				int mask=bindings.getMaskValue(local);
				return new BindMaskResidue(this, bindingsMaskLocal, mask);
			}
		}	
		return this;
	}
}
