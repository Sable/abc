/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Sascha Kuzins
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

package abc.weaving.weaver.around;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.DisableExceptionCheckTag;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;


public class ProceedMethod {
	public final AdviceMethod adviceMethod;

	public Set adviceApplications = new HashSet();

	public HashMap shadowInformation = new HashMap();

	private final AroundWeaver aroundWeaver;

	ProceedMethod(AdviceMethod method, SootClass shadowClass,
			boolean bStaticProceedMethod, String proceedMethodName,
			boolean bClosureMethod) {
		this.aroundWeaver = method.aroundWeaver;
		this.bStaticProceedMethod = bStaticProceedMethod;
		this.adviceMethod = method;
		//				this.adviceMethod = parent;
		this.shadowClass = shadowClass;
		this.bUseClosureObject = bClosureMethod;
		this.lookupStmtID=aroundWeaver.getUniqueID();
		
		String interfaceName = adviceMethod.interfaceInfo.closureInterface
				.getName();

		if (bStaticProceedMethod || bClosureMethod) {
			sootProceedMethod = new SootMethod(proceedMethodName,
					new LinkedList(), this.adviceMethod.getAdviceReturnType(),
					Modifier.PUBLIC | Modifier.STATIC);
		} else {
			AroundWeaver.debug("adding interface " + interfaceName
					+ " to class " + shadowClass.getName());

			shadowClass
					.addInterface(adviceMethod.interfaceInfo.closureInterface);

			// create new method					
			sootProceedMethod = new SootMethod(proceedMethodName,
					new LinkedList(), this.adviceMethod.getAdviceReturnType(),
					Modifier.PUBLIC);
		}
		sootProceedMethod.addTag(new DisableExceptionCheckTag());
		Body proceedBody = Jimple.v().newBody(sootProceedMethod);

		aroundWeaver.proceedMethods.put(sootProceedMethod, this);

		sootProceedMethod.setActiveBody(proceedBody);
		AroundWeaver.debug("adding method " + sootProceedMethod.getName()
				+ " to class " + shadowClass.getName());
		shadowClass.addMethod(sootProceedMethod);

		Chain proceedStatements = proceedBody.getUnits().getNonPatchingChain();

		// generate this := @this
		LocalGeneratorEx lg = new LocalGeneratorEx(proceedBody);
		Local lThis = null;
		if (!bStaticProceedMethod && !bClosureMethod) {
			lThis = lg.generateLocal(shadowClass.getType(), "this");
			proceedStatements.addFirst(Jimple.v().newIdentityStmt(lThis,
					Jimple.v().newThisRef(RefType.v(shadowClass))));
		}
		Util.validateMethod(sootProceedMethod);
		//proceedMethodInfo.targetLocal=Restructure.addParameterToMethod(
		//	proceedMethod, (Type)proceedMethodParameters.get(0), "targetArg");

		{
			Iterator it = this.adviceMethod.originalAdviceFormalTypes
					.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				//System.out.println(" " +method.getActiveBody().getUnits());
				Local l = Restructure.addParameterToMethod(sootProceedMethod,
						type, "orgAdviceFormal");
				//System.out.println(" " +method.getActiveBody().getUnits());
				Util.validateMethod(sootProceedMethod);
				adviceFormalLocals.add(l);
			}
		}
		Util.validateMethod(sootProceedMethod);

		shadowIdParamLocal = Restructure.addParameterToMethod(
				sootProceedMethod,
				(Type) this.adviceMethod.proceedMethodParameterTypes.get(0),
				"shadowID");
		shadowIDParamIndex = sootProceedMethod.getParameterCount() - 1;
		bindMaskParamLocal = Restructure.addParameterToMethod(
				sootProceedMethod,
				(Type) this.adviceMethod.proceedMethodParameterTypes.get(1),
				"bindMask");

		if (this.adviceMethod.proceedMethodParameterTypes.size() != 2)
			throw new InternalAroundError();

