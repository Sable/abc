package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A "dynamic" residue that can never match. 
 *  Intended for convenience during generation and residue analysis process.
 *  Can also use null to represent this; need to decide whether keeping
 *  it is worthwhile.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public class NeverMatch extends Residue {
    // is this worthwhile? (save on heap turnover)
    public final static NeverMatch v=new NeverMatch();

    public static boolean neverMatches(Residue r) {
	return r==null || r instanceof NeverMatch;
    }

    public String toString() {
	return "never";
    }

    // this ought not to get called
    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	if(!sense) return begin;

	Stmt abort=Jimple.v().newGotoStmt(fail);
	units.insertAfter(abort,begin);
	return abort;
    }
}
