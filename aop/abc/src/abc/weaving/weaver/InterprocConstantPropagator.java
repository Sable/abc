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

package abc.weaving.weaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Value;
import soot.ValueBox;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.util.Chain;
import abc.weaving.weaver.around.Util;

/**
 * @author Sascha Kuzins
 */
public class InterprocConstantPropagator {
	static boolean considerInstanceMethod(String methodName) {
		return 
			Util.isAroundAdviceMethodName(methodName) ||
			abc.weaving.weaver.AfterBeforeInliner.isAdviceMethodName(methodName);
	}
	static boolean considerStaticMethod(String methodName) {
		return 
		Util.isProceedMethodName(methodName) ||
		methodName.startsWith("proceed$") ||
		methodName.startsWith("if$") ||
		methodName.startsWith("shadow$") ||
		methodName.startsWith("inline$");		
	}
	static boolean considerMethod(String methodName) {
		return considerInstanceMethod(methodName) ||
		considerStaticMethod(methodName);
	}
	static private void debug(String message) {
		if (abc.main.Debug.v().interprocConstantPropagator)
			System.err.println("ICP*** " + message);
	}

	private static class ConstantMethodCallArguments {
		private final String methodSig;
		public ConstantMethodCallArguments(Stmt stmt) {
			InvokeExpr expr=stmt.getInvokeExpr();
			invocations.add(stmt);
			
			methodSig=expr.getMethodRef().toString();
			arguments=new Constant[expr.getArgCount()];
			
			int i=0;
			for (Iterator it=expr.getArgs().iterator();it.hasNext();i++) {
				Value arg=(Value)it.next();
				if (arg instanceof Constant) {
					arguments[i]=(Constant)arg;
				}				
			}		
		}
		public void addCall(Stmt stmt) {
			InvokeExpr expr=stmt.getInvokeExpr();
			
			if (!expr.getMethodRef().toString().equals(methodSig))
				throw new InternalCompilerError("");
				
			invocations.add(stmt);
			
			int i=0;
			for (Iterator it=expr.getArgs().iterator();it.hasNext();i++) {
				Value arg=(Value)it.next();
				if (arg instanceof Constant) {
					Constant c=(Constant)arg;
					if (!c.equals(arguments[i]))
						arguments[i]=null;
				} else {
					arguments[i]=null;
				}
			}
		}
		private boolean[] findUsedArguments(SootMethod method) {
			if (!method.makeRef().toString().equals(methodSig))
				throw new InternalCompilerError("");
			
			boolean[] result=new boolean[method.getParameterCount()];
			
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
		
			Set usedLocals=new HashSet();
			for (Iterator it=body.getUseBoxes().iterator(); it.hasNext(); ) {
				ValueBox b=(ValueBox)it.next();
				usedLocals.add(b.getValue());
			}
			
			int index=0;
			for (Iterator itSt=statements.iterator(); itSt.hasNext();) {
				Stmt s=(Stmt)itSt.next();
				if (s instanceof IdentityStmt) {
					IdentityStmt ids=(IdentityStmt)s;
					if (ids.getRightOp() instanceof ParameterRef) {
						result[index]=usedLocals.contains(ids.getLeftOp());
						index++;
					}
				} else
					break;
			}
			return result;
		}
		public int propagate(SootMethod method) {
			if (!method.makeRef().toString().equals(methodSig))
				throw new InternalCompilerError("");
			
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
			
			boolean[] usedArguments=findUsedArguments(method);
			
			int removed=0;
			for (int i=0; i<arguments.length; i++) {
				if (arguments[i]!=null || !usedArguments[i]) {
					
					removeArgument(method, i-removed, arguments[i]);
					for (Iterator it=this.invocations.iterator(); it.hasNext();) {
						Stmt invokeStmt=(Stmt)it.next();
						InvokeExpr expr=invokeStmt.getInvokeExpr();
						debug("Old: " + expr.getMethodRef().getSubSignature());
						debug("Old: " + expr.getMethodRef());
						debug("Met: " + method.makeRef().getSubSignature());
						InvokeExpr newExpr=removeArgument(expr, i-removed);
						
						newExpr.setMethodRef(method.makeRef()); // TODO: 
						
						invokeStmt.getInvokeExprBox().setValue(newExpr);
						
						/*if (!newExpr.getMethodRef().equals(method.makeRef())) {
							debug("New: " + newExpr.getMethodRef().getSubSignature());
							debug("New: " + newExpr.getMethodRef().getSignature());
							debug("Met: " + method.makeRef().getSubSignature());
							newExpr.getMethodRef().resolve();
							throw new RuntimeException();
						}*/
					}					
					removed++;
				}
			}
			return removed;
		}
		private InvokeExpr removeArgument(InvokeExpr expr, int id) {
			SootMethodRef ref=expr.getMethodRef();
			List newArgs=new ArrayList(expr.getArgs());
			List newTypes=new ArrayList(ref.parameterTypes());
			newArgs.remove(id);
			newTypes.remove(id);
			InvokeExpr newExpr=Util.createNewInvokeExpr(expr, newArgs, newTypes);
			return newExpr;
		}
		// removes the argument at position id.
		// if value is non-null, value is assigned to the former parameter local
		private void removeArgument(SootMethod method, int id, Constant value) {
			Body body=method.getActiveBody();
			Chain statements=body.getUnits().getNonPatchingChain();
			
			//List parameterLocals=AroundWeaver.Util.getParameterLocals(body);
			Local paramLocal=null;
			
			Stmt lastStmt=null;
			int index=0;
			for (Iterator itSt=statements.snapshotIterator(); itSt.hasNext();) {
				Stmt s=(Stmt)itSt.next();
				if (s instanceof IdentityStmt) {
					IdentityStmt ids=(IdentityStmt)s;
					if (ids.getRightOp() instanceof ParameterRef) {
						ParameterRef pr=(ParameterRef)ids.getRightOp();
						if (index==id) {
							paramLocal=(Local)ids.getLeftOp();
							statements.remove(s);
						} else if (index>id) {
							pr.setIndex(index-1);
						}
						if (index!=id)
							lastStmt=s;
						
						index++;
					} else
						lastStmt=s;
					
				} else {					
					break;
				}
			}
			//if (lastStmt==null) 
				//throw new InternalCompilerError("");
			if (paramLocal==null)
				throw new InternalCompilerError("");
			
			debug("removing argument, " + method + ", " + id + ": " + paramLocal.getName());
			if (value!=null) {
				debug(" replacing with value " + value.toString());
				Stmt as=soot.jimple.Jimple.v().newAssignStmt(
						paramLocal, value);
				if (lastStmt==null)
					statements.addFirst(as);
				else
					statements.insertAfter(as ,lastStmt);
			}			
			
			{
				List paramTypes=new ArrayList(method.getParameterTypes());
				paramTypes.remove(id);
				method.setParameterTypes(paramTypes);
			}
			
		}
		public Set invocations=new HashSet();
		public final soot.jimple.Constant[] arguments;
	}
	public static interface CallSiteFilter {
		public boolean considerCallSite(Body body);
	}
	private static class DefaultCallSiteFilter implements CallSiteFilter {
		public boolean considerCallSite(Body body) {
			return true;
		}
	}
	public static void inlineConstantArguments() {
		inlineConstantArguments(new DefaultCallSiteFilter());
	}
	public static void inlineConstantArguments(CallSiteFilter filter) {
		Set propagatedBodies=new HashSet();
		while (inlineConstantArgumentsPass(propagatedBodies, filter)>0)
			;
	}