		Stmt lastIDStmt = Restructure.getParameterIdentityStatement(
				sootProceedMethod, sootProceedMethod.getParameterCount() - 1);

		if (!bClosureMethod) {
			// generate exception code (default target)
			SootClass exception = Scene.v().getSootClass(
					"java.lang.RuntimeException");
			Local ex = lg.generateLocal(exception.getType(), "exception");
			Stmt newExceptStmt = Jimple.v().newAssignStmt(ex,
					Jimple.v().newNewExpr(exception.getType()));
			Stmt initEx = Jimple.v().newInvokeStmt(
					Jimple.v().newSpecialInvokeExpr(
							ex,
							exception.getMethod("<init>", new ArrayList())
									.makeRef()));
			Stmt throwStmt = Jimple.v().newThrowStmt(ex);

			defaultTarget = Jimple.v().newNopStmt();
			proceedStatements.add(defaultTarget);
			proceedStatements.add(newExceptStmt);
			proceedStatements.add(initEx);
			proceedStatements.add(throwStmt);
			defaultEnd = Jimple.v().newNopStmt();
			proceedStatements.add(defaultEnd);
			NopStmt endTag=Jimple.v().newNopStmt();
			endTag.addTag(new AroundWeaver.LookupStmtTag(lookupStmtID, false));
			proceedStatements.add(endTag);
			
			// just generate a nop for now.
			setLookupStmt(Jimple.v().newNopStmt());

			proceedStatements.insertAfter(getLookupStmt(), lastIDStmt);

			//AdviceMethod adviceMethodInfo = state.getInterfaceInfo(interfaceName);
			Iterator it = this.adviceMethod.contextArguments.iterator();
			while (it.hasNext()) {
				Type type = (Type) it.next();
				Local l = Restructure.addParameterToMethod(sootProceedMethod,
						type, "contextArg");
				contextParamLocals.add(l);
			}
		} else {
			defaultTarget = Jimple.v().newNopStmt();
			proceedStatements.add(defaultTarget);
			defaultEnd = Jimple.v().newNopStmt();
			proceedStatements.add(defaultEnd);
			// just generate a nop for now.
			setLookupStmt(Jimple.v().newNopStmt());
			proceedStatements.insertAfter(getLookupStmt(), lastIDStmt);
		}

		Util.validateMethod(sootProceedMethod);

