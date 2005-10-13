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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;



public class AdviceLocalMethod {
	public final AdviceLocalClass enclosingClass;

	public void modifyNestedInits(List addedAdviceParameterLocals) {
		AdviceLocalMethod pm=this;
		for (Iterator it0=pm.nestedInitCalls.iterator(); it0.hasNext();) {
			AdviceLocalMethod.NestedInitCall nc=
				(AdviceLocalMethod.NestedInitCall)it0.next();
			
			if (addedAdviceParameterLocals.size()!=nc.adviceLocalClass.addedFields.size())
				throw new InternalAroundError();
			
			Iterator it=addedAdviceParameterLocals.iterator();
			Iterator it1=nc.adviceLocalClass.addedFields.iterator();
			while (it.hasNext()) {
				Local l=(Local)it.next();
				SootField f=(SootField)it1.next();
				Stmt ns=
					Jimple.v().newAssignStmt(
							Jimple.v().newInstanceFieldRef(
									nc.baseLocal,
									f.makeRef()), l);
				pm.methodBody.getUnits().getNonPatchingChain().insertAfter(ns, nc.statement);
			}
		}
	}
	public void setDefaultParameters(List addedAdviceParameterLocals) {
		AdviceLocalMethod pm=this;
		int i=0;
		for (Iterator it=addedAdviceParameterLocals.iterator();it.hasNext();i++) {
			Local l=(Local)it.next();
		switch(i) {
			case 0: pm.interfaceLocal=l;
				 	l.setName("closureInterface" + AroundWeaver.v().getUniqueID()); break;
			case 1: pm.idLocal=l; 
					l.setName("shadowID" + AroundWeaver.v().getUniqueID());break;
			case 2: pm.staticDispatchLocal=l; 
					l.setName("staticClassID" + AroundWeaver.v().getUniqueID()); break;
			case 3: pm.bindMaskLocal=l;
					l.setName("bindMask" + AroundWeaver.v().getUniqueID());break;
			default: throw new InternalAroundError();
			}
		}
		pm.implicitProceedParameters.add(pm.idLocal);
		pm.implicitProceedParameters.add(pm.bindMaskLocal);
	}
	public class NestedInitCall {
		public NestedInitCall(final Stmt statement, 
				final AdviceLocalClass proceedClass,
				final Local baseLocal) {
			super();
			this.statement = statement;
			this.adviceLocalClass = proceedClass;
			this.baseLocal=baseLocal;
		}
		public final Stmt statement;
		public final AdviceLocalClass adviceLocalClass;
		public final Local baseLocal;
	}
	Local interfaceLocal;
	 Local staticDispatchLocal;
	private Local idLocal;
	private Local bindMaskLocal;
	
	public Local contextArgfieldBaseLocal;
	
	public final List implicitProceedParameters=new LinkedList();
	
	public void modifyInterfaceInvocations(List addedAdviceParameterLocals,
			List addedAdviceParameterTypes) {
		if (addedAdviceParameterLocals.size()!=addedAdviceParameterTypes.size())
			throw new InternalAroundError();
		// Modify the interface invocations. These must all be in the advice method.
		// This constraint is violated by adviceexecution() pointcuts.
		Iterator it = interfaceInvocationStmts.iterator();
		Chain statements=methodBody.getUnits().getNonPatchingChain();
		while (it.hasNext()) {
			Stmt stmt = (Stmt) it.next();
			if (!statements.contains(stmt))
				throw new InternalAroundError();
		
			InvokeExpr intfInvoke = stmt.getInvokeExpr();
			
			/*if (intfInvoke.getArgCount()!=
				intfInvoke.getMethodRef().parameterTypes().size())
				throw new InternalAroundError(
						" "  + intfInvoke);*/
			
			List params = new LinkedList(intfInvoke.getArgs());
			params.addAll(addedAdviceParameterLocals);
			List types=adviceMethod.interfaceInfo.abstractProceedMethod.getParameterTypes();// new LinkedList(intfInvoke.getMethodRef().parameterTypes()); 
			//types.addAll(addedAdviceParameterTypes);
			if (params.size()!=types.size())
				throw new InternalAroundError();
			
			InvokeExpr newInvoke = Util.createNewInvokeExpr(intfInvoke, params, types);
			//debug("newInvoke: " + newInvoke);
			stmt.getInvokeExprBox().setValue(newInvoke);
			//debug("newInvoke2" + stmt.getInvokeExpr());
			if (newInvoke.getArgCount()!=newInvoke.getMethodRef().parameterTypes().size())
				throw new InternalAroundError(
						"Signature: " + newInvoke.getMethodRef().getSignature() + 
						" Args: " + newInvoke.toString());
			
			/*try {
				newInvoke.getMethod();
			} catch (Exception e) {
				throw new InternalAroundError(
					"Signature: " + newInvoke.getMethodRef().getSignature() + 
					" Interface: " + interfaceInfo.abstractProceedMethod.getSignature()	+
					" message: " + e.getMessage()
				);
			}*/
		}
		implicitProceedParameters.addAll(addedAdviceParameterLocals);				
	}
	boolean isAdviceMethod() {
		return sootProceedCallMethod.equals(this.enclosingClass.adviceMethod.sootAdviceMethod);
	}
	
