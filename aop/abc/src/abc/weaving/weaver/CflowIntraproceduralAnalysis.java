/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Damien Sereni
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
	// Trade-off: if we are unlucky, we may be adding lots of null checks
	// that we can't get rid of (lazy initialisation strategy) - should
	// set a reasonable threshold in the hope that this doesn't happen too
	// often

	private final int SHARE_THRESHOLD = 2;

	private LocalGeneratorEx lg;

	private Map/*<String,String>*/ cflowCounterRename;
	private Map/*<String,String>*/ cflowStackRename;	
	
	private SootClass cflowStackClassV = null;
	private SootClass cflowCounterClassV = null;
	private Type objectTypeV = null;

	private SootClass cflowStackClass() {
		if (cflowStackClassV == null)
			cflowStackClassV = Scene.v().getSootClass
			("uk.ac.ox.comlab.abc.runtime.internal.CFlowStack");
		return cflowStackClassV;
	}
	private SootClass cflowCounterClass() {
		if (cflowCounterClassV == null)
			cflowCounterClassV = Scene.v().getSootClass
			("uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter");
		return cflowCounterClassV;
	}
	private Type objectType() {
		if (objectTypeV == null)
		 	objectTypeV = Scene.v().getSootClass
		 	("java.lang.Object").getType();
		 return objectTypeV;
	}

	private static CflowIntraproceduralAnalysis instance = 
		new CflowIntraproceduralAnalysis();
	public static void reset() { instance = new CflowIntraproceduralAnalysis(); }
		
	private CflowIntraproceduralAnalysis() {
		// Initialise the renaming cflowRename
		// Maps a CflowCounter/CflowStack method name
		// to the name of the equivalent method taking an 
		// additional parameter (the cached stack/counter)
		this.initCflowRename();
	}
	
	private static class CodeGen {
		// A bunch of helper methods to generate the relevant bits of code
		
		static Stmt genInitStmt(Local cflowLoc, Local threadl) {

		Stmt init;

		if (cflowLoc.getType().equals(v().cflowStackClass().getType())) {
			SootMethodRef stackGetThread = 
				Scene.v().makeMethodRef(v().cflowStackClass(), 
										"getStack",
										new ArrayList(),
										v().objectType(),
										false);
			init = 
				Jimple.v().newAssignStmt(threadl, 
				Jimple.v().newVirtualInvokeExpr(cflowLoc, 
												stackGetThread,
												new ArrayList()));
		} else 
		if (cflowLoc.getType().equals(v().cflowCounterClass().getType())) {
			SootMethodRef counterGetThread = 
							Scene.v().makeMethodRef(v().cflowCounterClass(), 
													"getCounter",
													new ArrayList(),
													v().objectType(),
													false);
			init = 
				Jimple.v().newAssignStmt(threadl, 
				Jimple.v().newVirtualInvokeExpr(cflowLoc, 
												counterGetThread,
												new ArrayList()));
		} else
			throw new RuntimeException("Cflow local "+cflowLoc+
					" has unexpected type "+cflowLoc.getType());

		return init;
	}
		
	static Local genThreadLocal(Local cflowLoc) {
		return v().lg.generateLocal(v().objectType(), cflowLoc.getName()+"Cache");
	}
	
	static Stmt genBranchIfNotNull(Local threadl, Unit targ) {
		return Jimple.v().newIfStmt(
					Jimple.v().newNeExpr(threadl,
										 NullConstant.v()), 
					targ);
	}
	
	static void genputAssignIfNull(Body b, Local cflowLoc, Local threadl, Unit fallthrough) {
		Stmt ass = genInitStmt(cflowLoc, threadl);
		b.getUnits().insertBefore(ass, fallthrough);
		Stmt br = genBranchIfNotNull(threadl, fallthrough);
		b.getUnits().insertBefore(br, ass);
	}
	
	static boolean isStackMethod(VirtualInvokeExpr vie) {
		if (vie.getMethodRef().declaringClass().equals(v().cflowStackClass())) {
			return true;
		} else
		if (vie.getMethodRef().declaringClass().equals(v().cflowCounterClass())) {
			return false;
		} else throw new RuntimeException("Unknown class in presumed cflow runtime operation: "+
										  vie.getMethodRef().getClass());
	}
	
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
			if (l.getType().equals(cflowStackClass().getType())) {
				cflowLocals.add(l);
			}
			if (l.getType().equals(cflowCounterClass().getType())) {
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
		Local threadl = lg.generateLocal(objectType(), cflowLoc.getName()+"Cache");
		
		// Initialise it to NULL
		// It will be initalised lazily later
		
		Stmt init = 
			Jimple.v().newAssignStmt(
				threadl,
				NullConstant.v());
		b.getUnits().addFirst(init);
		
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

	private void updateCflowUse(Body b, Stmt s, Local threadl) {
		ValueBox vb = s.getInvokeExprBox();
		
		// NOTE vb contains a VirtualInvokeExpr
		VirtualInvokeExpr vie = (VirtualInvokeExpr)vb.getValue();
		
		// Is this a stack or a counter?
		boolean isStack;
		isStack = CodeGen.isStackMethod(vie);
		
		// Get new method name
		String newName = getNewName(isStack, vie.getMethodRef().name());
		
		// Get new list of parameter types
		List newParameterTypes = new ArrayList();
		Iterator paramIt = vie.getMethodRef().parameterTypes().iterator();
		while (paramIt.hasNext()) {
			Type t = (Type)paramIt.next();
			newParameterTypes.add(t);
		}
		newParameterTypes.add(objectType());
		
		// Get new method
		// The new method is static
		SootMethodRef newMethod = Scene.v().makeMethodRef(
			vie.getMethodRef().declaringClass(),
			newName,
			newParameterTypes,
			vie.getMethodRef().returnType(),
			true);
			
		// Get new actuals
		ArrayList newParams = new ArrayList();
		Iterator actualIt = vie.getArgs().iterator();
		while (actualIt.hasNext()) {
			Value v = (Value)actualIt.next();
			newParams.add(v);
		}
		newParams.add(threadl);
			
		// Construct the call
		StaticInvokeExpr newie = 
			Jimple.v().newStaticInvokeExpr(
				newMethod,
				newParams);
		
		// Replace the original call
		vb.setValue(newie);
		
		// Add the code to initialize the stack/counter if was not done before
		CodeGen.genputAssignIfNull(b, (Local)vie.getBase(), threadl, s);
	}

	private void updateCflowUseIfNecessary(Body b, Stmt s, Local threadl) {
		
		// Update a use of a cflow variable to use the cached value
		// IF necessary - ie if the use is not a call to getStack/getCounter!
		
		ValueBox vb = s.getInvokeExprBox();		
		SootMethodRef meth = ((VirtualInvokeExpr)vb.getValue()).getMethodRef();
	
		if (meth.name().equals("getStack") || meth.name().equals("getCounter"))
			return;
		
		updateCflowUse(b, s, threadl);
	}

	private void updateCflowUses(Local cflowl, Local threadl, Body b) {
		Iterator it = b.getUnits().iterator();
		Set stmts = new HashSet();
		
		// Collect all the uses
		while (it.hasNext()) {
			Stmt s = (Stmt)it.next();
			if (s.containsInvokeExpr()) {
				InvokeExpr e = s.getInvokeExpr();
				if (e instanceof VirtualInvokeExpr) {
					Value base = ((VirtualInvokeExpr)e).getBase();
					if (cflowl.equivTo(base)) {
						stmts.add(s);
					}
				}
			}
		}
		
		// update them
		it = stmts.iterator();
		while (it.hasNext()) 
			updateCflowUseIfNecessary(b, (Stmt)it.next(), threadl);
		
	}

	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	protected void internalTransform(Body b, String phaseName, Map options) {
		// Get a new generator for local vars
		lg = new LocalGeneratorEx(b);
		
		// Get a list of CflowStacks/CflowCounters
	
		Chain/*<Local>*/ cflowLocals = getCflowLocals(b);
		
		// Iterate through the stacks/counters, if the # of occurences is
		// large enough, 
		//    - add a local to hold the thread stack/counter
		//    - initialize it to NULL at the beginning of the method body
		//    - change all uses of the cflow stack/counter to use the cached
		//      version, initializing it lazily as needed - rely on null check
		//      eliminator to get rid of the spurious null checks

		Iterator it = cflowLocals.iterator();
		while (it.hasNext()) {
			Local l = (Local)it.next(); 
			Chain/*<ValueBox>*/ invokes = getCflowLocalStmts(l, b);
			if (shouldShareStack(l, invokes, b)) {

				// Add a local and initialise it
				Local threadl = addThreadLocalAndInitialise(l, b);
				// Change all statements
				updateCflowUses(l, threadl, b);
			} 
		}

	}

}
