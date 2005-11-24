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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import polyglot.util.ErrorInfo;
import soot.Body;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.tagkit.Tag;
import soot.util.Chain;
import abc.main.options.OptionsParser;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AroundShadowInfoTag;
import abc.soot.util.DisableExceptionCheckTag;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.RedirectedExceptionSpecTag;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.AdviceSpec;
import abc.weaving.aspectinfo.AroundAdvice;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ExecutionAdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.Residue;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.InstructionShadowTag;
import abc.weaving.tagkit.InstructionSourceTag;
import abc.weaving.tagkit.Tagger;
import abc.weaving.weaver.CflowCodeGenUtils;
import abc.weaving.weaver.PointcutCodeGen;
import abc.weaving.weaver.WeavingContext;
import abc.weaving.weaver.around.AroundWeaver.ObjectBox;
import abc.weaving.weaver.around.AroundWeaver.ShadowInlineInfo;

public class AdviceApplicationInfo {
	private final ProceedMethod proceedMethod;

	public int shadowSize;

	public int shadowInternalLocalCount;

	public int getShadowSize() {
		int stmtCount=0;
		Iterator it=shadowMethodStatements.iterator(begin);
		while(it.hasNext()) {
			Stmt stmt=(Stmt)it.next();
			if (stmt==begin)
				continue;
			if (stmt==end)
				break;
			stmtCount++;
		}
		return stmtCount;
	}
	public final AroundWeaver aroundWeaver;
	//				final boolean bHasProceed;
	AdviceApplicationInfo(AroundWeaver aroundWeaver, ProceedMethod proceedMethod,
			AdviceApplication adviceAppl, SootMethod shadowMethod) {
		this.aroundWeaver=aroundWeaver;
		//this.proceedMethodName=ProceedMethod.this.proceedMethodSoot.getName();
		//this.bUseStaticProceedMethod=bS
		this.adviceAppl = adviceAppl;
		this.proceedMethod = proceedMethod;

		final boolean bExecutionAdvice = adviceAppl instanceof ExecutionAdviceApplication;

		AdviceDecl adviceDecl = (AdviceDecl) adviceAppl.advice;

		AdviceSpec adviceSpec = adviceDecl.getAdviceSpec();
		AroundAdvice aroundSpec = (AroundAdvice) adviceSpec;
		SootClass theAspect = adviceDecl.getAspect().getInstanceClass()
				.getSootClass();
		SootMethod method = adviceDecl.getImpl().getSootMethod();

		//this.bHasProceed=adviceDecl.getSootProceeds().size()>0;

		this.shadowMethod = shadowMethod;
		this.shadowClass = shadowMethod.getDeclaringClass();
		this.shadowMethodBody = shadowMethod.getActiveBody();
		this.shadowMethodStatements = shadowMethodBody.getUnits()
				.getNonPatchingChain();

		this.bStaticShadowMethod = shadowMethod.isStatic();
		this.begin = adviceAppl.shadowmatch.sp.getBegin();
		this.end = adviceAppl.shadowmatch.sp.getEnd();

		this.shadowSize = getShadowSize();

		AroundWeaver.debug("CLOSURE: "
				+ (this.proceedMethod.bUseClosureObject ? "Using closure"
						: "Not using closure"));

		if (this.proceedMethod.bUseClosureObject) {
			ShadowMatch sm = adviceAppl.shadowmatch;
			abc.main.Main.v().error_queue
					.enqueue(ErrorInfoFactory
							.newErrorInfo(
									ErrorInfo.WARNING,
									"Using closure object. This may impact performance.",
									sm.getContainer(), sm.getHost()));
		}

		// if the target is an around-advice method, 
		// make sure proceed has been generated for that method.
		if (bExecutionAdvice
				&& (Util.isAroundAdviceMethod(shadowMethod) || AroundWeaver.v()
						.getEnclosingAroundAdviceMethod(shadowMethod) != null)) {

			SootMethod relevantAdviceMethod;
			if (Util.isAroundAdviceMethod(shadowMethod))
				relevantAdviceMethod = shadowMethod;
			else
				relevantAdviceMethod = AroundWeaver.v()
						.getEnclosingAroundAdviceMethod(shadowMethod);

			AdviceMethod adviceMethodWovenInto = AroundWeaver.v()
					.getAdviceMethod(relevantAdviceMethod);
			if (adviceMethodWovenInto == null) {
				AdviceDecl advdecl;
				advdecl = getAdviceDecl(relevantAdviceMethod);
				List sootProceeds2 = new LinkedList();
				sootProceeds2.addAll(advdecl.getLocalSootMethods());
				if (!sootProceeds2.contains(relevantAdviceMethod))
					sootProceeds2.add(relevantAdviceMethod);

				adviceMethodWovenInto = new AdviceMethod(
						this.proceedMethod.adviceMethod.aroundWeaver,
						relevantAdviceMethod, AdviceMethod
								.getOriginalAdviceFormals(advdecl),
						sootProceeds2);
			}
			adviceMethodWovenInto.generateProceedCalls(false, true, null);
			adviceMethodWovenInto.bHasBeenWovenInto = true;
		}

	}
	private boolean isShadowBig() {
		return this.shadowSize>2;
	}
	private void extractShadowIntoStaticMethod(Local returnedLocal, List context) {

		AroundWeaver.debug("@@@@@@@@@@@@@@@@@@@@");
		//AroundWeaver.debug(Util.printMethod(shadowMethod));
		
		SootMethod method = new SootMethod("shadow$" + aroundWeaver.getUniqueID(),
				Util.getTypeListFromLocals(context), returnedLocal==null ? VoidType.v() : returnedLocal.getType(),
				Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
		
		
		
		Body shadowBody = Jimple.v().newBody(method);
		method.setActiveBody(shadowBody);
		
		shadowClass.addMethod(method);
		
		Chain statements=shadowBody.getUnits().getNonPatchingChain();
		
		Stmt nopStmt=Jimple.v().newNopStmt();
		statements.add(nopStmt);
		
		Stmt first;
		HashMap localMap;
		Stmt switchTarget;
		{ // copy shadow into proceed method
			ObjectBox result = new ObjectBox();			
			
			localMap = Util.copyStmtSequence(shadowMethodBody, begin, end,
					shadowBody,
					nopStmt, returnedLocal, result);
			first = (Stmt) result.object;
			if (first == null)
				throw new InternalAroundError();
		}

		AroundWeaver.updateSavedReferencesToStatements(localMap);

		//}
		{ // remove old shadow
			// remove any traps from the shadow before removing the shadow
			Util.removeTraps(shadowMethodBody, begin, end);
			// remove statements except original assignment
			Util.removeStatements(shadowMethodBody, begin, end, null);
			//StmtAdviceApplication stmtAppl = null;
			//if (adviceAppl instanceof StmtAdviceApplication) {
			//	stmtAppl = (StmtAdviceApplication) adviceAppl;
			//	stmtAppl.stmt = null;
			/// just for sanity, because we deleted that stmt
			//}
		}

		// add parameters to static$nnn
		int index=0;
		for (Iterator it=context.iterator(); it.hasNext();index++) {
			Local l=(Local)it.next();
			Local l2=(Local)localMap.get(l);			
			IdentityStmt newIDStmt=Jimple.v().newIdentityStmt(l2, 
					Jimple.v().newParameterRef(l2.getType(), index));
			statements.insertBefore(newIDStmt, nopStmt);
			//shadowBody.getLocals().add(l2);
		}
		
		Tag redirectExceptions;
		{
			List newstmts = new LinkedList();		
			for(Iterator it=statements.iterator();it.hasNext();) {
				newstmts.add(it.next());				
			}
			redirectExceptions = new RedirectedExceptionSpecTag(
				shadowBody, newstmts);
		}
		
		InvokeExpr expr=Jimple.v().newStaticInvokeExpr(method.makeRef(), context);
		Stmt invStmt;
		// insert method call to static$nnn
		if (returnedLocal==null) {
			invStmt=Jimple.v().newInvokeStmt(expr);
		} else {
			invStmt=Jimple.v().newAssignStmt(returnedLocal, expr);
		}
		invStmt.addTag(redirectExceptions);
		method.addTag(new DisableExceptionCheckTag());
		//shadowMethod.addException(shadow)
		
		shadowMethodStatements.insertAfter(invStmt, begin);
		
		
		 
		AroundWeaver.debug("@@@@@@@@@@@@@@@@@@@@2");
		//AroundWeaver.debug(Util.printMethod(shadowMethod));
		AroundWeaver.debug("@@@@@@@@@@@@@@@@@@@@3");
		//AroundWeaver.debug(Util.printMethod(method));
		
		shadowBody.validate();
	}
	public void doWeave() {
		Local lClosure = null;
		SootClass closureClass = null;
		List /*Local*/context = null;
		Local returnedLocal = null;
		List skipDynamicActuals;
		int shadowID;
		int[] argIndex;
		//if (bHasProceed) {
		returnedLocal = findReturnedLocal();
		{
			AroundWeaver.debug("Locals going in: ");
			//debug(Util.printMethod(shadowMethod));
		}

		context = findLocalsGoingIn(shadowMethodBody, begin, end);

		{ // print debug information

			AroundWeaver.debug(" Method: " + shadowMethod.toString());
			AroundWeaver.debug(" Application: " + adviceAppl.toString());
			//debug("Method + " + shadowMethod.toString());
			Iterator it = context.iterator();
			while (it.hasNext()) {
				Local l = (Local) it.next();
				AroundWeaver.debug("  " + l.toString());
			}
		}

		validateShadow(shadowMethodBody, begin, end);

		/*
		 * When inlining, it can be useful to have the shadow in a static method.
		 * Otherwise, the shadow may be inlined twice, once for the failed-case
		 * of the dynamic residue and once for the matched-case.
		 * If inlining is forced, however, everything is supposed to be inlined
		 * regardless of this issue.  
		 */
		if (OptionsParser.v().around_inlining() && // If inlining is *enabled*
			!OptionsParser.v().around_force_inlining()) { /* && // but not forced 
				isShadowBig()){ // and the shadow is big, extract it into a static method.
				*/
			extractShadowIntoStaticMethod(returnedLocal, context);
			this.shadowSize = getShadowSize();
		}
		
		
		List contextActuals;

		if (this.proceedMethod.bUseClosureObject) {
			if (!this.proceedMethod.adviceMethod.hasDynamicProceed) {
				this.proceedMethod.adviceMethod.generateProceedCalls(false, //bStaticProceedMethod 
						true, // bClosure
						null); // proceedMethod
			}
			argIndex = new int[context.size()];
			List types = new LinkedList();
			int i = 0;
			for (Iterator it = context.iterator(); it.hasNext(); i++) {
				Local l = (Local) it.next();
				types.add(l.getType());
				argIndex[i] = i;
				Local argLocal = Restructure.addParameterToMethod(
						this.proceedMethod.sootProceedMethod, l.getType(),
						"contextArg");
				this.proceedMethod.contextParamLocals.add(argLocal);
			}
			contextActuals = Util
					.getDefaultValues(this.proceedMethod.adviceMethod.contextArguments);
			skipDynamicActuals = context;
		} else {
			ObjectBox contextActualsBox = new ObjectBox();
			argIndex = this.proceedMethod.adviceMethod.modifyAdviceMethod(
					context, proceedMethod, contextActualsBox,
					this.proceedMethod.bStaticProceedMethod,
					this.proceedMethod.bUseClosureObject);
			contextActuals = (List) contextActualsBox.object;
			skipDynamicActuals = contextActuals;
		}
		if (this.proceedMethod.bUseClosureObject) {
			closureClass = generateClosure(
					proceedMethod.adviceMethod.interfaceInfo.abstractProceedMethod
							.getName(), this.proceedMethod.sootProceedMethod,
					context);
		}
		// copy shadow into proceed method with a return returning the relevant local.
		Stmt first;
		HashMap localMap;
		Stmt switchTarget;
		{ // copy shadow into proceed method
			ObjectBox result = new ObjectBox();
			if (this.proceedMethod.getLookupStmt() == null)
				throw new InternalAroundError();
			localMap = Util.copyStmtSequence(shadowMethodBody, begin, end,
					this.proceedMethod.proceedMethodBody,
					this.proceedMethod.getLookupStmt(), returnedLocal, result);
			first = (Stmt) result.object;
			if (first == null)
				throw new InternalAroundError();
			switchTarget = Jimple.v().newNopStmt();

			if (first == this.proceedMethod.getLookupStmt())
				throw new InternalAroundError();

			this.proceedMethod.proceedMethodStatements.insertBefore(
					switchTarget, first);
		}

		AroundWeaver.updateSavedReferencesToStatements(localMap);

		// Construct a tag to place on the invokes that are put in place of the removed
		// statements

		List newstmts = new LinkedList();
		Chain units = shadowMethodBody.getUnits();
		Stmt s = (Stmt) units.getSuccOf(begin);
		while (s != end) {
			newstmts.add(localMap.get(s));
			s = (Stmt) units.getSuccOf(s);
		}
		//IntConstant.v()
		Tag redirectExceptions = new RedirectedExceptionSpecTag(
				this.proceedMethod.proceedMethodBody, newstmts);

		//}
		{ // remove old shadow
			// remove any traps from the shadow before removing the shadow
			Util.removeTraps(shadowMethodBody, begin, end);
			// remove statements except original assignment
			Util.removeStatements(shadowMethodBody, begin, end, null);
			//StmtAdviceApplication stmtAppl = null;
			//if (adviceAppl instanceof StmtAdviceApplication) {
			//	stmtAppl = (StmtAdviceApplication) adviceAppl;
			//	stmtAppl.stmt = null;
			/// just for sanity, because we deleted that stmt
			//}
		}

		//if (bHasProceed) {

		{ // determine shadow ID
			if (this.proceedMethod.bUseClosureObject) {
				shadowID = -1; // bogus value, since not used in this case.
			} else if (this.proceedMethod.bStaticProceedMethod) {
				shadowID = this.proceedMethod.nextShadowID++;
			} else {
				shadowID = this.proceedMethod.adviceMethod.getUniqueShadowID();
			}
		}

		boolean bNeedsUnBoxing=
			returnedLocal!=null &&
			this.proceedMethod.adviceMethod.getAdviceReturnType().equals(Scene.v().getSootClass("java.lang.Object").getType()) &&
			Restructure.JavaTypeInfo.isSimpleType(returnedLocal.getType());
		
		this.proceedMethod.shadowInformation.put(new Integer(shadowID),
				new ShadowInlineInfo(this.shadowSize, shadowInternalLocalCount,
				bNeedsUnBoxing	
					));

		if (this.proceedMethod.bUseClosureObject) {
			lClosure = generateClosureCreation(closureClass, context);
		}
		//verifyBindings(staticBindings);
		//}

		Stmt failPoint = Jimple.v().newNopStmt();
		WeavingContext wc = PointcutCodeGen.makeWeavingContext(adviceAppl);
        wc.setShadowTag(new InstructionShadowTag(adviceAppl.shadowmatch.shadowId));
        wc.setSourceTag(new InstructionSourceTag(adviceAppl.advice.sourceId));

		Local bindMaskLocal = null;
		//if (bHasProceed) {
		Residue.Bindings bindings = new Residue.Bindings();

		adviceAppl.getResidue().getAdviceFormalBindings(bindings, null);
		bindings.calculateBitMaskLayout();

		AroundWeaver.debug(" " + bindings);

		{
			LocalGeneratorEx lg = new LocalGeneratorEx(shadowMethodBody);
			bindMaskLocal = lg.generateLocal(IntType.v(), "bindMask");
		}

		AroundWeaver.debug("Residue before modification: "
				+ adviceAppl.getResidue());

		adviceAppl.setResidue(adviceAppl.getResidue()
				.restructureToCreateBindingsMask(bindMaskLocal, bindings));

		AroundWeaver.debug("Residue after modification: "
				+ adviceAppl.getResidue());

		//}

		Stmt endResidue = weaveDynamicResidue(returnedLocal,
				skipDynamicActuals, shadowID, wc, failPoint, redirectExceptions);

		shadowMethodStatements.insertAfter(Jimple.v().newAssignStmt(
				bindMaskLocal, IntConstant.v(0)), begin);

		//List assignments=getAssignmentsToAdviceFormals(begin, endResidue, staticBindings);
		//createBindingMask(assignments, staticBindings, wc, begin, endResidue);

		this.proceedMethod.assignCorrectParametersToLocals(context, argIndex,
				first, localMap, bindings);

		if (!this.proceedMethod.bUseClosureObject)
			this.proceedMethod.modifyLookupStatement(switchTarget, shadowID);

		Local lThis = null;
		if (!bStaticShadowMethod)
			lThis = shadowMethodBody.getThisLocal();

		makeAdviceInvocation(bindMaskLocal, returnedLocal, contextActuals,
				(this.proceedMethod.bUseClosureObject ? lClosure : lThis),
				shadowID, failPoint, wc, new DisableExceptionCheckTag());

		if (abc.main.Debug.v().aroundWeaver)
			this.proceedMethod.sootProceedMethod.getActiveBody().validate();
		
		
	}

	/**
	 * Checks that:
	 * 	No units outside the shadow point to units inside the shadow
	 *  No units inside the shadow point to units outside the shadow, including end and start
	 * @param body
	 * @param begin
	 * @param end
	 */
	private void validateShadow(Body body, Stmt begin, Stmt end) {

		if (!abc.main.Debug.v().aroundWeaver)
			return;

		Chain statements = body.getUnits().getNonPatchingChain();

		if (!statements.contains(begin))
			throw new InternalAroundError();

		if (!statements.contains(end))
			throw new InternalAroundError();

		boolean insideRange = false;

		Iterator it = statements.iterator();
		while (it.hasNext()) {
			Stmt s = (Stmt) it.next();
			if (s == begin) {
				if (insideRange)
					throw new InternalAroundError();

				insideRange = true;
			}

			if (s == end) {
				if (!insideRange)
					throw new InternalAroundError();

				insideRange = false;
			}

			List unitBoxes = s.getUnitBoxes();
			Iterator it2 = unitBoxes.iterator();
			while (it2.hasNext()) {
				UnitBox box = (UnitBox) it2.next();
				if (insideRange) {
					if (!Util.isInSequence(body, begin, end, box.getUnit())) {
						if (box.getUnit() == end) {
							throw new InternalAroundError(
									"Unit in shadow points to endshadow");
						} else if (box.getUnit() == begin) {
							throw new InternalAroundError(
									"Unit in shadow points to beginshadow");
						} else
							throw new InternalAroundError(
									"Unit in shadow points outside of the shadow"
											+ body.toString());
					}
				} else {
					if (Util.isInSequence(body, begin, end, box.getUnit())) {
						throw new InternalAroundError(
								"Unit outside of shadow points inside the shadow");
					}
				}
			}
		}
	}

	/**
	 * 
	 * Algorithm:
	 * 	Find all locals which are used in the range.
	 *  Intersect with all locals written to outside the range.
	 * 
	 * @param body
	 * @param begin
	 * @param end
	 */
	private List findLocalsGoingIn(Body body, Stmt begin, Stmt end) {
		Chain statements = body.getUnits().getNonPatchingChain();

		if (!statements.contains(begin))
			throw new InternalAroundError();

		if (!statements.contains(end))
			throw new InternalAroundError();

		Set usedInside = new HashSet();

		Set definedOutside = new HashSet();

		boolean insideRange = false;
		{
			Iterator it = statements.iterator();
			while (it.hasNext()) {
				Stmt s = (Stmt) it.next();
				if (s == begin) {
					if (insideRange)
						throw new InternalAroundError();

					insideRange = true;
				}

				if (s == end) {
					if (!insideRange)
						throw new InternalAroundError();

					insideRange = false;
				}

				if (insideRange) {
					List useBoxes = s.getUseBoxes();
					Iterator it2 = useBoxes.iterator();
					while (it2.hasNext()) {
						ValueBox box = (ValueBox) it2.next();
						if (box.getValue() instanceof Local) {
							Local l = (Local) box.getValue();
							usedInside.add(l);
						}
					}
				} else {
					List defBoxes = s.getDefBoxes();
					Iterator it2 = defBoxes.iterator();
					while (it2.hasNext()) {
						ValueBox box = (ValueBox) it2.next();
						if (box.getValue() instanceof Local) {
							Local l = (Local) box.getValue();
							definedOutside.add(l);
						}
					}
				}
			}
		}

		int usedInsideCount = usedInside.size();

		List result = new LinkedList(usedInside);
		result.retainAll(definedOutside);
		
		// Remove cflow thread-locals. We musn't pass them as this is incorrect if 
		// the around creates a new thread. The thread-locals are regenerated inside
		// as needed
		// DS, based on a patch by Bruno Harbulot
		
		for (Iterator localIt = usedInside.iterator() ; localIt.hasNext() ;) {
			Local l = (Local)localIt.next() ;
			if (CflowCodeGenUtils.isThreadLocalType(l.getType())) {
				// Remove the cflow thread-local
				result.remove(l) ;
			}
		}

		shadowInternalLocalCount = usedInsideCount - result.size();
		return result;
	}

	private Stmt weaveDynamicResidue(Local returnedLocal, List contextActuals,
			int shadowID, WeavingContext wc, Stmt failPoint, Tag attachToInvoke) {
		LocalGeneratorEx localgen = new LocalGeneratorEx(shadowMethodBody);

		shadowMethodStatements.insertBefore(failPoint, end);

		// weave in residue
		Stmt endResidue = adviceAppl.getResidue().codeGen(shadowMethod,
				localgen, shadowMethodStatements, begin, failPoint, true, wc);

		// debug("weaving residue: " + adviceAppl.residue);
		if (adviceAppl.getResidue() instanceof AlwaysMatch //||
		//adviceAppl.getResidue() instanceof AspectOf
		) { ///TODO: work out proper solution with ganesh!!!!!
			// can't fail
		} else {
			InvokeExpr directInvoke;
			List directParams = new LinkedList();

			List defaultValues = Util
					.getDefaultValues(this.proceedMethod.adviceMethod.originalAdviceFormalTypes);
			directParams.addAll(defaultValues);
			directParams.add(IntConstant.v(shadowID));
			directParams.add(IntConstant.v(1)); //  bindMask parameter (1 => skip)
			directParams.addAll(contextActuals);
			if (this.proceedMethod.bUseClosureObject) {
				directInvoke = Jimple.v().newStaticInvokeExpr(
						this.proceedMethod.sootProceedMethod.makeRef(),
						directParams);
			} else if (this.proceedMethod.bStaticProceedMethod) {
				directInvoke = Jimple.v().newStaticInvokeExpr(
						this.proceedMethod.sootProceedMethod.makeRef(),
						directParams);
			} else {
				// TODO: can this call be replaced with an InvokeSpecial?
				directInvoke = Jimple
						.v()
						.newInterfaceInvokeExpr(
								shadowMethodBody.getThisLocal(),
								proceedMethod.adviceMethod.interfaceInfo.abstractProceedMethod
										.makeRef(), directParams);
			}
			{
				Stmt skipAdvice;
				boolean bDidUnbox=false;
				if (returnedLocal != null) {
					AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal,
							directInvoke);
					shadowMethodStatements.insertAfter(assign, failPoint);
					bDidUnbox=Restructure
							.insertBoxingCast(shadowMethodBody, assign, true);
					skipAdvice = assign;
				} else {
					skipAdvice = Jimple.v().newInvokeStmt(directInvoke);
					shadowMethodStatements.insertAfter(skipAdvice, failPoint);
				}
				skipAdvice.addTag(attachToInvoke);
				skipAdvice.addTag(new AroundShadowInfoTag(new ShadowInlineInfo(
						shadowSize, shadowInternalLocalCount, bDidUnbox)));
				this.proceedMethod.adviceMethod.directInvocationStmts
						.add(skipAdvice);
			}
		}
		return endResidue;
	}

