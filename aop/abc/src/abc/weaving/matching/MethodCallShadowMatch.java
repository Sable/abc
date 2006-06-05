/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Ondrej Lhotak
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

package abc.weaving.matching;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;
import soot.util.Chain;

import abc.soot.util.LocalGeneratorEx;
import abc.soot.util.Restructure;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.MethodCategory;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.*;
import polyglot.util.InternalCompilerError;

/** The results of matching at a method call shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class MethodCallShadowMatch extends StmtShadowMatch {
    
    private SootMethodRef methodref;
    private InvokeExpr invoke;
    
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new MethodCallShadowMatch(cim.target(), cim.map(stmt), invoke, methodref);
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }


    private MethodCallShadowMatch(SootMethod container,Stmt stmt,
				  InvokeExpr invoke,SootMethodRef methodref) {
	super(container,stmt);
	if(abc.main.Debug.v().java13) methodref=methodref.resolve().makeRef();
	this.methodref=methodref;
	this.invoke=invoke;
    }

    public SootMethodRef getMethodRef() {
	return methodref;
    }

    public List/*<SootClass>*/ getExceptions() {
	return methodref.resolve().getExceptions();
    }

    public static MethodCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof StmtMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("MethodCall");

	Stmt stmt=((StmtMethodPosition) pos).getStmt();

	InvokeExpr invoke;

	if (stmt instanceof InvokeStmt) {
	    InvokeStmt istmt=(InvokeStmt) stmt;
	    invoke=istmt.getInvokeExpr();
	} else if(stmt instanceof AssignStmt) {
	    AssignStmt as = (AssignStmt) stmt;
	    Value rhs = as.getRightOp();
	    if(!(rhs instanceof InvokeExpr)) return null;
	    invoke=(InvokeExpr) rhs;
	} else return null;
	SootMethodRef methodref=invoke.getMethodRef();

	if(methodref.name().equals(SootMethod.constructorName)) return null;
	// The next one really ought not to happen...
	if(methodref.name().equals(SootMethod.staticInitializerName)) return null;

	if(!MethodCategory.weaveCalls(methodref)) return null;

	if(abc.main.Debug.v().ajcCompliance) {
	    // eliminate super calls, following the specification for such
	    // calls described in 'invokespecial' in the JVM spec

	    SootMethod method=methodref.resolve();

	    // We already know it is not a <init>
	    if(invoke instanceof SpecialInvokeExpr && 
	       !method.isPrivate() && 
	       // this check should be redundant
	       !method.isStatic()) {
		SootClass declaringclass=method.getDeclaringClass();
		SootClass currentclass=pos.getContainer().getDeclaringClass();
		// FIXME: temporary until Soot gets fixed
		Scene.v().releaseActiveHierarchy();
		if(Scene.v().getActiveHierarchy()
		   .isClassSubclassOf(currentclass,declaringclass)) {
		    // Assume ACC_SUPER was set since we have no way of checking
		    // and it's only not set by legacy compilers anyway
		    return null;
		}
	    }
	}

	if(abc.main.Debug.v().traceMatcher) System.err.print("Restructuring...");
	// Eagerly restructure non-constructor InvokeStmts to AssignStmts, 
	// because it saves us from having to fix up the AdviceApplications later
	// We may wish to improve this behaviour later.
	if(stmt instanceof InvokeStmt && !(methodref.returnType() instanceof VoidType))
	    stmt=Restructure.getEquivAssignStmt(pos.getContainer(),(InvokeStmt) stmt);

	if(abc.main.Debug.v().traceMatcher) System.err.print("args -> unique locals...");
	StmtShadowMatch.makeArgumentsUniqueLocals(((StmtMethodPosition) pos).getContainer(), stmt);
	if(abc.main.Debug.v().traceMatcher) System.err.println("done");

	return new MethodCallShadowMatch(pos.getContainer(),stmt,invoke,methodref);
    }

    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("method-call",
             "org.aspectj.lang.reflect.MethodSignature",
             "makeMethodSig",
             AbcSJPInfo.makeMethodSigData(methodref),stmt);
    }

    public ContextValue getTargetContextValue() {
	if(invoke instanceof InstanceInvokeExpr) {
	    InstanceInvokeExpr ii=(InstanceInvokeExpr) invoke;
	    return new JimpleValue((Immediate) ii.getBase());
	} else return null;
    }

    public ContextValue getReturningContextValue() {
	if(methodref.returnType() instanceof VoidType)
	    return super.getReturningContextValue();  // null value

	// This shouldn't get triggered as long as we are eagerly restructuring
	// in the matcher above
	if(stmt instanceof InvokeStmt) 
	    stmt=Restructure.getEquivAssignStmt(container,(InvokeStmt) stmt);

	AssignStmt astmt=(AssignStmt) stmt;

	return new JimpleValue((Immediate)astmt.getLeftOp());
    }
    
    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Immediate) argsIt.next()));
	return ret;
    }

    public String joinpointName() {
	return "method call";
    }
}
