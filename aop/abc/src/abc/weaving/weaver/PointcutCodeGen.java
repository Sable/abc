package abc.weaving.weaver;
import soot.*;
import soot.util.*;
import soot.jimple.*;
import java.util.*;
import abc.weaving.aspectinfo.*;
import abc.weaving.matching.*;
import soot.javaToJimple.LocalGenerator;

public class PointcutCodeGen {

    public class StmtTracker {
        public StmtTracker(Stmt b, Stmt e){
            begin = b;
            end = e;
        }
       
        public boolean isNullBegin(){
            if (begin == null) return true;
            return false;
        }
        
        public boolean isNullEnd(){
            if (end == null) return true;
            return false;
        }
        
        
        private final Stmt begin;
        private final Stmt end;

        public Stmt begin(){
            return begin;
        }

        public Stmt end(){
            return end;
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append("Tracker: ");
            sb.append("Begin Stmt: ");
            sb.append(begin);
            sb.append(" End Stmt: ");
            sb.append(end);

            return sb.toString();
        }
    }
    
    public void weaveInAspects( SootClass cl) {
        for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
            final SootMethod method = (SootMethod) methodIt.next();
            if( method.isAbstract() ) continue;
            if( method.isNative() ) continue;

            // If the returns have been normalized in this method, first
            // and last point to the original first statement of the method
            // and the nop before the last return, respectively.
            // It the returns have not yet been normalized, first and last
            // remain null.
            Stmt first = null;
            Stmt last = null;
        
            // tracks start and end stmts
            StmtTracker stmtTracker = new StmtTracker(null, null);
          
            
	        MethodAdviceList adviceList = GlobalAspectInfo.v().getAdviceList(method);
            if( adviceList == null ) adviceList = new MethodAdviceList();
            System.out.println("AdviceList for " + method );
	        System.out.println(adviceList.toString());

            Body b = method.getActiveBody();
            LocalGenerator localgen = new LocalGenerator(b);
            Chain units = b.getUnits();
	    
            // Quick hack to make it compile
	        Iterator adviceIt = adviceList.stmtAdvice.iterator();
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

                if( aa instanceof StmtAdviceApplication ) {
                    StmtAdviceApplication saa = (StmtAdviceApplication) aa;
                    System.out.println("just before handle: "+stmtTracker);
                    stmtTracker = handle(aspect, method, localgen, stmtTracker.isNullBegin() ? saa.stmt : stmtTracker.begin(), stmtTracker.isNullEnd() ? saa.stmt : stmtTracker.end(), adviceImpl, adviceSpec);
                    System.out.println("just after handle: "+stmtTracker);
                    
                } else if( aa instanceof ExecutionAdviceApplication ) {
                    if( first == null ) {

                        // Here we move all returns to the end, and set up 
                        // first and last
                        first = (Stmt) units.getFirst();
                        last = (Stmt) units.getLast();

                        Local ret = null;
                        if( last instanceof ReturnStmt ) {
                            ReturnStmt lastRet = (ReturnStmt) last;
                            ret = (Local) lastRet.getOp();
                        } else if( last instanceof ReturnVoidStmt ) {
                            // do nothing
                        } else {
                            Type returnType = method.getReturnType();
                            ret = localgen.generateLocal(returnType);
                            units.insertAfter(
                                    Jimple.v().newReturnStmt(ret),
                                    last );
                        }

                        // now the last stmt should always be return ret
                        if( units.getLast() instanceof ReturnStmt ) {
                            ReturnStmt lastRet = (ReturnStmt) units.getLast();
                            if( lastRet.getOp() != ret ) 
                                throw new RuntimeException("This can't happen");
                        } else if( !(units.getLast() instanceof ReturnVoidStmt ) ) {
                            throw new RuntimeException("This can't happen");
                        }

                        // insert the nop
                        Stmt nop = Jimple.v().newNopStmt();
                        units.insertBefore( nop, units.getLast() );

                        // update any traps to end at nop
                        for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) {
                            final Trap tr = (Trap) trIt.next();
                            if( tr.getEndUnit() == units.getLast() ) {
                                tr.setEndUnit(nop);
                            }
                        }

                        // Look for returns in the middle of the method
                        Iterator it = units.snapshotIterator();
                        while( it.hasNext() ) {
                            Stmt u = (Stmt) it.next();
                            if( u == units.getLast() ) continue;
                            if( u instanceof ReturnStmt ) {
                                ReturnStmt ur = (ReturnStmt) u;
                                units.insertBefore( Jimple.v()
                                    .newAssignStmt( ret, ur.getOp() ), ur );
                            }
                            if( u instanceof ReturnVoidStmt ) {
                                Stmt gotoStmt = Jimple.v().newGotoStmt(nop);
                                units.swapWith( u, gotoStmt );
                                for( Iterator trIt = b.getTraps().iterator(); trIt.hasNext(); ) {
                                    final Trap tr = (Trap) trIt.next();
                                    for( Iterator boxIt = tr.getUnitBoxes().iterator(); boxIt.hasNext(); ) {
                                        final UnitBox box = (UnitBox) boxIt.next();
                                        if( box.getUnit() == u ) {
                                            box.setUnit( gotoStmt );
                                        }
                                    }
                                }
                            }
                        }

                        last = nop;
                    }
                    stmtTracker = handle(aspect, method, localgen, stmtTracker.isNullBegin() ? first : stmtTracker.begin(), stmtTracker.isNullEnd() ? last : stmtTracker.end(), adviceImpl, adviceSpec);
                } else throw new RuntimeException("Unrecognized advice application");
	    }
	}
    }

    private StmtTracker handle(
            SootClass aspect,
            SootMethod method,
            LocalGenerator localgen,
            Stmt begin,
            Stmt end,
            SootMethod adviceImpl,
            AdviceSpec adviceSpec){
        if( adviceSpec instanceof BeforeAdvice ) {
            return handleBefore(aspect, method, localgen, begin, end, adviceImpl);
        } else if( adviceSpec instanceof AfterReturningAdvice ) {
            return handleAfterReturning(aspect, method, localgen, begin, end, adviceImpl);
        } else if( adviceSpec instanceof AfterThrowingAdvice ) {
            return handleAfterThrowing(aspect, method, localgen, begin, end, adviceImpl);
        } else if( adviceSpec instanceof AfterAdvice ) {
            StmtTracker afterRet = handleAfterReturning(aspect, method, localgen, begin, end, adviceImpl);
            StmtTracker afterThrow = handleAfterThrowing(aspect, method, localgen, begin, end, adviceImpl);
            System.out.println("afterRet: "+afterRet);
            StmtTracker newRes = new StmtTracker(afterThrow.begin(), afterRet.end());
            System.out.println("newRes: "+newRes);
            
            return newRes;
        } else if( adviceSpec instanceof AroundAdvice ) {
            throw new RuntimeException("NYI");
        } else {
            throw new RuntimeException("Unrecognized advice type: "+adviceSpec);
        }
    }
    
    private StmtTracker handleBefore(SootClass aspect, SootMethod meth, LocalGenerator lg, Stmt begin, Stmt end, SootMethod adviceImpl){
        System.out.println("Handling before");
        System.out.println("begin: "+begin+" end: "+end);
        Body b = meth.getActiveBody();
        // this non patching chain is needed so that Soot doesn't "Fix" 
        // the traps (but it doesn't work if the pointcut is a try/catch) 
        Chain units = b.getUnits().getNonPatchingChain();
        Local l = lg.generateLocal( aspect.getType() );
        AssignStmt newbegin =  Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList())));
        units.insertBefore( newbegin, begin);
        units.insertBefore( Jimple.v().newInvokeStmt( Jimple.v().newVirtualInvokeExpr( l, adviceImpl ) ), begin );
        System.out.println("new begin: "+newbegin+" new end: "+end);
        return new StmtTracker(newbegin, end);
    }
    
    private StmtTracker handleAfterReturning(SootClass aspect, SootMethod meth, LocalGenerator lg, Stmt begin, Stmt end, SootMethod adviceImpl){
      
        System.out.println("Handling after returning");
        System.out.println("begin: "+begin+" end: "+end);
        Body b = meth.getActiveBody();
        Chain units = b.getUnits();
        // no params
        Local l = lg.generateLocal(aspect.getType());
        AssignStmt assignStmt =  Jimple.v().newAssignStmt( l, Jimple.v().newStaticInvokeExpr( aspect.getMethod("aspectOf", new ArrayList())));
        units.insertAfter( assignStmt, end);
        InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(l, adviceImpl));
        units.insertAfter( invokeStmt, assignStmt);
         
        System.out.println("new begin: "+begin+" new end: "+invokeStmt);
        return new StmtTracker(begin, invokeStmt);
    }
    
    private StmtTracker handleAfterThrowing(SootClass aspect, SootMethod meth, LocalGenerator lg, Stmt begin, Stmt end, SootMethod adviceImpl){
      
        System.out.println("Handling after throwing");
        System.out.println("begin: "+begin+" end: "+end);
        Body b = meth.getActiveBody();
        Chain units = b.getUnits();
        
        // insert nop after end to track region
        NopStmt nop1 = Jimple.v().newNopStmt();
        units.insertAfter(nop1, end);
        
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

        b.getTraps().add(Jimple.v().newTrap(Scene.v().getSootClass("java.lang.Throwable"), begin, nop1, idStmt));

        System.out.println("new begin: "+begin+" new end: "+throwStmt);
        return new StmtTracker(begin, throwStmt);
    }

    
}