	public SootClass generateClosure(String closureRunMethodName,
			SootMethod targetProceedMethod, List /*Local*/context) {

		final String packageName = targetProceedMethod.getDeclaringClass()
				.getPackageName();
		final String className = (packageName.length() == 0 ? "" : packageName
				+ ".")
				+ "Abc$closure$" + AroundWeaver.v().getUniqueID();
		SootClass closureClass = new SootClass(className, Modifier.PUBLIC);

		closureClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		//closureClass.implementsInterface(interfaceName);
		closureClass
				.addInterface(proceedMethod.adviceMethod.interfaceInfo.closureInterface);

		//SootMethod cons=new SootMethod()
		AroundWeaver.debug(" "
				+ Scene.v().getSootClass("java.lang.RuntimeException")
						.getMethod("<init>", new LinkedList()));
		//if (closureClass!=null)
		//	throw new RuntimeException();
		SootMethod cons = new SootMethod("<init>", new LinkedList(), VoidType
				.v(), Modifier.PUBLIC);
		closureClass.addMethod(cons);
		cons.setActiveBody(Jimple.v().newBody(cons));
		{
			Body b = cons.getActiveBody();
			Chain statements = b.getUnits().getNonPatchingChain();
			LocalGeneratorEx lg = new LocalGeneratorEx(b);
			Local lThis = lg.generateLocal(closureClass.getType(), "this");
			statements.addFirst(Jimple.v().newIdentityStmt(lThis,
					Jimple.v().newThisRef(closureClass.getType())));

			statements.addLast(Jimple.v().newInvokeStmt(
					Jimple.v().newSpecialInvokeExpr(
							lThis,
							Scene.v().getSootClass("java.lang.Object")
									.getMethod("<init>", new LinkedList())
									.makeRef())));
			statements.addLast(Jimple.v().newReturnVoidStmt());
            Tagger.tagChain(statements, InstructionKindTag.CLOSURE_INIT);
            Tagger.tagChain(statements, new InstructionSourceTag(adviceAppl.advice.sourceId));
		}

		SootMethod runMethod = new SootMethod(closureRunMethodName,
				new LinkedList(), this.proceedMethod.adviceMethod
						.getAdviceReturnType(), Modifier.PUBLIC);

		closureClass.addMethod(runMethod);
		//signature.setActiveBody(Jimple.v().newBody(signature));
		this.proceedMethod.adviceMethod.closureProceedMethods.add(runMethod);

		Scene.v().addClass(closureClass);
		closureClass.setApplicationClass();

		Body body = Jimple.v().newBody(runMethod);
		runMethod.setActiveBody(body);

		LocalGeneratorEx lg = new LocalGeneratorEx(body);

		Chain statements = body.getUnits().getNonPatchingChain();

		Local lThis = lg.generateLocal(closureClass.getType(), "this");
		statements.addFirst(Jimple.v().newIdentityStmt(lThis,
				Jimple.v().newThisRef(closureClass.getType())));

		List invokeLocals = new LinkedList();
		{
			int i = 0;
			for (Iterator it = proceedMethod.adviceMethod.interfaceInfo.abstractProceedMethod
					.getParameterTypes().iterator(); it.hasNext(); i++) {
				Type t = (Type) it.next();
				Local l = Restructure.addParameterToMethod(runMethod, t, "arg");

				// shadowID, bindMask, advice-formals
				if (i < 1 + 1 + this.proceedMethod.adviceMethod.originalAdviceFormalTypes
						.size())
					invokeLocals.add(l);
			}
		}

		Util.validateMethod(runMethod);

		{
			int i = 0;
			for (Iterator it = context.iterator(); it.hasNext(); i++) {
				Local l = (Local) it.next();
				SootField f = new SootField("context" + i, l.getType(),
						Modifier.PUBLIC);
				closureClass.addField(f);
				AroundWeaver.debug("1" + f.getType() + " : " + l.getType());
				Local lTmp = lg.generateLocal(l.getType());
				AssignStmt as = Jimple.v().newAssignStmt(lTmp,
						Jimple.v().newInstanceFieldRef(lThis, f.makeRef()));
				statements.add(as);
				invokeLocals.add(lTmp);
			}
		}

		if (targetProceedMethod.getParameterCount() != invokeLocals.size()) {
			throw new InternalAroundError("proceed method: "
					+ targetProceedMethod.getSignature() + " invoke locals: "
					+ invokeLocals);
		}

		InvokeExpr invEx = Jimple.v().newStaticInvokeExpr(
				targetProceedMethod.makeRef(), invokeLocals);
		if (this.proceedMethod.adviceMethod.getAdviceReturnType().equals(
				VoidType.v())) {
			statements.add(Jimple.v().newInvokeStmt(invEx));
			statements.add(Jimple.v().newReturnVoidStmt());
		} else {
			Local returnedLocal = lg
					.generateLocal(this.proceedMethod.adviceMethod
							.getAdviceReturnType());
			AssignStmt as = Jimple.v().newAssignStmt(returnedLocal, invEx);
			statements.add(as);
			statements.add(Jimple.v().newReturnStmt(returnedLocal));
		}

		Util.validateMethod(runMethod);

		return closureClass;
	}

