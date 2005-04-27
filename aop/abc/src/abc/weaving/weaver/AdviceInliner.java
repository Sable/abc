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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import polyglot.util.InternalCompilerError;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.toolkits.invoke.AccessManager;
import soot.jimple.toolkits.invoke.InlinerSafetyManager;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.toolkits.scalar.UnusedLocalEliminator;
import soot.util.Chain;
import abc.main.options.OptionsParser;
import abc.soot.util.AroundShadowInfoTag;
import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.SwitchFolder;
import abc.weaving.weaver.around.AroundWeaver;
import abc.weaving.weaver.around.Util;
import abc.weaving.weaver.around.soot.SiteInliner;

/**
 * @author Sascha Kuzins
 *
 */
public class AdviceInliner { //extends BodyTransformer {
	private static AdviceInliner instance = 
		new AdviceInliner();
	public static void reset() { instance = new AdviceInliner(); 	uniqueID=0; }
	public static AdviceInliner v() { return instance; }
	
	
	private Set shadowMethods=new HashSet();
	private Set additionalShadowMethods=new HashSet();
	public void addShadowMethod(SootMethod m) {
		shadowMethods.add(m);
	}
	
	InlineOptions getInlineOptions() {
		CombinedInlineOptions opts=new CombinedInlineOptions();
		if (OptionsParser.v().around_inlining()) {
			opts.inlineOptions.add(new AroundAdviceMethodInlineOptions());
			opts.inlineOptions.add(new ExtractedShadowMethodInlineOptions());
			opts.inlineOptions.add(new ProceedMethodInlineOptions());
		}
		if (OptionsParser.v().before_after_inlining()) {
			opts.inlineOptions.add(new AfterBeforeMethodInlineOptions());
		}
		if (OptionsParser.v().around_inlining() || OptionsParser.v().before_after_inlining()) {
			opts.inlineOptions.add(new IfMethodInlineOptions());
		}
		return opts;
	}
	
	public void run() {		
		debug("Starting around inliner.");
		Set visitedBodies=new HashSet();
    	for( Iterator mIt = shadowMethods.iterator(); mIt.hasNext(); ) {
    	    final SootMethod m = (SootMethod) mIt.next();
    		inlineMethods(m.getActiveBody(), null, getInlineOptions(), visitedBodies, 0);
    	}
    	do {
	    	if (additionalShadowMethods.size()>0) {
	    		debug("Running ICP.");
	    		InterprocConstantPropagator.inlineConstantArguments();
	    	}
	    	List copy=new ArrayList(additionalShadowMethods);
	    	additionalShadowMethods.clear();
	    	for( Iterator mIt = copy.iterator(); mIt.hasNext(); ) {
	    	    final SootMethod m = (SootMethod) mIt.next();
	    	    Body body=m.getActiveBody();
	    	   // debug(" SSSSSSSSSSSSSSSSSSSSSSSS\n" + Util.printMethod(m));
	    	    foldSwitches(body, true);
	    	    //eliminateUnreachableCode(body);
	    	    //debug(" TTTTTTTTTTTTTTTTTTTTTTTT\n" + Util.printMethod(m));
	    	    inlineMethods(body, null, getInlineOptions(), visitedBodies, 0);
	    		
	    		//eliminateUnreachableCode(body);
	    		
	    	}    	
    	} while (additionalShadowMethods.size()>0);
    	
    	debug("Around inliner done.");
	}
	public void clear() {
		shadowMethods.clear();
		additionalShadowMethods.clear();
	}
	public void runBoxingRemover() {
		for( Iterator mIt = shadowMethods.iterator(); mIt.hasNext(); ) {
    	    final SootMethod m = (SootMethod) mIt.next();
    	//    System.out.println("Boxing: " + m);
    	    //System.out.println(Util.printMethod(m));
    	//    System.out.println(m);
    	//    System.out.println("Old size: " + m.getActiveBody().getUnits().size());
    		BoxingRemover.v().transform(m.getActiveBody());
    	//	System.out.println("New size after boxing remover: " + m.getActiveBody().getUnits().size());
    	//	System.out.println("Allocated heap size:" + 
      //      		NumberFormat.getNumberInstance().format(Runtime.getRuntime().totalMemory()));
    	}
    	for( Iterator mIt = additionalShadowMethods.iterator(); mIt.hasNext(); ) {
    	    final SootMethod m = (SootMethod) mIt.next();
    	    //debug(" method: " + m.getName());
    	//    System.out.println("Boxing: " + m);
    	//    System.out.println(Util.printMethod(m));
    	    //System.out.println(m);
    	//    System.out.println("Old size: " + m.getActiveBody().getUnits().size());
    	    BoxingRemover.v().transform(m.getActiveBody());
    	//    System.out.println("New size after boxing remover: " + m.getActiveBody().getUnits().size());
    	//    System.out.println("Allocated heap size:" + 
       //     		NumberFormat.getNumberInstance().format(Runtime.getRuntime().totalMemory()));
    	    //debug(" " + Util.printMethod(m));
    	}
	}
	public static int getAccessViolationCount(SootMethod container, SootMethod adviceMethod) 
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
	private static void debug(String message) {
		if (abc.main.Debug.v().adviceInliner)
			System.err.println("AIL*** " + message);
	}
	private void debug(String s, int depth) {
		if (abc.main.Debug.v().adviceInliner) {
			System.err.print("AIL*** ");
			while(--depth>0)
				System.err.print("  ");
			
			System.err.println(s);
		}
	}
	//final public static int MAX_DEPTH=4;
	
