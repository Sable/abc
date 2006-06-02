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

import java.util.Collection;
import java.util.Iterator;

import soot.SootMethod;
import abc.main.Debug;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.matching.SMEdge;
import abc.tm.weaving.matching.SMNode;
import abc.tm.weaving.matching.TMStateMachine;


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
            
            if (to.hasEdgeTo(to, "") // (skip-loop)
                    && !to.hasEdgeTo(to, symbol)
                    && !abc.main.Debug.v().noNegativeBindings) {
            	

            	boolean generateSkipEdge;
                if(Debug.v().tmAnalysis) {
                	//if we use interprocedural static analysis, we can find out
                	//if this particular symbol can trigger this particular skip loop
                	//at all; so let's that that

                	//get the skip edge
                	SMEdge skipLoop = null;
                	for (Iterator iter = to.getOutEdgeIterator(); iter.hasNext();) {
						SMEdge edge = (SMEdge) iter.next();
						if(edge.isSkipEdge()) {
							skipLoop = edge;
							break;
						}
					}
                	assert skipLoop != null;
                	
                	//ask the analysis if this skip symbol may at all ever be triggered by
                	//this symbol in this particular program
                	generateSkipEdge =
                		tm.getMayFlowAnalysis().mayThisSymbolTriggerThisSkipLoop(symbol, skipLoop);
                	
                	if(Debug.v().debugTmAnalysis && !generateSkipEdge) {
                		debug( "Not generating skip loop for state "+to+ " with " +
                			   "number "+to.getNumber()+" since the static analysis found " +
                			   "that this edge can never be taken." );
                	}
                } else {
                	//else, we generate it in any case
                	generateSkipEdge = true;
                }
                if(generateSkipEdge)
                	helper.genSkipLabelUpdate(to.getNumber(), symbol, method);
            }
        }
    }

    protected void fillInSomeAdviceBody(SootMethod method, TraceMatch tm,
                                    CodeGenHelper helper)
    {
        TMStateMachine sm = (TMStateMachine) tm.getStateMachine();
        Iterator states = sm.getStateIterator();

        helper.genReturnIfNotUpdated(method);

        while (states.hasNext()) {
            SMNode state = (SMNode) states.next();
            boolean skip_loop;

            // there is only one final state, and we remember it
            // in order to generate solution code later
            if (state.isFinalNode())
                helper.setFinalState(state.getNumber());

            // we don't want to accumulate useless constraints
            // for initial states
            if (state.isInitialNode())
                continue;

            if (state.hasEdgeTo(state, "")) // (skip-loop)
                skip_loop = true;
            else
                skip_loop = false;

            helper.genLabelMasterUpdate(skip_loop, state.getNumber(),
                                            method, state.isFinalNode());
        }
    }
 
    /**
     * Fills in the method stubs generated by the frontend for a given tracematch.
     * @param tm the tracecmatch to deal with.
     */
    public void fillInTraceMatch(TraceMatch tm) {
        TMStateMachine tmsm = (TMStateMachine)tm.getStateMachine();

		Collection unused = tm.getUnusedFormals();
        
        tmsm.prepareForMatching(tm, tm.getFormalNames(), unused, tm.getPosition());
        
        
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