	public Local generateClosureCreation(SootClass closureClass,
			List /*Local*/context) {

		LocalGeneratorEx lg = new LocalGeneratorEx(shadowMethodBody);
		Local l = lg.generateLocal(closureClass.getType(), "closure");
		Stmt newStmt = Jimple.v().newAssignStmt(l,
				Jimple.v().newNewExpr(closureClass.getType()));
        Tagger.tagStmt(newStmt, InstructionKindTag.ADVICE_ARG_SETUP);
        Tagger.tagStmt(newStmt, new InstructionSourceTag(adviceAppl.advice.sourceId));
        Tagger.tagStmt(newStmt, new InstructionShadowTag(adviceAppl.shadowmatch.shadowId));
		//					Stmt init = Jimple.v().newInvokeStmt(
		//						Jimple.v().newSpecialInvokeExpr(l, 
		//							Scene.v().getSootClass("java.lang.Object").getMethod("<init>", new ArrayList())));
		Stmt init = Jimple.v().newInvokeStmt(
				Jimple.v().newSpecialInvokeExpr(l,
						closureClass.getMethodByName("<init>").makeRef()));//, new ArrayList())));
        Tagger.tagStmt(init, InstructionKindTag.ADVICE_ARG_SETUP);
        Tagger.tagStmt(init, new InstructionSourceTag(adviceAppl.advice.sourceId));
        Tagger.tagStmt(init, new InstructionShadowTag(adviceAppl.shadowmatch.shadowId));

		shadowMethodStatements.insertAfter(init, begin);
		shadowMethodStatements.insertAfter(newStmt, begin);
		int i = 0;
		for (Iterator it = context.iterator(); it.hasNext(); i++) {
			Local lContext = (Local) it.next();
			SootField f = closureClass.getFieldByName("context" + i);
			AroundWeaver.debug("2" + f.getType() + " : " + lContext.getType());
			AssignStmt as = Jimple.v().newAssignStmt(
					Jimple.v().newInstanceFieldRef(l, f.makeRef()), lContext);
			if (!f.getType().equals(lContext.getType()))
				throw new InternalAroundError("" + f.getType() + " : "
						+ lContext.getType());
			shadowMethodStatements.insertAfter(as, init);
            Tagger.tagStmt(as, InstructionKindTag.ADVICE_ARG_SETUP);
            Tagger.tagStmt(as, new InstructionSourceTag(adviceAppl.advice.sourceId));
            Tagger.tagStmt(as, new InstructionShadowTag(adviceAppl.shadowmatch.shadowId));
		}
		return l;
	}