		proceedMethodBody = sootProceedMethod.getActiveBody();
		proceedMethodStatements = proceedMethodBody.getUnits()
				.getNonPatchingChain();
	}

	public int numOfShadows = 0;

	public void doWeave(AdviceApplication adviceAppl, SootMethod shadowMethod) {

		AdviceApplicationInfo adviceApplication = new AdviceApplicationInfo(
				aroundWeaver,
				this, adviceAppl, shadowMethod);
		this.adviceApplications.add(adviceApplication);
		adviceApplication.doWeave();
		numOfShadows++;
	}

	public void modifyLookupStatement(Stmt switchTarget, int shadowID) {
		// modify the lookup statement in the proceed method
		lookupValues.add(IntConstant.v(shadowID));
		proceedMethodTargets.add(switchTarget);
		// generate new lookup statement and replace the old one
		Stmt newLookupStmt;
		if (bStaticProceedMethod && numOfShadows == 0)
			newLookupStmt = Jimple.v().newNopStmt();
		else
			newLookupStmt = Util.newSwitchStmt(// Jimple.v().newLookupSwitchStmt(
					shadowIdParamLocal, lookupValues, proceedMethodTargets,
					defaultTarget);
		proceedMethodStatements.insertAfter(newLookupStmt, getLookupStmt());
		proceedMethodStatements.remove(getLookupStmt());
		setLookupStmt(newLookupStmt);

		if (!bStaticProceedMethod) {
			this.adviceMethod.fixProceedMethodSuperCalls(shadowClass);
		}

		Util.cleanLocals(proceedMethodBody);
	}

	//HashMap /*String, Integer*/ fieldIDs=new HashMap();
	public void addParameters(List addedDynArgsTypes) {
		AroundWeaver.debug("adding parameters to access method "
				+ sootProceedMethod);
		Util.validateMethod(sootProceedMethod);

		Iterator it2 = addedDynArgsTypes.iterator();
		while (it2.hasNext()) {
			Type type = (Type) it2.next();
			AroundWeaver.debug(" " + type);
			Local l = Restructure.addParameterToMethod(sootProceedMethod, type,
					"contextArgFormal");
			contextParamLocals.add(l);
		}

		//				modify existing super call in the access method		
		Stmt stmt = superInvokeStmt;
		if (stmt != null) {
			//throw new InternalCompilerError("This does not work until soot allows changing method refs properly");

			//addEmptyDynamicParameters(method, addedDynArgs, proceedMethodName);
			InvokeExpr invoke = (InvokeExpr) stmt.getInvokeExprBox().getValue();
			List newParams = new LinkedList();
			newParams.addAll(Util.getParameterLocals(sootProceedMethod
					.getActiveBody()));
			List types = new LinkedList(sootProceedMethod.getParameterTypes());

			InvokeExpr newInvoke = Util.createNewInvokeExpr(invoke, newParams,
					types);
			stmt.getInvokeExprBox().setValue(newInvoke);
		}
	}

	public void assignCorrectParametersToLocals(List context, int[] argIndex,
			Stmt first, HashMap localMap, Residue.Bindings bindings) {

		AroundWeaver
				.debug("Access method: assigning correct parameters to locals*********************");

		LocalGeneratorEx lg = new LocalGeneratorEx(proceedMethodBody);

		// Assign the correct access parameters to the locals 
		Stmt insertionPoint = first;
		Stmt skippedCase = Jimple.v().newNopStmt();
		Stmt nonSkippedCase = Jimple.v().newNopStmt();
		Stmt neverBoundCase = Jimple.v().newNopStmt();
		Stmt gotoStmt = Jimple.v().newGotoStmt(neverBoundCase);
        Tagger.tagStmt(gotoStmt, InstructionKindTag.AROUND_PROCEED);
		Stmt ifStmt = Jimple.v().newIfStmt(
				Jimple.v().newEqExpr(bindMaskParamLocal, IntConstant.v(1)),
				skippedCase);
        Tagger.tagStmt(ifStmt, InstructionKindTag.AROUND_PROCEED);
		proceedMethodStatements.insertBefore(ifStmt, insertionPoint);
		proceedMethodStatements.insertBefore(nonSkippedCase, insertionPoint);
		proceedMethodStatements.insertBefore(gotoStmt, insertionPoint);
		proceedMethodStatements.insertBefore(skippedCase, insertionPoint);
		proceedMethodStatements.insertBefore(neverBoundCase, insertionPoint);
		NopStmt afterDefault = Jimple.v().newNopStmt();
		proceedMethodStatements.insertAfter(afterDefault, nonSkippedCase);

		Local maskLocal = lg.generateLocal(IntType.v(), "maskLocal");

		Set defaultLocals = new HashSet();

		boolean emptyIf = true;

		// Process the bindings.
		// The order is important.
		for (int index = bindings.numOfFormals() - 1; index >= 0; index--) {
			List localsFromIndex = bindings.localsFromIndex(index);
			if (localsFromIndex == null) {
			} else {
				emptyIf = false;

				if (localsFromIndex.size() == 1) { // non-skipped case: assign advice formal
					Local paramLocal = (Local) adviceFormalLocals.get(index);
					Local actual = (Local) localsFromIndex.get(0);
					Local actual2 = (Local) localMap.get(actual);
					AssignStmt s = Jimple.v()
							.newAssignStmt(actual2, paramLocal);
                    Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
					proceedMethodStatements.insertAfter(s, nonSkippedCase);
					Restructure.insertBoxingCast(sootProceedMethod
							.getActiveBody(), s, true);

					/// allow boxing?
				} else {
					// Before all the switch statements, the default values are
					// assigned.
					// The switch statements then (possibly) overwrite the values with 
					// the actual bindings.
					{
						//NopStmt nop=Jimple.v().newNopStmt();
						//statements.insertAfter(nop, nonSkippedCase);
						for (Iterator itl = localsFromIndex.iterator(); itl
								.hasNext();) {
							Local l = (Local) itl.next();
							int id = context.indexOf(l);
							if (id == -1) {
								AroundWeaver.debug(" skipped local: " + l);
							} else if (!defaultLocals.contains(l)) {
								defaultLocals.add(l);
								Local paramLocal = (Local) contextParamLocals
										.get(argIndex[id]);
								Local actual3 = (Local) localMap.get(l);
								AssignStmt s = Jimple.v().newAssignStmt(
										actual3, paramLocal);
                                Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
								proceedMethodStatements.insertBefore(s,
										afterDefault);
								Restructure.insertBoxingCast(sootProceedMethod
										.getActiveBody(), s, true);
								emptyIf = false;
							}
						}
					}

					//generatedSwitchStmtsIDs.add(new Integer(index));
					int mask = bindings.getMaskBits(index);
					AssignStmt as = Jimple.v().newAssignStmt(
							maskLocal,
							Jimple.v().newAndExpr(bindMaskParamLocal,
									IntConstant.v(mask)));
					AssignStmt as2 = Jimple.v().newAssignStmt(
							maskLocal,
							Jimple.v().newShrExpr(maskLocal,
									IntConstant.v(bindings.getMaskPos(index))));
                    Tagger.tagStmt(as, InstructionKindTag.AROUND_PROCEED);
                    Tagger.tagStmt(as2, InstructionKindTag.AROUND_PROCEED);
					proceedMethodStatements.insertAfter(as, afterDefault);
					proceedMethodStatements.insertAfter(as2, as);
					NopStmt endStmt = Jimple.v().newNopStmt();
					proceedMethodStatements.insertAfter(endStmt, as2);

					int localIndex = 0;
					List lookupValues = new LinkedList();
					List targets = new LinkedList();
					for (Iterator itl = localsFromIndex.iterator(); itl
							.hasNext(); localIndex++) {
						Local l = (Local) itl.next();
						lookupValues.add(IntConstant.v(localIndex));

						Local actual3 = (Local) localMap.get(l);

						NopStmt targetNop = Jimple.v().newNopStmt();

						proceedMethodStatements.insertAfter(targetNop, as2);
						targets.add(targetNop);

						Local paramLocal = (Local) adviceFormalLocals
								.get(index);
						AssignStmt s = Jimple.v().newAssignStmt(actual3,
								paramLocal);
                        Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
						proceedMethodStatements.insertAfter(s, targetNop);
						GotoStmt g = Jimple.v().newGotoStmt(endStmt);
                        Tagger.tagStmt(g, InstructionKindTag.AROUND_PROCEED);
						proceedMethodStatements.insertAfter(g, s);
						Restructure.insertBoxingCast(sootProceedMethod
								.getActiveBody(), s, true);

					}

					// default case (exception)								
					SootClass exception = Scene.v().getSootClass(
							"java.lang.RuntimeException");
					Local ex = lg.generateLocal(exception.getType(),
							"exception");
					Stmt newExceptStmt = Jimple.v().newAssignStmt(ex,
							Jimple.v().newNewExpr(exception.getType()));
					Stmt initEx = Jimple.v().newInvokeStmt(
							Jimple.v().newSpecialInvokeExpr(
									ex,
									exception.getMethod("<init>",
											new ArrayList()).makeRef()));
					Stmt throwStmt = Jimple.v().newThrowStmt(ex);
                    Tagger.tagStmt(newExceptStmt, InstructionKindTag.AROUND_PROCEED);
                    Tagger.tagStmt(initEx, InstructionKindTag.AROUND_PROCEED);
                    Tagger.tagStmt(throwStmt, InstructionKindTag.AROUND_PROCEED);
					proceedMethodStatements.insertAfter(newExceptStmt, as2);
					proceedMethodStatements.insertAfter(initEx, newExceptStmt);
					proceedMethodStatements.insertAfter(throwStmt, initEx);

					Stmt lp = Util.newSwitchStmt(// Jimple.v().newLookupSwitchStmt(
							maskLocal, lookupValues, targets, newExceptStmt);
                    Tagger.tagStmt(lp, InstructionKindTag.AROUND_PROCEED);
					proceedMethodStatements.insertAfter(lp, as2);
				}
			}
		}

		int i = 0;
		// process the context
		for (Iterator it = context.iterator(); it.hasNext(); i++) {

			Local actual = (Local) it.next(); // context.get(i);
			Local actual2 = (Local) localMap.get(actual);
			if (!proceedMethodBody.getLocals().contains(actual2))
				throw new InternalAroundError();
			if (actual2 == null)
				throw new InternalAroundError();

			if (bindings.contains(actual)) {
				AroundWeaver.debug(" static binding: " + actual.getName());

				/*
				 // We use lastIndexOf here to mimic ajc's behavior:
				 // When binding the same value multiple times, ajc's
				 // proceed only regards the last one passed to it.
				 // Can be changed to indexOf to pick the first one 
				 // (which would also seem reasonable). 
				 int index = bindings.lastIndexOf(actual);
				 */

				{ // skipped case: assign dynamic argument
					Local paramLocal = (Local) contextParamLocals
							.get(argIndex[i]);
					AssignStmt s = Jimple.v()
							.newAssignStmt(actual2, paramLocal);
                    Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
					proceedMethodStatements.insertAfter(s, skippedCase);
					Restructure.insertBoxingCast(sootProceedMethod
							.getActiveBody(), s, true);
					/// allow boxing?
				}
				emptyIf = false;
			} else {
				AroundWeaver.debug(" no binding: " + actual.getName());
				// no binding
				Local paramLocal = (Local) contextParamLocals.get(argIndex[i]);
				AssignStmt s = Jimple.v().newAssignStmt(actual2, paramLocal);
                Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
				proceedMethodStatements.insertAfter(s, neverBoundCase);
				Util.insertCast(sootProceedMethod.getActiveBody(), s, s
						.getRightOpBox(), actual2.getType());
			}
		}

		if (emptyIf)
			proceedMethodStatements.remove(ifStmt);

		AroundWeaver
				.debug("done: Access method: assigning correct parameters to locals*********************");
	}

	//public final AdviceMethod adviceMethod;
	public final SootClass shadowClass;

	public final boolean bStaticProceedMethod;

	public final Body proceedMethodBody;

	public final Chain proceedMethodStatements;

	public final boolean bUseClosureObject;

	final List proceedMethodTargets = new LinkedList();

	final List lookupValues = new LinkedList();

	final NopStmt defaultTarget;

	final NopStmt defaultEnd;

	private Stmt _lookupStmt;
	private final int lookupStmtID;
	void setLookupStmt(Stmt lookupStmt) {
		_lookupStmt=lookupStmt;
		_lookupStmt.addTag(new AroundWeaver.LookupStmtTag(lookupStmtID, true));
	}
	Stmt getLookupStmt() {
		return _lookupStmt;
	}
	
	int nextShadowID;

	public final int shadowIDParamIndex;

	public final Local shadowIdParamLocal;

	public final Local bindMaskParamLocal;

	final List contextParamLocals = new LinkedList();

	final List adviceFormalLocals = new LinkedList();

	public final SootMethod sootProceedMethod;

	SootClass superCallTarget = null;

	Stmt superInvokeStmt = null;

}
