package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.util.Chain;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;
import java.util.*;

/** The dynamic residue of an if(...) pointcut
 *  @author Ganesh Sittampalam
 */ 

public class IfResidue extends Residue {
    private SootMethod impl;
    private List/*<WeavingVar>*/ args;

    private IfResidue(SootMethod impl,List args) {
	this.impl=impl;
	this.args=args;
    }

    public static IfResidue construct(SootMethod impl,List args) {
	return new IfResidue(impl,args);
    }

    public String toString() {
	return "if(...)";
    }

    public Stmt codeGen
	(SootMethod method,LocalGeneratorEx localgen,
	 Chain units,Stmt begin,Stmt fail,boolean sense,
	 WeavingContext wc) {

	List actuals=new Vector(args.size());
	Iterator it=args.iterator();
	while(it.hasNext())
	    actuals.add(((WeavingVar) (it.next())).get());
	Local ifresult=localgen.generateLocal(BooleanType.v(),"ifresult");
	InvokeExpr ifcall=Jimple.v().newStaticInvokeExpr(impl.makeRef(),actuals);
	AssignStmt assign=Jimple.v().newAssignStmt(ifresult,ifcall);
	Expr test;
	if(sense) test=Jimple.v().newEqExpr(ifresult,IntConstant.v(0));
	else test=Jimple.v().newNeExpr(ifresult,IntConstant.v(0));
	IfStmt abort=Jimple.v().newIfStmt(test,fail);
	units.insertAfter(assign,begin);
	units.insertAfter(abort,assign);
	return abort;
    }
}