	private void makeAdviceInvocation(Local bindMaskLocal, Local returnedLocal,
			List contextActuals, Local lThis, int shadowID,
			Stmt insertionPoint, WeavingContext wc, Tag attachToInvoke) {
		LocalGeneratorEx lg = new LocalGeneratorEx(shadowMethodBody);
		Chain invokeStmts = adviceAppl.advice.makeAdviceExecutionStmts(
				adviceAppl, lg, wc);

		VirtualInvokeExpr invokeEx = (VirtualInvokeExpr) ((InvokeStmt) invokeStmts
				.getLast()).getInvokeExpr();
		Local aspectRef = (Local) invokeEx.getBase();

		{
			Stmt last = (Stmt) invokeStmts.getLast();
			NopStmt nop = Jimple.v().newNopStmt();
			invokeStmts.insertBefore(nop, last);
			last.redirectJumpsToThisTo(nop);
			invokeStmts.removeLast();
		}
		for (Iterator stmtlist = invokeStmts.iterator(); stmtlist.hasNext();) {
			Stmt nextstmt = (Stmt) stmtlist.next();
			if (nextstmt == null)
				throw new InternalAroundError();
			if (insertionPoint == null)
				throw new InternalAroundError();
			if (shadowMethodStatements == null)
				throw new InternalAroundError();
			if (!shadowMethodStatements.contains(insertionPoint))
				throw new InternalAroundError();
			shadowMethodStatements.insertBefore(nextstmt, insertionPoint);
		}

		// we need to add some of our own parameters to the invocation
		List params = new LinkedList();
		if (this.proceedMethod.bUseClosureObject) {
			if (lThis == null)
				throw new InternalAroundError();

			params.add(lThis); // pass the closure
		} else if (this.proceedMethod.bStaticProceedMethod) {
			params.add(NullConstant.v());
		} else {
			if (lThis == null)
				throw new InternalAroundError();

			params.add(lThis); // pass the closure
		}
		//params.add(targetLocal);
		params.add(IntConstant.v(shadowID));
		if (this.proceedMethod.bUseClosureObject) {
			params.add(IntConstant.v(0));
		} else if (this.proceedMethod.bStaticProceedMethod) { // pass the static class id
			params.add(IntConstant.v(this.proceedMethod.adviceMethod
					.getStaticDispatchTypeID(shadowClass.getType())));
		} else {
			params.add(IntConstant.v(0));
		}
		//params.add(targetLocal);
		params.add(bindMaskLocal);

		// and add the original parameters 
		params.addAll(0, invokeEx.getArgs());

		params.addAll(contextActuals);

		// generate a new invoke expression to replace the old one
		VirtualInvokeExpr invokeEx2 = Jimple.v().newVirtualInvokeExpr(
				aspectRef,
				this.proceedMethod.adviceMethod.sootAdviceMethod.makeRef(),
				params);

		Stmt invokeStmt;
		boolean bDidUnbox=false;
		if (returnedLocal == null) {
			invokeStmt = Jimple.v().newInvokeStmt(invokeEx2);
			shadowMethodStatements.insertBefore(invokeStmt, insertionPoint);
		} else {
			AssignStmt assign = Jimple.v().newAssignStmt(returnedLocal,
					invokeEx2);
			shadowMethodStatements.insertBefore(assign, insertionPoint);
			bDidUnbox=Restructure.insertBoxingCast(shadowMethod.getActiveBody(), assign,
					true);
			
			invokeStmt = assign;
		}
		invokeStmt.addTag(attachToInvoke);
		invokeStmt.addTag(new AroundShadowInfoTag(new ShadowInlineInfo(
				shadowSize, shadowInternalLocalCount, bDidUnbox)));
        Tagger.tagStmt(invokeStmt, InstructionKindTag.ADVICE_EXECUTE);
        Tagger.tagStmt(invokeStmt, new InstructionShadowTag(adviceAppl.shadowmatch.shadowId));
        Tagger.tagStmt(invokeStmt, new InstructionSourceTag(adviceAppl.advice.sourceId));
		
		Stmt beforeEnd = Jimple.v().newNopStmt();
		shadowMethodStatements.insertBefore(beforeEnd, end);
		shadowMethodStatements.insertBefore(Jimple.v().newGotoStmt(beforeEnd),
				insertionPoint);

		if (invokeStmt == null)
			throw new InternalAroundError();

		this.proceedMethod.adviceMethod.adviceMethodInvocationStmts
				.add(invokeStmt);

		//if (abc.main.Debug.v().aroundWeaver)
		//	shadowMethodBody.validate();
	}

