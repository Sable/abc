package abc.weaving.residues;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;

/** Copy a weaving variable into another one
 *  @author Ganesh Sittampalam
 */ 

public class Copy extends Residue {
    public WeavingVar from;
    public WeavingVar to;

    public Copy(WeavingVar from,WeavingVar to) {
	this.from=from;
	this.to=to;
    }

    public String toString() {
	return "copy("+from+"->"+to+")";
    }

    public Stmt codeGen
	(SootMethod method,LocalGeneratorEx localgen,
	 Chain units,Stmt begin,Stmt fail,boolean sense,
	 WeavingContext wc) {

	if(!sense) {
	    Stmt jump=Jimple.v().newGotoStmt(fail);
	    units.insertAfter(jump,begin);
	    return jump;
	}
	return to.set(localgen,units,begin,wc,from.get());
    }
}
