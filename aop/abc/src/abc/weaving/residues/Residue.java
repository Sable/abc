package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.Stmt;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** The base class defining dynamic residues of pointcuts
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public abstract class Residue {
    /** Generate the code for this dynamic residue and insert it into
     *  "units", starting just after "begin". Jump to "fail" if the residue
     *  fails. Return the final statement that was inserted.
     */
    // make this abstract once everything implements it
    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {
	throw new RuntimeException("residue not implemented for "+this);
    }

    /** Must provide a toString method */
    public abstract String toString();

}