	private Local findReturnedLocal() {
		Value v = adviceAppl.shadowmatch.getReturningContextValue()
				.getSootValue();

		if (v instanceof Local) {
			return (Local) v;
		} else {
			LocalGeneratorEx lg = new LocalGeneratorEx(shadowMethodBody);
			Type type = this.proceedMethod.adviceMethod.getAdviceReturnType();
			if (type.equals(VoidType.v())) {
				return null;
			} else {
				Local l = lg.generateLocal(type, "returnedLocal");
				Stmt s = Jimple.v().newAssignStmt(l, v);
				shadowMethodStatements.insertAfter(s, begin);
				return l;
			}
		}
	}

	private AdviceDecl getAdviceDecl(SootMethod method) {
		List l = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getAdviceDecls();
		for (Iterator it = l.iterator(); it.hasNext();) {
			AdviceDecl decl = (AdviceDecl) it.next();
			if (decl.getImpl().getSootMethod().equals(method)) {
				return decl;
			}
		}
		throw new InternalAroundError();
	}

	public final Stmt begin;

	public final Stmt end;

	public final AdviceApplication adviceAppl;

	//public final AdviceMethod adviceMethod;
	public final SootClass shadowClass;

	public final SootMethod shadowMethod;

	public final Chain shadowMethodStatements;

	public final Body shadowMethodBody;

	public final boolean bStaticShadowMethod;
	//public final boolean bUseStaticProceedMethod;
	//public final AdviceMethod.ProceedMethod proceedMethod;	
}
