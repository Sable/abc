package abc.weaving.residues;

import soot.*;
import soot.jimple.Stmt;
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
	 Chain units,Stmt begin,Stmt fail,
	 WeavingContext wc) {

	return to.set(localgen,units,begin,wc,from.get());
    }
}
