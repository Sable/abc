package abc.weaving.residues;

import soot.SootMethod;
import soot.util.Chain;
import soot.jimple.Stmt;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** A convenient base class for residues
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public abstract class AbstractResidue implements Residue {
    /** Must provide a toString method */
    public abstract String toString();

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	throw new RuntimeException("residue not implemented for "+this);

    }


}