	private static boolean bodyContainsRelevantMethods(Body body) {
		Chain statements=body.getUnits();
        
        for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
        	Stmt stmt=(Stmt)stmtIt.next();
        	if (stmt.containsInvokeExpr()) {
        		InvokeExpr expr=stmt.getInvokeExpr();
        		String name=expr.getMethodRef().name();
        		if (considerMethod(name))
        			return true;
        	}
        }
        return false;
	}
	// returns number of removed arguments
	public static int inlineConstantArgumentsPass(Set propagatedBodies, CallSiteFilter filter) {
		int removedArgs=0;
//		 Retrieve all bodies
		Map /*String, MethodCallArgs*/ methods=new HashMap();
		
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            //final AbcClass cl = (AbcClass) clIt.next();
            soot.SootClass cl=(soot.SootClass)clIt.next();
			
            for( Iterator methodIt = cl.getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                
                if (method.isAbstract())
                	continue;
                
                if (!method.hasActiveBody()) {
                	debug("method does not have active body!");
                	continue;
                }
                
                
                
                Body body=method.getActiveBody();
                
                if (!bodyContainsRelevantMethods(body))
                	continue;
                
                if (!filter.considerCallSite(body)) {
                	continue;
                }
                
                if (!propagatedBodies.contains(body)) {
                	ConstantPropagatorAndFolder.v().transform(body); // TODO: phase name, options?
                	propagatedBodies.add(body);
                }
                
                Chain statements=body.getUnits();
                
                for (Iterator stmtIt=statements.iterator(); stmtIt.hasNext(); ) {
                	Stmt stmt=(Stmt)stmtIt.next();
                	if (stmt.containsInvokeExpr()) {
                		InvokeExpr expr=stmt.getInvokeExpr();
                		String name=expr.getMethodRef().name();
                		if (considerMethod(name)) {// considerMethod(expr.getMethodRef().name())) {
                			String methodSig=expr.getMethodRef().toString();
                			ConstantMethodCallArguments args=
                				(ConstantMethodCallArguments)methods.get(methodSig);
                			//debug("considering method " + methodSig);
                			if (args==null) {
                				args=new ConstantMethodCallArguments(stmt);
                				//debug("adding method " + methodSig);
                				methods.put(methodSig, args);
                			} else {
                				args.addCall(stmt);                				
                			}                			
                		}
                	}
                }
                
            }
        }
        
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            //final AbcClass cl = (AbcClass) clIt.next();
            SootClass cl=(SootClass)clIt.next();
            List clMethods=new ArrayList(cl.getMethods());
            for( Iterator methodIt = clMethods.iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                
                String name=method.getName();
                if ( (!method.isStatic() && considerInstanceMethod(name)) || 
                	 (method.isStatic() && considerMethod(name) )) {
                	
                	String methodSig=method.makeRef().toString();
                	
                	ConstantMethodCallArguments args=
        				(ConstantMethodCallArguments)methods.get(methodSig);
        			if (args==null) {
        				debug("Found unused method, should have been removed: " + method.toString());
        			} else { 
        				int removed=args.propagate(method);
        				if (removed>0)
        					propagatedBodies.remove(method.getActiveBody());
        				removedArgs+=removed;
                	}                	
                }                
            }
        }	
        return removedArgs;
	}
}
