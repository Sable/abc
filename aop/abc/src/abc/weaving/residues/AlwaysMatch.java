package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.Stmt;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A "dynamic" residue that can never match. 
 *  Intended for convenience during generation and residue analysis process.
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public class AlwaysMatch extends AbstractResidue {
    // is this worthwhile? (save on heap turnover)
    public final static AlwaysMatch v=new AlwaysMatch();

    public String toString() {
	return "always";
    }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,WeavingContext wc) {

	return begin;
    }

}
