package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.Chain;
import abc.soot.util.Restructure;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** Box a weaving variable (if necessary) into another one
 *  @author Ganesh Sittampalam
 */ 

public class Box extends Residue {
    public WeavingVar from;
    public WeavingVar to;

    public Box(WeavingVar from,WeavingVar to) {
	this.from=from;
	this.to=to;
    }

    public String toString() {
	return "box("+from+"->"+to+")";
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

	Type type=from.getType();
	if(type instanceof PrimType) {
	    SootClass boxClass=Restructure.JavaTypeInfo.getBoxingClass(type);
	    Stmt newStmt=to.set(localgen,units,begin,wc,
				Jimple.v().newNewExpr(boxClass.getType()));
	    List paramTypeList=new ArrayList(1);
	    paramTypeList.add(type);
	    Stmt constrStmt=Jimple.v().newInvokeStmt
		(Jimple.v().newSpecialInvokeExpr
		 (to.get(),boxClass.getMethod(SootMethod.constructorName,paramTypeList),from.get()));
	    units.insertAfter(constrStmt,newStmt);
	    
	    return constrStmt;

	} else {
	    return to.set(localgen,units,begin,wc,from.get());
	}
    }

}
