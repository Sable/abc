package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;

public class CflowResidue extends Residue {
    private CflowSetup setup;
    private List/*<WeavingVar>*/ vars;

    public CflowResidue(CflowSetup setup,List vars) {
	this.setup=setup;
	this.vars=vars;
    }

   public static void debug(String message)
     { if (abc.main.Debug.v().residueCodeGen) 
          System.err.println(message);
     }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,
			WeavingContext wc) {

	debug("beginning cflow codegen");

	SootClass stackClass
	    =Scene.v().loadClassAndSupport("org.aspectj.runtime.internal.CFlowStack");
	Type object=Scene.v().getSootClass("java.lang.Object").getType();

	debug("getting cflowStack");
	Local cflowStack=localgen.generateLocal(stackClass.getType(),"cflowstack");
        Stmt getstack=Jimple.v().newAssignStmt
	    (cflowStack,Jimple.v().newStaticFieldRef(setup.getCflowStack()));
	units.insertAfter(getstack,begin);

	debug("checking validity");
	Local isvalid=localgen.generateLocal(BooleanType.v(),"cflowactive");
	SootMethod isValidMethod=stackClass.getMethod("isValid",new ArrayList());
	Stmt checkvalid=Jimple.v().newAssignStmt
	    (isvalid,
	     Jimple.v().newVirtualInvokeExpr(cflowStack,isValidMethod));
	units.insertAfter(checkvalid,getstack);

	debug("generating abort");
	Stmt abort=Jimple.v().newIfStmt
	    (Jimple.v().newEqExpr(isvalid,IntConstant.v(0)),
	     fail);
	units.insertAfter(abort,checkvalid);
	
	debug("setting up to get bound values");
	ArrayList getargs=new ArrayList(1);
	getargs.add(IntType.v());
	SootMethod getMethod=stackClass.getMethod("get",getargs);
	Local item=localgen.generateLocal(object,"cflowbound");

	debug("starting iteration");
	Stmt last=abort; int index=0;
	Iterator it=vars.iterator();
	while(it.hasNext()) {
	    WeavingVar to=(WeavingVar) (it.next());
	    
	    debug("handling weaving var"+to);

	    Type type=to.getType();

	    Stmt getItem=Jimple.v().newAssignStmt
		(item,
		 Jimple.v().newVirtualInvokeExpr(cflowStack,getMethod,IntConstant.v(index)));
	    units.insertAfter(getItem,last);

	    Value result;
	    
	    if(type instanceof PrimType) { 
		SootClass boxClass=Restructure.JavaTypeInfo.getBoxingClass(type);
		SootMethod unboxMethod=boxClass.getMethod
		    (Restructure.JavaTypeInfo.getBoxingClassMethodName(type),new ArrayList()); 
		
		result=Jimple.v().newVirtualInvokeExpr(item,unboxMethod);
		
	    } else {

		result=Jimple.v().newCastExpr(item,type);
	    }
	    last=to.set(localgen,units,getItem,wc,result);
	    index++;
	}
	debug("done with cflow codegen");
	return last;

    }

    public String toString() {
	return "cflow("+setup.getPointcut()+")";
    }

}