	// with  50, all cases pass with forced inlining.
	// 100 works as well
	// with 300, some run out of memory (@512M).
	final public static int MAX_CONTAINER_SIZE=20000; //5000;
	
	public static interface InlineOptions {
		public final static int DONT_INLINE=0;
		public final static int INLINE_STATIC_METHOD=1;
		public final static int INLINE_DIRECTLY=2;
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr);
		public boolean considerForInlining(String methodName);
	}
	
	public static class CombinedInlineOptions implements InlineOptions {
		public List inlineOptions=new ArrayList();
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			String name=expr.getMethodRef().name();
			
			for (Iterator it=inlineOptions.iterator(); it.hasNext();) {
				InlineOptions opts=(InlineOptions)it.next();
				if (opts.considerForInlining(name)) {
					return opts.inline(container, stmt, expr);
				}
			}
			return DONT_INLINE;
		}
		public boolean considerForInlining(String methodName) {
			for (Iterator it=inlineOptions.iterator(); it.hasNext();) {
				InlineOptions opts=(InlineOptions)it.next();
				if (opts.considerForInlining(methodName)) {
					return true;
				}
			}
			return false;
		}
	}
	private static int uniqueID=0;
	public static int getUniqueID() { return uniqueID++; }
	
	public static class InlineRange {
		NopStmt begin;
		NopStmt end;
	}
	//Set bodiesExceedingMaximumSize=new HashSet();
	
	protected void inlineMethods(Body body, InlineRange range, InlineOptions inlineOptions, Set visitedBodies, int depth) {
		depth++;
		
		if (range==null && visitedBodies.contains(body))
			return;
		
		visitedBodies.add(body);
		
		debug("Visiting method " + body.getMethod().getName(), depth);
		
		if (!bodyHasRelevantCalls(inlineOptions, body, range)) {
			debug("no relevant calls.", depth);
			return;
		}
		
//		 remove dead code from the dynamic residues.
		// this is important because the dead code may contain a call
		// to the proceed method.
		SwitchFolder.cheapConstantPropagator(body, true);
		eliminateUnreachableCode(body);
		
		
		
//		StmtBody stmtBody = (StmtBody)body;
		
		
		
		Chain units = body.getUnits();
		
		if (range!=null) {
			debug("running on range.", depth);
			if (abc.main.Debug.v().adviceInliner) {
				if (!units.contains(range.begin))
					throw new InternalCompilerError("");
				if (!units.contains(range.end))
					throw new InternalCompilerError("");
			}
		}
		
		
        ArrayList unitList = new ArrayList(); unitList.addAll(units);

        boolean bDidInline=false;
        
        List rangesToInline=new LinkedList();
        
        Iterator stmtIt;
        if (range==null)
        	stmtIt=unitList.iterator();
        else
        	stmtIt=unitList.listIterator(unitList.indexOf(range.begin));
        
        //boolean bDidInlineSwitch=false;
        //Set visitedStatements=new HashSet();
        while (stmtIt.hasNext()) {
        	Stmt stmt = (Stmt)stmtIt.next();
        	
        	//visitedStatements.add(stmt);
        	
        	if (range!=null && stmt==range.end)
        		break;
        	
        	if (!body.getUnits().contains(stmt)) {
    			debug("XXXXXXXXXXXXXXXXXX Statement has been eliminated", depth);
        		continue;
        		//throw new InternalCompilerError("");
        	}
        	
        	
        	if (!stmt.containsInvokeExpr())
                continue;
        	
        	InvokeExpr expr=stmt.getInvokeExpr();
        	
        	boolean runOnRange=false;
        	
        	//debug(" EXPR: " + expr);
        	int inliningMode=inlineOptions.inline(body.getMethod(),stmt, expr);
        	
        	// only want to recurse on ranges once
        	if (range==null && Util.isAroundAdviceMethodName(expr.getMethod().getName()) &&
        			inliningMode==InlineOptions.INLINE_DIRECTLY) 
        		runOnRange=true;
        	
        	InlineRange r=null;
        	if (runOnRange) {
        		r=new InlineRange();
        		r.begin=Jimple.v().newNopStmt();
        		r.end=Jimple.v().newNopStmt();
        		units.insertBefore(r.begin, stmt);
        		units.insertAfter(r.end, stmt);        		
        	}
        	
        	// This is the big step:
        	// Before inlining, recursively visit methods that could be inlined
        	if (inlineOptions.considerForInlining(expr.getMethod().getName())) {
        		inlineMethods(expr.getMethod().getActiveBody(), null, inlineOptions, visitedBodies, depth);
        	}        	
        	
        	if (!body.getUnits().contains(stmt))
    			throw new InternalCompilerError("");
        	
            if (inliningMode==InlineOptions.INLINE_DIRECTLY) {
            	/*if (units.size()>MAX_CONTAINER_SIZE) {
            		if (!bodiesExceedingMaximumSize.contains(body)) {
	            		debug("Method body exceeds maximum size. Trying to compress. " + body.getMethod() + ":" + units.size(), depth);
	            		
	            		//BoxingRemover.runJopPack(body);
	            		foldSwitches(body, true);
	            		debug("New size: " + units.size());
	            		if (units.size()>MAX_CONTAINER_SIZE) {
	            			bodiesExceedingMaximumSize.add(body);
	            			//debug("" + Util.printMethod(body.getMethod()));
	            			//UnreachableCodeEliminator.v().transform(body);
	            			//debug("yyyyyyyyyyyyyyyyyyyyyyyyyyy\n" + Util.printMethod(body.getMethod()));
	            			//System.exit(0);
	            		}
	            		if (!body.getUnits().contains(stmt)) {
	            			debug("XXXXXXXXXXXXXXXXXX Statement has been eliminated(2)", depth);
	                		continue;
	                		//throw new InternalCompilerError("");
	                	}
            		}
            	}*/
            	//if (units.size()>MAX_CONTAINER_SIZE) {
        		//	debug("Method body exceeds maximum size. No inlining. " + body.getMethod(), depth);
            	//} else {           	//debug(" Trying to inline " + expr.getMethodRef());
            	{
            		if (InlinerSafetyManager.ensureInlinability(
	            			expr.getMethod(), stmt, body.getMethod(), "accessors")) { // "unsafe"
	            		
	            		Stmt before=null;
	            		try { before=(Stmt)units.getPredOf(stmt);} catch(NoSuchElementException e){};
	            		Stmt after=null;
	            		try { after=(Stmt)units.getSuccOf(stmt);} catch(NoSuchElementException e){};
	            		
	            		//debug(" method: " + Util.printMethod(expr.getMethod()));
	            		//debug(" stmt: " + stmt);
	            		if (!body.getUnits().contains(stmt))
	            			throw new InternalCompilerError("");
	            		
	            		
	            		inlineSite(expr.getMethod(), stmt, body.getMethod());
	            		
	            		
	            		
	            		AccessManager.createAccessorMethods(body, before, after);           		
	            		
	            		
	            		//bDidInlineSwitch=bDidInlineSwitch || 
						//	rangeContainsSwitch(units, before, after);
	            								
	            		/*if (units.size()>MAX_CONTAINER_SIZE/2
	            				&& Util.isAroundAdviceMethodName(expr.getMethod().getName())) {
	            			foldSwitches(body);
	            		}*/
	            		
	            		bDidInline=true;
	            		debug("Succeeded.", depth);
	            		rangesToInline.add(r);
	            		debug("QQQ adding range", depth);
	            	} else {
	            		debug("Failed.", depth);
	            	}
            	}
            } else if (inliningMode==InlineOptions.INLINE_STATIC_METHOD) {
            	SootMethod m=expr.getMethod();
            	List inlineMethodArgTypes=new LinkedList();//(expr.getMethodRef().parameterTypes());
            	for (Iterator it=expr.getArgs().iterator(); it.hasNext(); ) {
            		Value v=(Value)it.next();
            		Type t=v.getType();
            		inlineMethodArgTypes.add(t);
            	}
            	SootClass targetClass=
            		expr.getMethodRef().declaringClass();
            	if (!m.isStatic())
            		inlineMethodArgTypes.add(0, targetClass.getType());
            	Type retType=expr.getMethodRef().returnType();
            	/*Type retType;
            	if (stmt instanceof AssignStmt) {
            		AssignStmt as=(AssignStmt)stmt;
            		retType=as.getLeftOp().getType();
            	} else {
            		retType=m.getReturnType();
            	}*/
            	
            	SootMethod method = new SootMethod("inline$" + 
            			getUniqueID() + "$" +
            			expr.getMethodRef().name(),            			
        				inlineMethodArgTypes, retType, 
        				Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL,
						m.getExceptions()
						);
            	
            	Body inlineBody = Jimple.v().newBody(method);
        		method.setActiveBody(inlineBody);
        		
        		targetClass.addMethod(method); 
        		
        		additionalShadowMethods.add(method); 
        		
        		Chain statements=inlineBody.getUnits().getNonPatchingChain();
        		LocalGeneratorEx lg=new LocalGeneratorEx(inlineBody);
        		
        		List locals=new LinkedList();
        		//int i=m.isStatic() ? 0 : -1;
        		int i=0;
        		for (Iterator it=inlineMethodArgTypes.iterator(); it.hasNext();i++) {
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
				
        		inlineSite(inv.getMethod(), invStmt, method);
        		bDidInline=true;
        		
        		debug("Succeeded(2).", depth);
        		//BoxingRemover.runJopPack(method.getActiveBody());
        		//BoxingRemover.removeUnnecessaryCasts(method.getActiveBody());
            } else {
            	//debug(" No inlining.");
            }            
        }
        if (bDidInline) {
        	debug("QQQ WWWWWWWWWWWWWWWWWWWWWWWWWW(0)", depth);
        	//if (bDidInlineSwitch)
        	foldSwitches(body, false);
        	
        	for (Iterator it=rangesToInline.iterator(); it.hasNext();) {
        		InlineRange r=(InlineRange)it.next();
   	
        		debug("QQQ WWWWWWWWWWWWWWWWWWWWWWWWWW", depth);
        		inlineMethods(body, r, new ProceedMethodInlineOptions(), visitedBodies, depth);
        	}
        	//if (rangesToInline.size()>0)
        		//foldSwitches(body);
        }
        if (range==null && units.size()>1000) {
        	// Perform some more serious opts to reduce size, to prevent the nullcheck analysis
        	// from blowing up later.
        	debug("Inlinee is big, trying to reduce size: " + units.size(), depth);
        	soot.jimple.toolkits.scalar.CopyPropagator.v().transform(body);
            ConstantPropagatorAndFolder.v().transform(body);
            DeadAssignmentEliminator.v().transform(body);
            UnusedLocalEliminator.v().transform(body);
        }        
        //BoxingRemover.runJopPack(body);
		//BoxingRemover.removeUnnecessaryCasts(body);
	}
	
	public static void inlineSite(SootMethod inlinee, Stmt invStmt, SootMethod container) {
		HashMap dummyOptions = new HashMap();
        dummyOptions.put( "enabled", "true" );
		SiteInliner.inlineSite(inlinee, invStmt, container, dummyOptions);
	}
	
	public boolean aroundForceInline() {
		return OptionsParser.v().around_force_inlining();
	}
	public boolean afterBeforeForceInline() {
		return OptionsParser.v().before_after_force_inlining();
	}
	
	
	public static boolean isAfterBeforeAdviceMethod(String name) {
		return name.startsWith("before$") || name.startsWith("after$") ||
		name.startsWith("afterReturning$") ||
		name.startsWith("afterThrowing$");
	}
	private class AfterBeforeMethodInlineOptions implements InlineOptions {
		public boolean considerForInlining(String name) {
			return isAfterBeforeAdviceMethod(name);
		}
		
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!considerForInlining(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			debug("    Trying to inline advice method " + method);
			
			if (afterBeforeForceInline()) {
				debug("    force inline on.");
				return InlineOptions.INLINE_DIRECTLY;	
			} 
		
			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>1)
					return InlineOptions.DONT_INLINE;					
			}
			Body body=method.getActiveBody();
			
			//if (info.proceedInvocations>1)
			int size=body.getUnits().size();
			debug("     Size of advice method: " + size);
			int addedLocals=body.getLocalCount()-method.getParameterCount();
			debug("     Number of added locals (approximately): " + addedLocals);			
						
			if (size<6)
				return InlineOptions.INLINE_STATIC_METHOD;
			
			
			return InlineOptions.DONT_INLINE;
		}
	}

	private static boolean bodyHasRelevantCalls(InlineOptions inlineOptions, Body body, InlineRange range) {
		Chain statements=body.getUnits();
		
		Iterator stmtIt;
		if (range==null)
			stmtIt=statements.iterator();
		else
			stmtIt=statements.iterator(range.begin);
		
        while (stmtIt.hasNext()) {
        	Stmt stmt=(Stmt)stmtIt.next();
        	if (range!=null && stmt==range.end)
        		break;
        	
        	if (stmt.containsInvokeExpr()) {
        		InvokeExpr expr=stmt.getInvokeExpr();
        		String name=expr.getMethodRef().name();
        		if (inlineOptions.considerForInlining(name))
        			return true;
        	}
        }
        return false;
	}
	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	private void foldSwitches(Body body, boolean evaluate) {
		if (!methodContainsSwitch(body))
			return;
		//ConstantPropagatorAndFolder.v().transform(body);
    	//SwitchFolder.cheapConstantPropagator(body, true);
		//InterprocConstantPropagator.removeUnusedLocals(body.getMethod());
		SwitchFolder.v().foldWithCheapPropagation(body, evaluate);// .transform(body); // TODO: phaseName etc.?
		//eliminateUnreachableCode(body);
	}
	
	/**
	 * @param body
	 */
	private void eliminateUnreachableCode(Body body) {
		//SwitchFolder.simpleUnusedCodeRemover(body);
		
		UnreachableCodeEliminator.v().transform(body);
	}
	
	
	private static boolean methodContainsSwitch(Body body) {
		Chain statements=body.getUnits();
		for (Iterator it=statements.iterator();it.hasNext();) {
			Stmt s=(Stmt)it.next();
			if (s instanceof TableSwitchStmt || s instanceof LookupSwitchStmt)
				return true;
		}
		return false;
	}
	private static boolean rangeContainsSwitch(Chain units, Stmt before, Stmt after) {
		Iterator it;
		if (before==null)
			it=units.iterator();
		else 
			it=units.iterator(before);
		
		while(it.hasNext()) {
			Stmt s=(Stmt)it.next();
			if (s==after)
				return false;
			if (s instanceof TableSwitchStmt || s instanceof LookupSwitchStmt)
				return true;
		}
		return false;
	}
	protected class IfMethodInlineOptions implements InlineOptions {
		public boolean considerForInlining(String name) {
			return name.startsWith("if$");
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			if (!considerForInlining(expr.getMethodRef().name()))
				return DONT_INLINE;
			
			if (!method.isStatic())
				return DONT_INLINE;
			
			//if (!method.getDeclaringClass().equals(container.getDeclaringClass()))
			//	return false;
			
			debug("Trying to inline if method " + method);
			
			if (aroundForceInline() || afterBeforeForceInline()) { /// hack
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
	private class AroundAdviceMethodInlineOptions implements InlineOptions {
		public boolean considerForInlining(String name) {
			return Util.isAroundAdviceMethodName(name);
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			if (!Util.isAroundAdviceMethodName(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			int bDidInline=internalInline(container, stmt, expr);
			if (bDidInline!=InlineOptions.INLINE_DIRECTLY) {
				//adviceMethodsNotInlined.add(method);
			}
			return bDidInline;
		}
		private int internalInline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			//debug("   Trying to inline advice method " + method);
			
			if (aroundForceInline()) {
			//	debug("    force inline on.");
				return InlineOptions.INLINE_DIRECTLY;	
			} else if (true) {				
				/*if (container.getName().startsWith("inline$")) {
					//if (true)throw new InternalCompilerError("");
					return DONT_INLINE;
				} else if (Util.isAroundAdviceMethodName(container.getName())) {
						return DONT_INLINE;
				} else {
					//if (true)throw new InternalCompilerError("");
				*/	debug("    container: " + container.getName());
					return InlineOptions.INLINE_STATIC_METHOD;
				//}
			}
			// unreachable code below.
			
			AroundWeaver.AdviceMethodInlineInfo info=
					AroundWeaver.v().getAdviceMethodInlineInfo(method);
			
			AroundWeaver.ShadowInlineInfo shadowInfo=null;
			debug("Proceed method: " + method);
			
			if (stmt.hasTag("AroundShadowInfoTag"))	{
				AroundShadowInfoTag tag=
					(AroundShadowInfoTag)stmt.getTag("AroundShadowInfoTag");
			
				debug(" Found tag.");
				shadowInfo=tag.shadowInfo;
			}
			if (shadowInfo!=null) {
				if (shadowInfo.weavingRequiredUnBoxing) {
					debug(" (Un-)Boxing detected. Inlining.");
					return InlineOptions.INLINE_STATIC_METHOD;
				}
			}
			
			int accessViolations=getAccessViolationCount(container, method);
			if (accessViolations!=0) {
				debug("Access violations");
				debug(" Method: " + container);
				debug(" Advice method: " + method); 
				debug(" Violations: " + accessViolations);
				if (accessViolations>1)
					return InlineOptions.DONT_INLINE;					
			}
			
			if (info.nestedClasses) {
				debug(" Skipped (nested classes)");
				return InlineOptions.DONT_INLINE;
			}
			
			//if (info.proceedInvocations>1)
			debug(" Size of advice method: " + info.originalSize);
			debug(" Number of applications: " + info.applications);
			debug(" Number of added locals (approximately): " + info.internalLocalCount);
			debug(" Proceed invocations: " + info.proceedInvocations);
			
			
			//if (info.originalSize< (20 >> (depth-1)))
			//	return InlineOptions.INLINE_STATIC_METHOD;
			
			//if (info.internalLocalCount==0)
			//	return true;
			//if (info.applications==1)
			//	return true;
			
			return InlineOptions.DONT_INLINE;
		}
	}
	private class ProceedMethodInlineOptions implements InlineOptions {
		public ProceedMethodInlineOptions() {
			
		}
		public boolean considerForInlining(String methodName) {
			return Util.isProceedMethodName(methodName);
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			//debug("PROCEED: " + method);
			if (!considerForInlining(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			if (!method.isStatic())
				return InlineOptions.DONT_INLINE;
			
			if (!method.getDeclaringClass().equals(container.getDeclaringClass())) {
				if (OptionsParser.v().around_force_inlining())
					return InlineOptions.DONT_INLINE;
				else { 
					if (container.getName().startsWith("inline$")) /// is there a better way to express this?
						return InlineOptions.INLINE_DIRECTLY;
					else
						return DONT_INLINE;
				}
			}
			
			debug("Trying to inline proceed method " + method);
			
//			 we now *always* inline proceed 
			// because the shadow is always tiny due to the extraction.
		
			if (true)
				return InlineOptions.INLINE_DIRECTLY;
			// unreachable code below
			
			if (aroundForceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;
			}
	
						
			AroundWeaver.ProceedMethodInlineInfo info=					
				AroundWeaver.v().getProceedMethodInlineInfo(method);
			
			AroundWeaver.ShadowInlineInfo shadowInfo=null;
			debug("Proceed method: " + method);
			
			if (stmt.hasTag("AroundShadowInfoTag"))	{
				AroundShadowInfoTag tag=
					(AroundShadowInfoTag)stmt.getTag("AroundShadowInfoTag");
			
				debug(" Found tag.");
				shadowInfo=tag.shadowInfo;
			} else {
				soot.Value v=expr.getArg(info.shadowIDParamIndex);
				if (Evaluator.isValueConstantValued(v)) {
                    v = Evaluator.getConstantValueOf(v);
                    int shadowID=((IntConstant) v).value;
                  
                    shadowInfo=
                    	(AroundWeaver.ShadowInlineInfo) info.shadowInformation.get(new Integer(shadowID));                 	
                    
                    stmt.addTag(new AroundShadowInfoTag(
                    		shadowInfo));
				}
			}
			if (shadowInfo!=null) {
				debug(" Shadow size: " + shadowInfo.size);
				debug(" Number of additional locals (approximately): " + shadowInfo.internalLocals);
			} else {
				debug(" Could not find shadow information.");				
			}
			if (shadowInfo!=null) {
				if (shadowInfo.weavingRequiredUnBoxing) {
					debug(" (Un-)Boxing detected. Inlining.");
					return INLINE_DIRECTLY;
				}
				
				if (shadowInfo.size<10)
					return INLINE_DIRECTLY;
				
				//if (shadowInfo.internalLocals==0)
				//	return true;
			}
				
			
			

			return DONT_INLINE;
		}
	}
	

	private class ExtractedShadowMethodInlineOptions implements InlineOptions {
		public boolean considerForInlining(String name) {
			return name.startsWith("shadow$");
		}
		public ExtractedShadowMethodInlineOptions() {
			
		}
		public int inline(SootMethod container, Stmt stmt, InvokeExpr expr) {
			SootMethod method=expr.getMethod();
			
			//debug("PROCEED: " + method);
			if (!considerForInlining(expr.getMethodRef().name()))
				return InlineOptions.DONT_INLINE;
			
			if (!method.isStatic())
				throw new InternalCompilerError("");
			
			
			if (!method.getDeclaringClass().equals(container.getDeclaringClass())) {
				int accessViolations=getAccessViolationCount(container, method);
				if (accessViolations>0)
					return DONT_INLINE;
			}  
				
				
			
			debug("Trying to inline shadow method " + method);
			
//			 we now *always* inline proceed 
			// because the shadow is always tiny due to the extraction.
		
			if (aroundForceInline()) {
				debug("force inline on.");
				return InlineOptions.INLINE_DIRECTLY;
			}
	
			int size=method.getActiveBody().getUnits().size()
				- method.getParameterCount();
			debug("  size: " + size);
			if (size<3)
				return INLINE_DIRECTLY;
			
			return DONT_INLINE;
		}
	}
}
