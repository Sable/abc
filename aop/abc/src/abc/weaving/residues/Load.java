package abc.weaving.residues;

import java.util.Vector;
import soot.*;
import soot.util.Chain;
import soot.jimple.Stmt;
import soot.jimple.Jimple;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Load a context value into a local or argument,
 *  without boxing or casting
 *  @author Ganesh Sittampalam
 */ 

public class Load extends Residue {
    public ContextValue value;
    public WeavingVar variable;

    public Load(ContextValue value,WeavingVar variable) {
	this.value=value;
	this.variable=variable;
    }

    public String toString() {
	return "load("+value+","+variable+")";
    }
	public Stmt codeGen(
		SootMethod method,
		LocalGeneratorEx localgen,
		Chain units,
		Stmt begin,
		Stmt fail,
		WeavingContext wc) {
	    return variable.set(localgen,units,begin,wc,value.getSootValue());
	}

}
