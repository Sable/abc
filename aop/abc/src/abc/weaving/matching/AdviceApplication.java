package abc.weaving.matching;

import soot.*;
import soot.jimple.*;
import soot.util.*;

import abc.weaving.aspectinfo.*;
import java.util.*;

/** The data structure the pointcut matcher computes */
/*  @author Ganesh Sittampalam                       */
/*  @date 23-Apr-04                                  */
public class AdviceApplication {
    public Stmt begin,end;
    public AdviceDecl advice;
    public ConditionPointcutHandler cph;

    public AdviceApplication(Stmt _begin,Stmt _end,
			     AdviceDecl _advice,
			     ConditionPointcutHandler _cph) {
	begin=_begin;
	end=_end;
	advice=_advice;
	cph=_cph;
    }

    public static Hashtable computeAdviceLists(GlobalAspectInfo info) {
	Iterator clsIt;

	Hashtable ret=new Hashtable();

	for(clsIt=info.getWeavableClasses().iterator();clsIt.hasNext();) {

	    final AbcClass cls 
		= (AbcClass) clsIt.next();

	    SootClass sootCls = cls.getSootClass();
	    Iterator methodIt;

	    for(methodIt=sootCls.methodIterator();methodIt.hasNext();) {

		final SootMethod method = (SootMethod) methodIt.next();

		if(method.isAbstract()) continue;
		if(method.isNative()) continue;
		
		List/*<AdviceApplication>*/ apps=new LinkedList();

		Chain stmtsChain=method.retrieveActiveBody().getUnits();
		Stmt current=(Stmt) stmtsChain.getFirst();

		for(current=(Stmt) stmtsChain.getFirst();
		    current!=null;
		    current=(Stmt) stmtsChain.getSuccOf(current)) {
		    
		    Iterator adviceIt;
		    for(adviceIt=info.getAdviceDecls().iterator();
		        adviceIt.hasNext();) {

			final AdviceDecl ad = (AdviceDecl) adviceIt.next();

			Pointcut pc=ad.getPointcut();
		        boolean matches;

			if(pc!=null) {
			    matches=pc.matchesAt(sootCls,method,current);
			} else {
			    // BIG TEMPORARY HACK
			    matches=false;
			    if (current instanceof AssignStmt) {
				AssignStmt as = (AssignStmt) current;
				Value lhs = as.getLeftOp();
				if(lhs instanceof FieldRef) matches=true;
			    }
			}

			if(matches) {
			    apps.add(new AdviceApplication(current,current,
							   ad,null));
			}    
		    }
		}
		    
		ret.put(method,apps);

	    }
	}
	return ret;
    }
}
