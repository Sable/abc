package abc.tm.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;

import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.UnUsedParams;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;
import abc.weaving.aspectinfo.*;


/**
 * Fills in method stubs for tracematch classes.
 * @author Pavel Avgustinov
 */
public class TraceMatchCodeGen {
    
    // TODO: Perhaps have a dedicated flag for tracematch codegen
    private static void debug(String message)
    { if (abc.main.Debug.v().aspectCodeGen)
        System.err.println("TCG*** " + message);
    }

    /**
     * Fills in the method stubs that have been generated for this tracematch.
     * @param tm the tracematch in question
     */
    protected void fillInAdviceBodies(TraceMatch tm, Collection unused)
    {
        List body_formals = traceMatchBodyParameters(unused, tm);

        CodeGenHelper helper = new CodeGenHelper(tm);
        helper.makeAndInitLabelFields();

        Iterator syms = tm.getSymbols().iterator();

        while (syms.hasNext()) {
            String symbol = (String) syms.next();
            SootMethod advice_method = tm.getSymbolAdviceMethod(symbol);

            fillInSymbolAdviceBody(symbol, advice_method, tm, helper);
        }

        Iterator kinds = tm.getKinds().iterator();
        while (kinds.hasNext()) {
            String kind = (String) kinds.next();
            SootMethod advice_method = tm.getSomeAdviceMethod(kind);

            fillInSomeAdviceBody(kind, advice_method, tm, helper, body_formals);
        }
    }
    
    protected void fillInSymbolAdviceBody(String symbol, SootMethod method,
                                            TraceMatch tm, CodeGenHelper helper)
    {
        TMStateMachine sm = (TMStateMachine) tm.getState_machine();
        Iterator to_states = sm.getStateIterator();

        while (to_states.hasNext()) {
            SMNode to = (SMNode) to_states.next();
            Iterator edges = to.getInEdgeIterator();

            // we don't accumulate useless constraints for
            // initial states
            if (to.isInitialNode())
                continue;

            helper.genNullChecks(method);

            while (edges.hasNext()) {
                SMEdge edge = (SMEdge) edges.next();

                if (edge.getLabel().equals(symbol)) {
                    SMNode from = (SMNode) edge.getSource();

                    helper.genLabelUpdate(from.getNumber(), to.getNumber(),
                                            edge.getLabel(), method);
                }
            }

            helper.genNullChecksJumpTarget(method);

            if (to.hasEdgeTo(to, "")) // (skip-loop)
                helper.genSkipLabelUpdate(to.getNumber(), symbol, method);
        }
    }

    protected void fillInSomeAdviceBody(String kind, SootMethod method,
                                        TraceMatch tm, CodeGenHelper helper,
                                        List body_formals)
    {
        TMStateMachine sm = (TMStateMachine) tm.getState_machine();
        Iterator states = sm.getStateIterator();
        SMNode final_state = null;

        helper.genTestAndResetUpdated(method);

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            boolean skip_loop;

            // there is only one final state, and we remember it
            // in order to generate solution code later
            if (state.isFinalNode())
                final_state = state;

            // we don't want to accumulate useless constraints
            // for initial states
            if (state.isInitialNode())
                continue;

            if (state.hasEdgeTo(state, "")) // (skip-loop)
                skip_loop = true;
            else
                skip_loop = false;

            helper.genLabelMasterUpdate(skip_loop, state.getNumber(), method);
        }

