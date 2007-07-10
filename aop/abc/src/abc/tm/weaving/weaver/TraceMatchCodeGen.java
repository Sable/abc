/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Pavel Avgustinov
 * Copyright (C) 2005 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.tm.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;

import abc.soot.util.LocalGeneratorEx;
import abc.tm.weaving.aspectinfo.*;
import abc.tm.weaving.matching.*;
import abc.weaving.aspectinfo.*;


/**
 * Fills in method stubs for tracematch classes.
 * @author Pavel Avgustinov
 * @author Julian Tibble
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
    protected void fillInAdviceBodies(TraceMatch tm)
    {
        CodeGenHelper helper = tm.getCodeGenHelper();
        helper.makeAndInitLabelFields();
        helper.genAcquireLock();

        Iterator syms = tm.getSymbols().iterator();

        while (syms.hasNext()) {
            String symbol = (String) syms.next();
            SootMethod advice_method = tm.getSymbolAdviceMethod(symbol);

            fillInSymbolAdviceBody(symbol, advice_method, tm, helper);
        }

        SootMethod some_advice_method = tm.getSomeAdviceMethod();
        fillInSomeAdviceBody(some_advice_method, tm, helper);

        helper.transformParametersAndProceeds();
    }
    
    protected void fillInSymbolAdviceBody(String symbol, SootMethod method,
                                            TraceMatch tm, CodeGenHelper helper)
    {
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        Iterator to_states = sm.getStateIterator();

        helper.genNullChecks(method);
        helper.genRegisterSymbolBindings(symbol, method);

        while (to_states.hasNext()) {
            SMNode to = (SMNode) to_states.next();
            Iterator edges = to.getInEdgeIterator();

            // don't accumulate useless constraints for
            // initial states
            if (to.isInitialNode())
                continue;

            while (edges.hasNext()) {
                SMEdge edge = (SMEdge) edges.next();

                if (!edge.isSkipEdge() && edge.getLabel().equals(symbol)) {
                    SMNode from = (SMNode) edge.getSource();

                    helper.genLabelUpdate(from.getNumber(), to.getNumber(),
                                            edge.getLabel(), method);
                }
            }

            // FIXME: remove when old code-gen is removed
            if (to.hasSkipLoop(symbol) 
                    && !to.hasEdgeTo(to, symbol)
                    && !abc.main.Debug.v().noNegativeBindings)
                helper.genSkipLabelUpdate(to, symbol, method);
        }
    }

    protected void fillInSomeAdviceBody(SootMethod method, TraceMatch tm,
                                    CodeGenHelper helper)
    {
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        Iterator states = sm.getStateIterator();
        int final_state_num = -1;

        helper.genReturnIfNotUpdated(method);

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            boolean skip_loop;

            // there is only one final state, and it is recorded
            // in order to generate solution code later
            // the update for this state is also done last because
            // it may release the tracematch lock
            if (state.isFinalNode()) {
                final_state_num = state.getNumber();
                continue;
            }

            // don't accumulate useless constraints
            // for initial states
            if (state.isInitialNode())
                continue;

            if (state.hasSkipLoop())
                skip_loop = true;
            else
                skip_loop = false;

            helper.genLabelMasterUpdate(skip_loop, state.getNumber(),
                                            method, false);
        }

        helper.genLabelMasterUpdate(false, final_state_num, method, true);
        helper.setFinalState(final_state_num);
    }
 
    /**
     * Fills in the method stubs generated by the frontend for a given tracematch.
     * @param tm the tracecmatch to deal with.
     */
    public void fillInTraceMatch(TraceMatch tm) {
        TMStateMachine tmsm = (TMStateMachine)tm.getStateMachine();

        Collection unused = tm.getUnusedFormals();
        
        tmsm.prepareForMatching(tm, tm.getFormalNames(), unused, tm.getPosition());
        tm.createIndexingScheme();
        
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
        fillInAdviceBodies(tm);
    }
}
