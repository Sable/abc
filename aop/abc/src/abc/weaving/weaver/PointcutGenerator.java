package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

public class PointcutGenerator {

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
                final SootMethod adviceImpl =
                    advicedecl.getImpl().getSootMethod();

                if( adviceSpec instanceof BeforeAdvice ) {
                    Local l = localgen.generateLocal( aspect.getType() );
                    units.insertBefore( Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList()))), aa.begin);
                    units.insertBefore( 
                                    Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr( l, adviceImpl ) ), aa.begin );
                } else if( adviceSpec instanceof AfterReturningAdvice ) {
                    throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AfterThrowingAdvice ) {
                    throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AfterAdvice ) {
                    throw new RuntimeException("NYI");
                } else if( adviceSpec instanceof AroundAdvice ) {
                    throw new RuntimeException("NYI");
                } else {
                    throw new RuntimeException("Unrecognized advice type: "+adviceSpec);
                }
	    }
	}
    }
}
