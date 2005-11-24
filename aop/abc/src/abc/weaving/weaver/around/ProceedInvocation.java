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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.tagkit.InstructionKindTag;
import abc.weaving.tagkit.Tagger;


public class ProceedInvocation {
	private final AdviceLocalMethod enclosingMethod;

	private final int ID;

	public ProceedInvocation(
				AdviceLocalMethod method, List originalActuals, Stmt originalStmt) {					
		
		this.originalActuals.addAll(originalActuals);
		this.enclosingMethod = method;
		this.ID=method.adviceMethod.aroundWeaver.getUniqueID();
		
		
		this.begin = Jimple.v().newNopStmt();
		this.end = Jimple.v().newNopStmt();
		this.end.addTag(new AroundWeaver.LookupStmtTag(ID, false));
		if (originalStmt instanceof AssignStmt) {
			lhs = (Local) (((AssignStmt) originalStmt).getLeftOp());
		}
		Chain statements=this.enclosingMethod.methodBody.getUnits().getNonPatchingChain();
		statements.insertBefore(begin, originalStmt);
		statements.insertAfter(end, originalStmt);
		originalStmt.redirectJumpsToThisTo(begin);
		AroundWeaver.debug("Removing original statement: " + originalStmt);
		statements.remove(originalStmt);
	}
				
	private Local lhs;
	final public NopStmt begin;
	final public NopStmt end;

	//List lookupValues=new LinkedList();
	List defaultTargetStmts;
	//Stmt lookupStmt;
	final List staticInvokes = new LinkedList();
	final List staticLookupValues = new LinkedList();

	Stmt dynamicInvoke;

	final List originalActuals = new LinkedList();

	
	
	public void generateProceed(ProceedMethod proceedMethod, String newStaticInvokeClassName) {
		AroundWeaver.debug("1YYYYYYYYYYYYYY generateProceed()");
		//debug(Util.printMethod(sootProceedCallMethod));
		Util.removeStatements(this.enclosingMethod.methodBody, begin, end, null);
		AroundWeaver.debug("YYYYYYYYYYYYYY generateProceed()" + this.enclosingMethod.enclosingClass.adviceMethod.sootAdviceMethod);
		List parameters = new LinkedList();
		parameters.addAll(this.originalActuals);
		AroundWeaver.debug(" param count: " + parameters.size());
		parameters.addAll(this.enclosingMethod.implicitProceedParameters);
		AroundWeaver.debug(" param count: " + parameters.size());
		if (this.dynamicInvoke == null && this.enclosingMethod.enclosingClass.adviceMethod.hasDynamicProceed) {
			/*if (parameters.size()!=
				interfaceInfo.abstractProceedMethod.getParameterCount())
				throw new InternalAroundError(
						"Signature " + interfaceInfo.abstractProceedMethod.getSignature() +
						" Parameters " + parameters.toString());
				*/		
			InvokeExpr newInvokeExpr = Jimple.v().newInterfaceInvokeExpr(
						this.enclosingMethod.interfaceLocal, 
						this.enclosingMethod.enclosingClass.adviceMethod.interfaceInfo.abstractProceedMethod.makeRef(), parameters);
			
			/*if (newInvokeExpr.getArgCount()!=
				interfaceInfo.abstractProceedMethod.getParameterCount())
				throw new InternalAroundError(
						"Signature " + interfaceInfo.abstractProceedMethod.getSignature() +
						" Parameters " + parameters.toString());
			
			while (newInvokeExpr.getMethodRef().parameterTypes().size()>parameters.size())
				newInvokeExpr.getMethodRef().parameterTypes().remove(
						newInvokeExpr.getMethodRef().parameterTypes().get(
								newInvokeExpr.getMethodRef().parameterTypes().size()-1
						)
				);
			*/
			
			Stmt s;
			if (this.lhs == null) {
				s = Jimple.v().newInvokeStmt(newInvokeExpr);
			} else {
				s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
			}
            Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
			this.dynamicInvoke = s;
			this.enclosingMethod.interfaceInvocationStmts.add(s);
		}
		
		if (newStaticInvokeClassName != null) {
			SootClass cl = Scene.v().getSootClass(newStaticInvokeClassName);

			this.staticLookupValues.add(IntConstant.v(this.enclosingMethod.enclosingClass.adviceMethod.getStaticDispatchTypeID(cl.getType())));
		
			InvokeExpr newInvokeExpr = Jimple.v().newStaticInvokeExpr(
					proceedMethod.sootProceedMethod.makeRef(), parameters);
			
			
			Stmt s;
			if (this.lhs == null) {
				s = Jimple.v().newInvokeStmt(newInvokeExpr);
			} else {
				s = Jimple.v().newAssignStmt(this.lhs, newInvokeExpr);
			}
            Tagger.tagStmt(s, InstructionKindTag.AROUND_PROCEED);
			this.staticInvokes.add(s);
			this.enclosingMethod.interfaceInvocationStmts.add(s);
		}
		if (this.defaultTargetStmts == null) {
			//				generate exception code (default target)
			this.defaultTargetStmts = new LinkedList();
			LocalGeneratorEx lg = new LocalGeneratorEx(this.enclosingMethod.methodBody);
			SootClass exception = Scene.v().getSootClass("java.lang.RuntimeException");
			Local ex = lg.generateLocal(exception.getType(), "exception");
			Stmt newExceptStmt = Jimple.v().newAssignStmt(ex, Jimple.v().newNewExpr(exception.getType()));
			Stmt initEx = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(ex, exception.getMethod("<init>", new ArrayList()).makeRef()));
			Stmt throwStmt = Jimple.v().newThrowStmt(ex);
			this.defaultTargetStmts.add(newExceptStmt);
			this.defaultTargetStmts.add(initEx);
			this.defaultTargetStmts.add(throwStmt);
		}
		
