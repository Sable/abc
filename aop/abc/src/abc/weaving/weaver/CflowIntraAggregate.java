/* Abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL; 
 * if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.weaving.weaver;

import java.util.*;

import soot.*;
import soot.util.*;
import soot.jimple.*;
import abc.soot.util.LocalGeneratorEx;


/** Cflow Intraprocedural analysis phase I:
 *  aggregates all the local variables that access the same
 *  CflowStack/Counter field of an aspect into one variable,
 *  assigned at the beginning of the method.
 *  Improves the bytecode slightly over what is otherwise produced.
 *  Relies on: Any local variable of type CFlowStack/CFlowCounter is
 *  introduced by the weaver, and the CFlowStack/CFlowCounter static
 *  fields in aspects are never modified after they are initialized. 
 * 
 * @author Damien Sereni
 */
public class CflowIntraAggregate extends BodyTransformer {

	// Number of different locals representing the same cflow stack/counter
	// that we require before aggregating them
	private final int AGGREGATE_THRESHOLD = 2;

	private LocalGeneratorEx lg;

	private SootClass cflowStackClass;
	private SootClass cflowCounterClass;

	private static CflowIntraAggregate instance = 
		new CflowIntraAggregate();
	private CflowIntraAggregate() {
		// Get the SootClass objects for CflowStack and CflowCounter
		this.cflowStackClass = Scene.v().getSootClass(
			"uk.ac.ox.comlab.abc.runtime.internal.CFlowStack");
		this.cflowCounterClass = Scene.v().getSootClass(
			"uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter");
	}
	
	public static CflowIntraAggregate v() { return instance; }

	private void addToMultimap(Map/*<SootFieldRef, Chain<Local>>*/ m, 
							   SootFieldRef fr,
							   Local l) {
		if (m.containsKey(fr)) {
			Chain oldchain = (Chain)m.get(fr);
			oldchain.add(l);			   
		} else {
			HashChain newchain = new HashChain();
			newchain.add(l);
			m.put(fr, newchain);
		}
	}

	private Map/*<SootFieldRef,Chain<Local>>*/ getCflowStackLocals(Body b) {
		HashMap/*<SootFieldRef,Chain<Local>>*/ m = new HashMap();
		Iterator units = b.getUnits().iterator();
		while (units.hasNext()) {
			Stmt s = (Stmt)units.next();
			// Looking for:
			// LOCAL<type: CflowStack or CflowCounter> := STATIC FIELD REF
			if (s instanceof AssignStmt) {
				Value left = ((AssignStmt)s).getLeftOp();
				Value right = ((AssignStmt)s).getRightOp();
				if (left instanceof Local) {
				if (   left.getType().equals(cflowStackClass.getType()) 
				    ||  left.getType().equals(cflowCounterClass.getType())) {
				    	// We have an assignment to a stack/counter variable
				    	// There should be no others, but just check anyway that
				    	// we have a static field ref
				    	if (right instanceof FieldRef) {
				    		SootFieldRef fr = ((FieldRef)right).getFieldRef();
				    		addToMultimap(m, fr, (Local)left);
				    	}
				    }
				}
			}
		}
		return m;
	}

	private boolean shouldAggregate(Chain/*<Local>*/ ls) {
		return (ls.size() >= AGGREGATE_THRESHOLD);
	}

	private String getLocalName(SootFieldRef sf) {
		if (sf.type().equals(cflowStackClass.getType())) {
			return "cflowstack";
		}
		if (sf.type().equals(cflowCounterClass.getType())) {
			return "cflowcounter";
		}
		throw new RuntimeException("Unknown Static Field Ref type: "+sf);
	}

	private Local addNewCflowLocalAndInit(Body b, Chain/*<Local>*/ ls, SootFieldRef sf) {
		Local l = lg.generateLocal(sf.type(), getLocalName(sf));	
		
		Stmt assign = Jimple.v().newAssignStmt(l, Jimple.v().newStaticFieldRef(sf));
		
		b.getUnits().addFirst(assign);
		
		return l;
	}

	private void replaceCflowLocal(ValueBox e, Local newl) {
		// Replace the base of a virtual invoke stmt by newl
		
		VirtualInvokeExpr vie = (VirtualInvokeExpr)e.getValue();
		VirtualInvokeExpr newvie = 
			Jimple.v().newVirtualInvokeExpr(newl, vie.getMethodRef(), vie.getArgs());
		e.setValue(newvie);
	}

	private void replaceCflowLocalIfApplicable(ValueBox e, 
											   Chain/*<Local>*/ ls,
											   Local newl)
	{
		// Replace the base of a virtual invoke stmt by newl 
		// PROVIDED the base was a local var in ls
		
		Value base = ((VirtualInvokeExpr)e.getValue()).getBase();
		
		Iterator it = ls.iterator();
		while (it.hasNext()) {
			Local l = (Local)it.next();
			if (l.equivTo(base)) {
				replaceCflowLocal(e, newl);
				return;
			}
		}
	}

	private void findAndReplaceCflowLocals(Body b, Chain/*<Local>*/ ls, Local newl) {
		Iterator units = b.getUnits().iterator();
		
		while (units.hasNext()) {
			Stmt s = (Stmt)units.next();
			
			if (s.containsInvokeExpr()) {
				InvokeExpr e = s.getInvokeExpr();
				if (e instanceof VirtualInvokeExpr) {
					replaceCflowLocalIfApplicable(s.getInvokeExprBox(),ls,newl);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	protected void internalTransform(Body b, String phaseName, Map options) {
		// Initialise a local var generator:
		
		lg = new LocalGeneratorEx(b);
		
		// Find all the locals that get assigned cflow stacks/counters, and produce
		// the mapping from cflow stacks/counters to sets of variables that represent
		// them
		
		Map/*<SootFieldRef,Chain<Local>>*/ cflows = getCflowStackLocals(b);
		
		// FIXME Check that this doesn't cause a problem with the initial setting of 
		// the CflowStack/Counter field in the aspect
		
		// For each stack/counter, if we bother to aggregate, make a new var to hold the
		// stack/counter, and assign to it at the beginning of the method
		// TODO Be more clever and assign to it at the latest common ancestors of all references
		// Then replace all references to any of the vars it subsumes by that var
		//System.out.println("******* " + b.getMethod().getName());
		Iterator it = cflows.keySet().iterator();
		while (it.hasNext()) {
			SootFieldRef sf = (SootFieldRef)it.next();
			Chain/*<Local>*/ ls = (Chain/*<Local>*/)cflows.get(sf);
			if (shouldAggregate(ls)) {
				//System.out.println("Found something to aggregate: " + sf + " with " + 
		//		ls.size() + " uses");
				
				// Add the new local and initialisation
				Local newl = addNewCflowLocalAndInit(b, ls, sf);
				//System.out.println("Aggregated to " + newl);
				
				// Replace other locals
				
				findAndReplaceCflowLocals(b, ls, newl);
			}
		}
		
		// TODO Check that useless locals do get thrown away after this!

	}

}