	private final Set nestedInitCalls=new HashSet();
	public final NopStmt nopAfterEnclosingLocal;
	public final int originalSize;
	public final int internalLocalCount;
	
	public final AdviceMethod adviceMethod;
	public AdviceLocalMethod(AdviceLocalClass enclosingClass, AdviceMethod adviceMethod, SootMethod method) {
		//this.adviceMethod=adviceMethod;
		this.sootProceedCallMethod=method;
		this.enclosingClass = enclosingClass;
		this.adviceMethod=enclosingClass.adviceMethod;
		this.methodBody=method.getActiveBody();			
	
		this.originalSize=methodBody.getUnits().size();
		this.internalLocalCount =
			methodBody.getLocalCount() - 
			method.getParameterCount();
		if (internalLocalCount<0)
			throw new InternalAroundError();
			
		AroundWeaver.debug("YYYYYYYYYYYYYYYYYYY creating ProceedCallMethod " + method);
		
		this.nopAfterEnclosingLocal=Jimple.v().newNopStmt();
		
		Chain methodStatements=methodBody.getUnits().getNonPatchingChain();			
		
		for (Iterator it = methodStatements.snapshotIterator();
			it.hasNext();) {
			Stmt s = (Stmt) it.next();
			InvokeExpr invokeEx;
			try {
				invokeEx = s.getInvokeExpr();
			} catch (Exception ex) {
				invokeEx = null;
			}

			if (invokeEx != null) {
				if (invokeEx.getMethodRef().name().startsWith("proceed$")) { 							
					ProceedInvocation invocation = 
						new ProceedInvocation(
								this, invokeEx.getArgs(), s);
					proceedInvocations.add(invocation);							
				}
				// find <init> calls to local/anonymous classes
				if (invokeEx instanceof SpecialInvokeExpr) {
					 
					SpecialInvokeExpr si=(SpecialInvokeExpr) invokeEx;
					Local baseLocal=(Local)si.getBase();
					SootClass baseClass=((RefType)baseLocal.getType()).getSootClass();
					if (!baseClass.equals(this.enclosingClass.adviceMethod.getAspect())) {									
						if (si.getMethodRef().name().equals("<init>") && 
							!this.methodBody.getThisLocal().equals(baseLocal) ) {
							if (adviceMethod.adviceLocalClasses.containsKey(baseClass)) {
								AroundWeaver.debug("WWWWWWWWWWWW base class: " + baseClass);
								AdviceLocalClass pl=
									(AdviceLocalClass)
										adviceMethod.adviceLocalClasses.get(baseClass);
								
								if (pl.isFirstDegree()) {											
									nestedInitCalls.add(
											new NestedInitCall(s, pl, 
													(Local)si.getBase()));
								}
							//	nestedFirstDegreeClasses.add(baseClass);
							}
						}
					}
				}
			}
		}
		Stmt insert=Restructure.findFirstRealStmtOrNop(method, methodStatements);
		
		methodStatements.insertBefore(nopAfterEnclosingLocal, insert);
		insert=nopAfterEnclosingLocal;
		if (isAdviceMethod()) {
			contextArgfieldBaseLocal=null;						
		} else {
			if (this.enclosingClass.isFirstDegree()) {
				contextArgfieldBaseLocal=methodBody.getThisLocal();
			} else {
				LocalGeneratorEx lg=new LocalGeneratorEx(methodBody);
				SootClass cl=this.enclosingClass.sootClass;
				
				Local lBase=methodBody.getThisLocal();
				
				
				while (true)  {
					AroundWeaver.debug(" Class: " + cl);
					SootField f=cl.getFieldByName("this$0");
					
					if (!this.enclosingClass.adviceMethod.adviceLocalClasses.containsKey(((RefType)f.getType()).getSootClass()))
						throw new InternalAroundError(" " + ((RefType)f.getType()).getSootClass());		
					
					AdviceLocalClass pl=(AdviceLocalClass)this.enclosingClass.adviceMethod.adviceLocalClasses.get(((RefType)f.getType()).getSootClass());
					
					Local l=lg.generateLocal(f.getType(), "enclosingLocal");
					AssignStmt as=Jimple.v().newAssignStmt(
							l, Jimple.v().newInstanceFieldRef(
									lBase, f.makeRef()));
					methodStatements.insertBefore(as , insert);
					if (pl.isFirstDegree()) {
						contextArgfieldBaseLocal=l;
						break;
					} else {
						lBase=l;
						cl=pl.sootClass;
					}
				}							
				if (contextArgfieldBaseLocal==null)
					throw new InternalAroundError();
			}
		} 
	}
	public void generateProceeds(ProceedMethod proceedMethod, String newStaticInvoke) {
		for (Iterator it=proceedInvocations.iterator(); it.hasNext();) {
			ProceedInvocation inv=(ProceedInvocation)it.next();
			inv.generateProceed(proceedMethod, newStaticInvoke);
		}				
	}
	public final List proceedInvocations=new LinkedList();
	
	//private final AdviceMethod adviceMethod;
	public final SootMethod sootProceedCallMethod;
	public final Body methodBody;
	//private final Chain statements;
	public final Set interfaceInvocationStmts = new HashSet();
}
