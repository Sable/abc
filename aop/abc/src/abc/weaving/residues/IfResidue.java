package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** The dynamic residue of an if(...) pointcut
 *  @author Ganesh Sittampalam
 */ 

public class IfResidue extends Residue {
    private SootMethod impl;
    private List/*<WeavingVar>*/ args;

    public IfResidue(SootMethod impl,List args) {
	this.impl=impl;
	this.args=args;
    }

    public String toString() {
	return "if(...)";
    }

    public Stmt codeGen
	(SootMethod method,LocalGeneratorEx localgen,
	 Chain units,Stmt begin,Stmt fail,
	 WeavingContext wc) {

	List actuals=new Vector(args.size());
	Iterator it=args.iterator();
	while(it.hasNext())
	    actuals.add(((WeavingVar) (it.next())).get());
	Local ifresult=localgen.generateLocal(BooleanType.v(),"ifresult");
	InvokeExpr ifcall=Jimple.v().newStaticInvokeExpr(impl,actuals);
	AssignStmt assign=Jimple.v().newAssignStmt(ifresult,ifcall);
	IfStmt abort=Jimple.v().newIfStmt(Jimple.v().newEqExpr(ifresult,IntConstant.v(0)),fail);
	units.insertAfter(assign,begin);
	units.insertAfter(abort,assign);
	return abort;
    }
}
