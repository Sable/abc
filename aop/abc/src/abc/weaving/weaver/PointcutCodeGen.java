package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

public class PointcutCodeGen {

    public void weaveInAspects( SootClass cl) {
        for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
            final SootMethod method = (SootMethod) methodIt.next();
            if( method.isAbstract() ) continue;
            if( method.isNative() ) continue;

	    List/*<AdviceApplication>*/ adviceList = GlobalAspectInfo.v().getAdviceList(method);
	    System.out.println("AdviceList for " + method );
	    System.out.println(adviceList.toString());

            Body b = method.getActiveBody();
            LocalGenerator localgen = new LocalGenerator(b);
            Chain units = b.getUnits();
	    Iterator adviceIt = adviceList.iterator();
	    Stmt stmt = null;
	    AdviceApplication aa = null;
            while( adviceIt.hasNext()) {
		aa = (AdviceApplication) adviceIt.next();
		final AdviceDecl advicedecl=aa.advice;
                final AdviceSpec adviceSpec = advicedecl.getAdviceSpec();
		final SootClass aspect=
                    advicedecl.getAspect().getInstanceClass().getSootClass();
        System.out.println("JENNIFER: advicedecl: "+advicedecl+" impl: "+advicedecl.getImpl().getClass());
                final SootMethod adviceImpl =
                    advicedecl.getImpl().getSootMethod();

                if( adviceSpec instanceof BeforeAdvice ) {
                    Local l = localgen.generateLocal( aspect.getType() );
                    units.insertBefore( Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList()))), aa.begin);
                    units.insertBefore( 
                                    Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr( l, adviceImpl ) ), aa.begin );
                } else if( adviceSpec instanceof AfterReturningAdvice ) {
                    handleAfterReturning(aspect, method, localgen, aa, adviceImpl);
                    //throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AfterThrowingAdvice ) {
                    handleAfterThrowing(aspect, method, localgen, aa, adviceImpl);
                    //throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AfterAdvice ) {
                    handleAfterReturning(aspect, method, localgen, aa, adviceImpl);
                    handleAfterThrowing(aspect, method, localgen, aa, adviceImpl);
                    //throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AroundAdvice ) {
                    throw new RuntimeException("NYI");
                } else {
                    throw new RuntimeException("Unrecognized advice type: "+adviceSpec);
                }
	    }
	}
    }

    private void handleAfterReturning(SootClass aspect, SootMethod meth, LocalGenerator lg, AdviceApplication aa, SootMethod adviceImpl){
      
        System.out.println("Handling after returning");
        Body b = meth.getActiveBody();
        Chain units = b.getUnits();
        // no params
        Local l = lg.generateLocal(aspect.getType());
        AssignStmt assignStmt =  Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList())));
        units.insertAfter( assignStmt, aa.end);
        units.insertAfter( Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(l, adviceImpl)), assignStmt);
         
    }
    
    private void handleAfterThrowing(SootClass aspect, SootMethod meth, LocalGenerator lg, AdviceApplication aa, SootMethod adviceImpl){
      
        System.out.println("Handling after throwing");
        Body b = meth.getActiveBody();
        Chain units = b.getUnits();
        
        // insert nop after aa.end to track region
        NopStmt nop1 = Jimple.v().newNopStmt();
        units.insertAfter(nop1, aa.end);
        
        NopStmt nop2 = Jimple.v().newNopStmt();
        units.insertAfter(nop2, nop1);
       
        GotoStmt goto1 = Jimple.v().newGotoStmt(nop2);
        units.insertAfter(goto1, nop1);
        
        Local catchLocal = lg.generateLocal(RefType.v("java.lang.Throwable"));
        CaughtExceptionRef exceptRef = Jimple.v().newCaughtExceptionRef();
        IdentityStmt idStmt = Jimple.v().newIdentityStmt(catchLocal, exceptRef);
        
        System.out.println("inserting: "+idStmt);
        units.insertAfter(idStmt, goto1);
                
        // no params
        Local l = lg.generateLocal(aspect.getType());
        AssignStmt assignStmt =  Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList())));
        units.insertAfter( assignStmt, idStmt);
        InvokeStmt vInvokeStmt =  Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(l, adviceImpl));
        units.insertAfter( vInvokeStmt, assignStmt);
         
        ThrowStmt throwStmt = Jimple.v().newThrowStmt(catchLocal);

        units.insertAfter(throwStmt, vInvokeStmt);

        b.getTraps().add(Jimple.v().newTrap(Scene.v().getSootClass("java.lang.Throwable"), aa.begin, nop1, idStmt));

    }

    
}
