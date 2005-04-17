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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtBody;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.AccessManager;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;

/**
 * @author Sascha Kuzins
 *
 */
public abstract class AdviceInliner extends BodyTransformer {
	public int getAccessViolationCount(SootMethod container, SootMethod adviceMethod) 
	{
		int violations=0;
		Body body=adviceMethod.getActiveBody();
		Chain statements=body.getUnits();
		for (Iterator it=statements.iterator(); it.hasNext();) {
			Stmt stmt=(Stmt)it.next();
			if (!AccessManager.isAccessLegal(container, stmt))
				violations++;
		}
		return violations;
	}
	private void debug(String message) {
		if (abc.main.Debug.v().adviceInliner)
			System.err.println("AIL*** " + message);
	}
	final public static int MAX_DEPTH=4;
	
	// with  50, all cases pass with forced inlining.
	// 100 works as well
	// with 300, some run out of memory (@512M).
	final public static int MAX_CONTAINER_SIZE=1000; //5000;
	
	public static interface InlineOptions {
		public final static int DONT_INLINE=0;
		public final static int INLINE_STATIC_METHOD=1;
		public final static int INLINE_DIRECTLY=2;
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth);
	}
	
	private static int uniqueID=0;
	public static int getUniqueID() { return uniqueID++; }
	public static void reset() {
		uniqueID=0;
	}
	protected boolean inlineMethods(Body body, Map options, InlineOptions inlineOptions, int depth) {
		StmtBody stmtBody = (StmtBody)body;
		
		
		
		Chain units = stmtBody.getUnits();
		
		if (units.size()>MAX_CONTAINER_SIZE) {
			debug("Method body exceeds maximum size. No inlining. " + body.getMethod());
			return false;
		}
		
        ArrayList unitList = new ArrayList(); unitList.addAll(units);

        boolean bDidInline=false;
        Iterator stmtIt = unitList.iterator();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
        	
        	if (!stmt.containsInvokeExpr())
                continue;
        	
        	InvokeExpr expr=stmt.getInvokeExpr();
        	
        	//debug(" EXPR: " + expr);
        	int inliningMode=inlineOptions.inline(body.getMethod(),stmt, expr, depth);
            if (inliningMode==InlineOptions.INLINE_DIRECTLY) {
            	//debug(" Trying to inline " + expr.getMethodRef());
            	if (InlinerSafetyManager.ensureInlinability(
            			expr.getMethod(), stmt, body.getMethod(), "accessors")) { // "unsafe"
            		
            		Stmt before=null;
            		try { before=(Stmt)units.getPredOf(stmt);} catch(NoSuchElementException e){};
            		Stmt after=null;
            		try { after=(Stmt)units.getSuccOf(stmt);} catch(NoSuchElementException e){};
            		SiteInliner.inlineSite(expr.getMethod(), stmt, body.getMethod(), options);
            		
            		AccessManager.createAccessorMethods(body, before, after);           		
            		            		
            		bDidInline=true;
            		debug("  Succeeded.");
            	} else {
            		debug("  Failed.");
            	}
            } else if (inliningMode==InlineOptions.INLINE_STATIC_METHOD) {
            	SootMethod m=expr.getMethod();
            	List args=new LinkedList(expr.getMethodRef().parameterTypes());
            	SootClass targetClass=
            		expr.getMethodRef().declaringClass();
            	if (!m.isStatic())
            		args.add(0, targetClass.getType());
            	Type retType=expr.getMethodRef().returnType();
            	
            	SootMethod method = new SootMethod("inline$" + 
            			getUniqueID() + "$" +
            			expr.getMethodRef().name(),            			
        				args, retType, 
        				Modifier.PUBLIC | Modifier.STATIC,
						m.getExceptions()
						);
            	
            	Body inlineBody = Jimple.v().newBody(method);
        		method.setActiveBody(inlineBody);
        		
        		targetClass.addMethod(method); 
        		
        		AroundInliner.v().adviceMethodsNotInlined.add(method); /// bad hack!!! put this some place else!
        		
        		Chain statements=inlineBody.getUnits().getNonPatchingChain();
        		LocalGeneratorEx lg=new LocalGeneratorEx(inlineBody);
        		
        		List locals=new LinkedList();
        		//int i=m.isStatic() ? 0 : -1;
        		int i=0;
        		for (Iterator it=args.iterator(); it.hasNext();i++) {
        			Type type=(Type)it.next();
        			Local l=lg.generateLocal(type); 
        			statements.add(Jimple.v().newIdentityStmt(
        				l,
					//	i==-1 ?
					//				(Value)Jimple.v().newThisRef((RefType)type) :
									(Value)Jimple.v().newParameterRef(type, i)	
        				));
					locals.add(l);
        		}
        		InvokeExpr inv;
        		SootMethodRef ref=expr.getMethodRef();
        		if (expr instanceof InstanceInvokeExpr) {
        			Local base = (Local)locals.get(0);
        			locals.remove(0);
        			if (expr instanceof InterfaceInvokeExpr)
        				inv= Jimple.v().newInterfaceInvokeExpr(base, ref, locals);
        			else if (expr instanceof SpecialInvokeExpr) {
        				inv=Jimple.v().newSpecialInvokeExpr(base, ref, locals);
        			} else if (expr instanceof VirtualInvokeExpr)
        				inv= Jimple.v().newVirtualInvokeExpr(base, ref, locals);
        			else
        				throw new InternalCompilerError("");
        		} else {
        			inv= Jimple.v().newStaticInvokeExpr(ref, locals);
        		}
        		Stmt invStmt;
        		if (method.getReturnType().equals(VoidType.v())) {
        			invStmt=Jimple.v().newInvokeStmt(inv);
        			statements.add(invStmt);
        			statements.add(Jimple.v().newReturnVoidStmt());
        		} else {
        			Local retl=lg.generateLocal(method.getReturnType());
        			invStmt=Jimple.v().newAssignStmt(retl, inv);
        			statements.add(invStmt);
        			statements.add(Jimple.v().newReturnStmt(retl));
        		}
        		
        		List newArgs=new LinkedList(expr.getArgs());
        		if (expr instanceof InstanceInvokeExpr)
        			newArgs.add(0, ((InstanceInvokeExpr)expr).getBase());
        		
        		stmt.getInvokeExprBox().setValue(
        				Jimple.v().newStaticInvokeExpr(method.makeRef(),
        					newArgs
        				));
				
        		SiteInliner.inlineSite(inv.getMethod(), invStmt, method, options);
            } else {
            	// debug(" No inlining.");
            }
        }		
        return bDidInline;
	}
	
	public abstract boolean forceInline();
	
	protected class IfMethodInlineOptions implements InlineOptions {
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr, int depth) {
			SootMethod method=expr.getMethod();
			
			if (!expr.getMethodRef().name().startsWith("if$"))
				return DONT_INLINE;
			
			if (!method.isStatic())
				return DONT_INLINE;
			
			//if (!method.getDeclaringClass().equals(container.getDeclaringClass()))
			//	return false;
			
			debug("Trying to inline if method " + method);
			
			if (forceInline()) {
				debug("force inline on.");
				return INLINE_DIRECTLY;
			}

			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>0)
					return DONT_INLINE;					
			}
			
			Body body=method.getActiveBody();
			
			//if (info.proceedInvocations>1)
			int size=body.getUnits().size();
			debug(" Size of if method: " + size);
			int addedLocals=body.getLocalCount()-method.getParameterCount();
			debug(" Number of added locals (approximately): " + addedLocals);			
						
			if (size<6)
				return INLINE_DIRECTLY;
			

			return DONT_INLINE;
		}
	}

}