		Chain statements=this.enclosingMethod.methodBody.getUnits().getNonPatchingChain();
		if (this.enclosingMethod.enclosingClass.adviceMethod.staticProceedTypes.isEmpty()) {
			statements.insertAfter(this.dynamicInvoke, this.begin);
		} else if (this.enclosingMethod.enclosingClass.adviceMethod.hasDynamicProceed == false && this.enclosingMethod.enclosingClass.adviceMethod.staticProceedTypes.size() == 1) {
			statements.insertAfter(this.staticInvokes.get(0), this.begin);
		} else {
			List targets = new LinkedList();
			List lookupValues = new LinkedList();
			if (this.dynamicInvoke != null) {
				targets.add(this.dynamicInvoke);
				lookupValues.add(IntConstant.v(0));
			}
			targets.addAll(this.staticInvokes);
			lookupValues.addAll(this.staticLookupValues);
		
			Local key = this.enclosingMethod.staticDispatchLocal; ///
			Stmt lookupStmt = Util.newSwitchStmt(//Jimple.v().newLookupSwitchStmt(									
					key, lookupValues, targets, (Unit) this.defaultTargetStmts.get(0));
			lookupStmt.addTag(new AroundWeaver.LookupStmtTag(ID, true));
			
			statements.insertBefore(lookupStmt, this.end);
			if (this.dynamicInvoke != null) {
				statements.insertBefore(this.dynamicInvoke, this.end);
				statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
			}
		
			Iterator it2 = this.staticInvokes.iterator();
			while (it2.hasNext()) {
				Stmt stmt = (Stmt) it2.next();
				statements.insertBefore(stmt, this.end);
				statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
			}
			it2 = this.defaultTargetStmts.iterator();
			while (it2.hasNext()) {
				Stmt stmt = (Stmt) it2.next();
				statements.insertBefore(stmt, this.end);
			}
			// just in case: // TODO: what for?
			//statements.insertBefore(Jimple.v().newGotoStmt(this.end), this.end);
		}
	}

}
