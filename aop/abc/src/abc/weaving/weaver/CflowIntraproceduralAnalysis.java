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

import abc.soot.util.LocalGeneratorEx;
import java.util.*;
import soot.*;
import soot.util.*;
import soot.jimple.*;

/**
 * @author Damien Sereni
 *
 * A simple intraprocedural analysis to avoid having to find the current
 * thread more than once for CflowStack / CflowCounter updating
 */

public class CflowIntraproceduralAnalysis extends BodyTransformer {

	// The sharing threshold: the number of invocations of methods on a
	// single cflow stack or counter required for us to get the right 
	// object in advance to avoid getting the current thread over and 
	// over again
	// Trade-off: currently the thread get will occur at the beginning of
	// the method, regardless of whether the cflow operations happen or not
	// So can make it slower - should only do this if we're reasonably certain
	// that the number of invokes makes it (possibly) worthwhile
	// Also, adds a local per cflow stack or counter

	private final int SHARE_THRESHOLD = 2;

	private LocalGeneratorEx lg;

	private Map/*<String,String>*/ cflowCounterRename;
	private Map/*<String,String>*/ cflowStackRename;	
	
	private SootClass cflowStackClass;
	private SootClass cflowCounterClass;
	private Type objectType;

	private static CflowIntraproceduralAnalysis instance = 
		new CflowIntraproceduralAnalysis();
	private CflowIntraproceduralAnalysis() {
		// Get the SootClass objects for CflowStack and CflowCounter
		this.cflowStackClass = Scene.v().getSootClass(
			"uk.ac.ox.comlab.abc.runtime.internal.CFlowStack");
		this.cflowCounterClass = Scene.v().getSootClass(
			"uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter");
		this.objectType = Scene.v().getSootClass(
			"java.lang.Object").getType();
			
		// Initialise the renaming cflowRename
		// Maps a CflowCounter/CflowStack method name
		// to the name of the equivalent method taking an 
		// additional parameter (the cached stack/counter)
		this.initCflowRename();
	}
	
	private void initCflowRename() {
		cflowCounterRename = new HashMap();
		cflowStackRename = new HashMap();
		
		// CFlowCounter Methods
		cflowCounterRename.put("inc", "incCounter");
		cflowCounterRename.put("dec", "decCounter");
		cflowCounterRename.put("isValid", "isValidCounter");
		
		// CFlowStacke Methods
		cflowStackRename.put("push", "pushStack");
		cflowStackRename.put("pushInstance", "pushInstanceStack");
		cflowStackRename.put("pop", "popStack");
		
		cflowStackRename.put("peek", "peekStack");
		cflowStackRename.put("get", "getTopStack");
		cflowStackRename.put("peekInstance", "peekInstanceStack");
		cflowStackRename.put("peekCFlow", "peekCFlowStack");
		cflowStackRename.put("peekTopCFlow", "peekTopCFlowStack");
		
		cflowStackRename.put("isValid", "isValidStack");
	}
	
	public static CflowIntraproceduralAnalysis v() { return instance; }

	private Chain/*<Local>*/ getCflowLocals(Body b) {
		Chain/*<Local>*/ locals = b.getLocals();
		Chain /*<Local>*/ cflowLocals = new HashChain();
		
		Iterator it = locals.iterator();
		while (it.hasNext()) {
			Local l = (Local)it.next();
			if (l.getType().equals(cflowStackClass.getType())) {
				cflowLocals.add(l);
			}
			if (l.getType().equals(cflowCounterClass.getType())) {
				cflowLocals.add(l);
			}
		}
		
		return cflowLocals;
	}

	private Chain/*<ValueBox>*/ getCflowLocalStmts(Local l, Body b) {
		PatchingChain units = b.getUnits();
		Chain cflowstmts = new HashChain();
		
		Iterator it = units.iterator();
		while (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			
			if (s.containsInvokeExpr()) {
				InvokeExpr e = s.getInvokeExpr();
				if (e instanceof VirtualInvokeExpr) {
					Value v = ((VirtualInvokeExpr)e).getBase();
					if (l.equivTo(v)) {
						cflowstmts.add(s.getInvokeExprBox());
					}
				}
			}
		}
		return cflowstmts;
	}

	private boolean shouldShareStack(Local l, Chain/*<ValueBox>*/ invokes, Body b) {
		return (invokes.size() >= SHARE_THRESHOLD);
	}

	private Stmt findInitStmt(Local cflowLoc, Body b) {
		// Find the initialising statement for cflowLoc
		
		Iterator it = b.getUnits().iterator();
		while (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			if (s instanceof AssignStmt) {
				Value base = ((AssignStmt)s).getLeftOp();
				if (cflowLoc.equivTo(base))
				 	return s;
			}
		}
		throw new RuntimeException("Could not find statement initialising "+cflowLoc.getName());
	}

