package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;

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
            Chain units = b.getUnits();
            Iterator codeIt = units.snapshotIterator();
	    Iterator adviceIt = adviceList.iterator();
	    Stmt stmt = null;
	    AdviceApplication aa = null;
            while( codeIt.hasNext() && adviceIt.hasNext()) {
		if(stmt==null) stmt = (Stmt) codeIt.next();
		if(aa==null) aa = (AdviceApplication) adviceIt.next();
		if(stmt!=aa.begin) {
		    stmt=null;
		    continue;
		}
		final AdviceDecl advicedecl=aa.advice;
		final SootClass aspect=advicedecl.getAspect().getInstanceClass().getSootClass();

		Local l = Jimple.v().newLocal( localName(), aspect.getType() );
		b.getLocals().add(l);
		units.insertBefore( Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList()))), stmt);
		units.insertBefore( 
				   Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr( l, aspect.getMethod("before$1", new ArrayList() ) ) ), stmt );
		aa=null;
	    }
	}
    }


    // TODO: get rid of this
    static int localNum = 1;
     private static String localName() {
         localNum++;
         return "aspect$"+localNum;
     }
}
