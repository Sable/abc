package abc.weaving.residues;

import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.util.*;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.weaver.WeavingContext;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import java.util.*;

public class CflowResidue extends Residue {
    private CflowSetup setup;
    private List/*<WeavingVar>*/ vars;
    private boolean useCounter;

    public CflowResidue(CflowSetup setup,List vars) {
	this.setup=setup;
	this.vars=vars;
        this.useCounter=setup.useCounter();
    }

   public static void debug(String message)
     { if (abc.main.Debug.v().residueCodeGen) 
          System.err.println("RCG: "+message);
     }

    private SootClass getStackOrCounterClass()
     { 

	 /* Load counter counter or stack class, depending on useCounter */

	 if (useCounter) { 
	     return Scene.v().getSootClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter");
		 } else {
	     return Scene.v().getSootClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowStack");
		 }
     }

    private SootFieldRef getCflowStackOrCounter() {

	/* Load field for class or counter from setup, depending on useCounter */

	if (useCounter) {
	    return setup.getCflowCounter();
	} else {
	    return setup.getCflowStack();
	}
    }

    private Stmt isValidStmt;
    /** Returns the statement that was woven to test isValid(). */
    public Stmt getIsValidStmt() { return isValidStmt; }

    public Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
			Chain units,Stmt begin,Stmt fail,boolean sense,
			WeavingContext wc) {

	/* Generate the code for checking that the cflow applies and 
	   binding any variables 
	   Only difference bet. stack and counter: different class names,
  	     give different var names for clearer jimple, and 
	     don't bother creating the array for counter */

	debug("beginning cflow codegen");
        debug(useCounter?"use Counter ":"no counter");

	SootClass stackorcounterClass=getStackOrCounterClass();
	Type object=Scene.v().getSootClass("java.lang.Object").getType();

	debug("getting cflow" + (useCounter?"Counter":"Stack"));
	Local cflowStackOrCounter = 
            localgen.generateLocal(stackorcounterClass.getType(),
                                   (useCounter?"cflowcounter":"cflowstack"));
        Stmt getstackorcounter=Jimple.v().newAssignStmt
	    (cflowStackOrCounter,Jimple.v().newStaticFieldRef(getCflowStackOrCounter()));
	units.insertAfter(getstackorcounter,begin);

	debug("checking validity");
	Local isvalid=localgen.generateLocal(BooleanType.v(),"cflowactive");
	SootMethodRef isValidMethod=Scene.v().makeMethodRef
	    (stackorcounterClass,"isValid",new ArrayList(),BooleanType.v(),false);
	Stmt checkvalid=Jimple.v().newAssignStmt
	    (isvalid,
	     Jimple.v().newVirtualInvokeExpr(cflowStackOrCounter,isValidMethod));
	units.insertAfter(checkvalid,getstackorcounter);

        isValidStmt = checkvalid;

	debug("generating abort");
	Expr test;
	if(sense) test=Jimple.v().newEqExpr(isvalid,IntConstant.v(0));
	else test=Jimple.v().newNeExpr(isvalid,IntConstant.v(0));
	Stmt abort=Jimple.v().newIfStmt(test,fail);
	units.insertAfter(abort,checkvalid);

        Stmt last = abort;

        if (sense && !useCounter) {

	debug("setting up to get bound values");
	ArrayList getargs=new ArrayList(1);
	getargs.add(IntType.v());
	SootMethodRef getMethod=Scene.v().makeMethodRef(stackorcounterClass,"get",getargs,object,false);
	Local item=localgen.generateLocal(object,"cflowbound");

	debug("starting iteration");
        int index=0;
	Iterator it=vars.iterator();
	while(it.hasNext()) {
	    WeavingVar to=(WeavingVar) (it.next());
	    
	    debug("handling weaving var "+to);

	    Type type=to.getType();

	    Stmt getItem=Jimple.v().newAssignStmt
		(item,
		 Jimple.v().newVirtualInvokeExpr(cflowStackOrCounter,getMethod,IntConstant.v(index)));
	    units.insertAfter(getItem,last);
	    last=getItem;

	    Value result;
	    
	    if(type instanceof PrimType) { 
		SootClass boxClass=Restructure.JavaTypeInfo.getBoxingClass(type);
		SootMethodRef unboxMethod=Scene.v().makeMethodRef
		    (boxClass,
		     Restructure.JavaTypeInfo.getBoxingClassMethodName(type),
		     new ArrayList(),
		     type,
		     false); 

		Local castval=localgen.generateLocal(boxClass.getType(),"cflowbound");
		
		Stmt caststmt=Jimple.v().newAssignStmt
		    (castval,Jimple.v().newCastExpr(item,boxClass.getType()));
		
		units.insertAfter(caststmt,last);
		last=caststmt;

		result=Jimple.v().newVirtualInvokeExpr(castval,unboxMethod);
		
	    } else {

		result=Jimple.v().newCastExpr(item,type);
	    }
	    last=to.set(localgen,units,last,wc,result);
	    index++;
	}

	}

	debug("done with cflow codegen");
	return last;

    }

    public String toString() {
	return "cflow("+setup.getPointcut()+")";
    }

    public CflowSetup setup() { return setup; }

}