        if (!tm.isAround() || kind.equals("around"))
            helper.genRunSolutions(final_state.getNumber(),
                                    method, body_formals);
    }
 
    protected List traceMatchBodyParameters(Collection unused, TraceMatch tm)
    {
        List formals = new ArrayList(tm.getFormals().size());
        Iterator i = tm.getFormals().iterator();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();

            if (!unused.contains(f.getName()))
                formals.add(f);
        }

        return formals;
    }

    protected void prepareAdviceBody(SootMethod sm, List names, Collection unused) {
    	List paramTypes = new LinkedList();
    	List paramNames = new LinkedList();
    	Iterator ptIter = sm.getParameterTypes().iterator();
    	Iterator nameIter = names.iterator();
    	while (ptIter.hasNext()) {
    		Type pt = (Type) ptIter.next();
    		String name = (String) nameIter.next();
    		if (!unused.contains(name)){
    			paramTypes.add(pt); 
    			paramNames.add(name);
    		}
    	}
    	
    	// renumber the identity statements
    	Body body = sm.getActiveBody();
    	Unit u = null;
    	Chain units = body.getUnits();
    	for (Iterator unitIter = units.iterator(); unitIter.hasNext(); ) {
    		u = (Unit) unitIter.next();
    		if (u instanceof IdentityStmt) {
    			IdentityStmt is = (IdentityStmt) u;
    			Value rhs = is.getRightOp();
    			if (rhs instanceof ParameterRef) {
    				ParameterRef pr = (ParameterRef) rhs;
    				String oldName = (String) names.get(pr.getIndex());
    				if (paramNames.contains(oldName)) {
    				   int newIndex = paramNames.indexOf(oldName);
    				   pr.setIndex(newIndex);
    			    } else 
    			    	unitIter.remove();
    			}
    		}
    	}
    	
    	sm.setParameterTypes(paramTypes);
 /*   
    	if (u == null) return;
		
		SootClass scIter = Scene.v().getSootClass("java.util.Iterator");
		paramTypes.add(scIter.getType());
		
		
    	Local iterLocal = body.getParameterLocal(paramTypes.size()-1);
    	LocalGeneratorEx lgen = new LocalGeneratorEx(body);
    	
    	// is there still a binding remaining?
    	Local hasNext = lgen.generateLocal(BooleanType.v(),"hasNext");
    	SootMethodRef smrHasNext = scIter.getMethod("hasNext",new LinkedList()).makeRef();
    	InvokeExpr e = Jimple.v().newVirtualInvokeExpr(iterLocal,smrHasNext,new LinkedList());
		AssignStmt ass = Jimple.v().newAssignStmt(hasNext,e);
		units.insertBefore(u,ass);
		u.redirectJumpsToThisTo(ass);
		
		// if not, proceed
		EqExpr ce = Jimple.v().newEqExpr(hasNext,IntConstant.v(0));
    	Stmt stmtIfHasNext = Jimple.v().newIfStmt(ce,u);
    	units.insertBefore(u,stmtIfHasNext);
    
    	// otherwise recursively call the body with another binding
        // FIXME: this is missing!

        // jump over the normal proceed
		Unit elsetarget = (Unit) units.getSuccOf(u);
		Stmt stmtJump = Jimple.v().newGotoStmt(elsetarget);
    	units.insertBefore(u,elsetarget);
  
		System.out.println(body.toString());		
		*/
    }
    /**
     * Fills in the method stubs generated by the frontend for a given tracematch.
     * @param tm the tracecmatch to deal with.
     */
    public void fillInTraceMatch(TraceMatch tm) {
        TMStateMachine tmsm = (TMStateMachine)tm.getState_machine();

		Collection unused = UnUsedParams.unusedFormals(tm.getBodyMethod(),tm.getFormalNames());
        
        tmsm.prepareForMatching(tm.getSymbols(), tm.getFormalNames(), tm.getSym_to_vars(), 
                                                UnUsedParams.unusedFormals(tm.getBodyMethod(),tm.getFormalNames()),
                                                tm.getPosition());
        
        
        prepareAdviceBody(tm.getBodyMethod(),tm.getFormalNames(),unused);
        
        // Create the constraint class(es). A constraint is represented in DNF as a set of
        // disjuncts, which are conjuncts of positive or negative bindings. For now, we 
        // only create one kind of disjunct class for each tracematch, specialised to have
        // fields for the tracecmatch variables. A potential optimisation is to specialise
        // the disjunct class to each state, as negative bindings needn't be kept for all
        // states in general -- this may/will be done in time.
        ClassGenHelper cgh = new ClassGenHelper(tm);
        cgh.generateClasses();
        
        // Fill in the advice bodies. The method stubs have been created by the frontend and
        // can be obtained from the TraceMatch object; code to keep track of changing 
        // constraints and to run the tracematch advice when appropriate, with the necessary
        // bindings, should be added.
        fillInAdviceBodies(tm, unused);
    }
}
