package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A "dynamic" residue that always matches.
 *  Intended for convenience during generation and residue analysis process.
 *  @author Ganesh Sittampalam
 */ 

public class AlwaysMatch extends Residue {
    // is this worthwhile? (save on heap turnover)
    public final static AlwaysMatch v=new AlwaysMatch();
    public static AlwaysMatch v() {
	return v;
    }

    public String toString() {
	return "always";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	if(sense) return begin;

	Stmt abort=Jimple.v().newGotoStmt(fail);
	units.insertAfter(abort,begin);
	return abort;
    }

}