	private Local addThreadLocalAndInitialise(Local cflowLoc, Body b) {
		Local threadl = lg.generateLocal(objectType, cflowLoc.getName()+"Cache");
		
		if (cflowLoc.getType().equals(cflowStackClass.getType())) {
			SootMethodRef stackGetThread = 
				Scene.v().makeMethodRef(cflowStackClass, 
										"getStack",
										new ArrayList(),
										objectType,
										false);
			Stmt init = 
				Jimple.v().newAssignStmt(threadl, 
				Jimple.v().newVirtualInvokeExpr(cflowLoc, 
												stackGetThread,
												new ArrayList()));
			
			b.getUnits().insertAfter(init,
									 findInitStmt(cflowLoc, b));
		} else 
		if (cflowLoc.getType().equals(cflowCounterClass.getType())) {
			SootMethodRef counterGetThread = 
							Scene.v().makeMethodRef(cflowCounterClass, 
													"getCounter",
													new ArrayList(),
													objectType,
													false);
			Stmt init = 
				Jimple.v().newAssignStmt(threadl, 
				Jimple.v().newVirtualInvokeExpr(cflowLoc, 
												counterGetThread,
												new ArrayList()));
			
			b.getUnits().insertAfter(init,
									 findInitStmt(cflowLoc, b));
		} else
			throw new RuntimeException("Cflow local "+cflowLoc+
					" has unexpected type "+cflowLoc.getType());

		return threadl;
	}

	private String getNewName(boolean isStack, String oldname) {
		String newname = (String) 
			(isStack?cflowStackRename:cflowCounterRename)
				.get(oldname);
		
		if (newname == null) {
			throw new RuntimeException("Error: could not find "+
			(isStack?"CFlowStack":"CFlowCounter")+" method "+oldname);
		}
		return newname;
	}

	private void updateCflowUse(ValueBox vb, Local threadl) {
		// NOTE vb contains a VirtualInvokeExpr
		VirtualInvokeExpr vie = (VirtualInvokeExpr)vb.getValue();
		
		boolean isStack;
		
		if (vie.getMethodRef().declaringClass().equals(cflowStackClass)) {
			isStack = true;
		} else
		if (vie.getMethodRef().declaringClass().equals(cflowCounterClass)) {
			isStack = false;
		} else throw new RuntimeException("Unknown class in presumed cflow runtime operation: "+
										  vie.getMethodRef().getClass());
		
		String newName = getNewName(isStack, vie.getMethodRef().name());
						
		List newParameterTypes = new ArrayList();
		Iterator paramIt = vie.getMethodRef().parameterTypes().iterator();
		while (paramIt.hasNext()) {
			Type t = (Type)paramIt.next();
			newParameterTypes.add(t);
		}
		newParameterTypes.add(objectType);
		
		SootMethodRef newMethod = Scene.v().makeMethodRef(
			vie.getMethodRef().declaringClass(),
			newName,
			newParameterTypes,
			vie.getMethodRef().returnType(),
			vie.getMethodRef().isStatic());
			
		ArrayList newParams = new ArrayList();
		Iterator actualIt = vie.getArgs().iterator();
		while (actualIt.hasNext()) {
			Value v = (Value)actualIt.next();
			newParams.add(v);
		}
		newParams.add(threadl);
			
		VirtualInvokeExpr newvie = 
			Jimple.v().newVirtualInvokeExpr(
				(Local)vie.getBase(),
				newMethod,
				newParams);
			
		vb.setValue(newvie);
	}

	private void updateCflowUseIfNecessary(ValueBox vb, Local threadl) {
		// Update a use of a cflow variable to use the cached value
		// IF necessary - ie if the use is not the call to getStack/getCounter!
		
		SootMethodRef meth = ((VirtualInvokeExpr)vb.getValue()).getMethodRef();
		
		if (meth.name().equals("getStack") || meth.name().equals("getCounter"))
			return;
		
		updateCflowUse(vb, threadl);
	}

	private void updateCflowUses(Local cflowl, Local threadl, Body b) {
		Iterator it = b.getUnits().iterator();
		
		while (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			if (s.containsInvokeExpr()) {
				InvokeExpr e = s.getInvokeExpr();
				if (e instanceof VirtualInvokeExpr) {
					Value base = ((VirtualInvokeExpr)e).getBase();
					if (cflowl.equivTo(base)) {
						updateCflowUseIfNecessary(s.getInvokeExprBox(), threadl);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	protected void internalTransform(Body b, String phaseName, Map options) {
		// Get a new generator for local vars
		lg = new LocalGeneratorEx(b);
		
		// Get a list of CflowStacks/CflowCounters
	
		Chain/*<Local>*/ cflowLocals = getCflowLocals(b);
		
		// For each CflowStack/Counter, get all occurences (Stmts)
		
		// Iterate through the stacks/counters, if the # of occurences is
		// large enough, 
		//    - add a local to hold the thread stack/counter
		//    - add a statement to initialise it at beginning of body
		// TODO find a better place to initialise the thread stack/counter
		// (as may not always need to initialise it!)
		//    - change all statements to use the saved stack/counter

		Iterator it = cflowLocals.iterator();
		while (it.hasNext()) {
			Local l = (Local)it.next(); 
			Chain/*<ValueBox>*/ invokes = getCflowLocalStmts(l, b);
			if (shouldShareStack(l, invokes, b)) {
				//System.out.println("Found something to share!: " +
				//	l + " with " + invokes.size() + " statements");
				// Add a local and initialise it
				
				Local threadl = addThreadLocalAndInitialise(l, b);
				
				// Change all statements
				
				updateCflowUses(l, threadl, b);
			} 
		}

	}

}
