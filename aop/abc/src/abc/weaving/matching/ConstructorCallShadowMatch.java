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

import polyglot.util.InternalCompilerError;

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.Residue;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.weaver.*;

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 */
public class ConstructorCallShadowMatch extends StmtShadowMatch {
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new ConstructorCallShadowMatch(cim.target(), cim.map(stmt),
                cim.map(next), invoke, isSuperCall );
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    private Stmt next;
    private SpecialInvokeExpr invoke;
    private boolean isSuperCall;
    private static Set newcalls = new HashSet();
        
    
    public static void reset() {
    	newcalls.clear();
    }
    
    private ConstructorCallShadowMatch(SootMethod container,Stmt stmt,
    		                           Stmt next,SpecialInvokeExpr invoke,boolean issuper) {
	super(container,stmt);
	this.next=next;
        this.invoke=invoke;
        this.isSuperCall = issuper;
    }

    public SootMethodRef getMethodRef() {
	return invoke.getMethodRef();
    }

    public List/*<SootClass>*/ getExceptions() {
	return invoke.getMethodRef().resolve().getExceptions();
    }
    
    public boolean isSpecial() {
    	return isSuperCall;
    }
    
    public static ConstructorCallShadowMatch matchesAt2(MethodPosition pos) {
    	if(abc.main.Debug.v().ajcCompliance) return null;
    	if(!(pos instanceof StmtMethodPosition)) return null;
    	Stmt current = ((StmtMethodPosition) pos).getStmt();
    	if(newcalls.contains(current)) return null;
    	if(!(current instanceof InvokeStmt)) return null;
    	InvokeExpr iexpr=((InvokeStmt) current).getInvokeExpr();
    	if(!(iexpr instanceof SpecialInvokeExpr)) return null;
    	SpecialInvokeExpr siexpr=(SpecialInvokeExpr) (((InvokeStmt) current).getInvokeExpr());
    	if(!(siexpr.getMethodRef().name().equals("<init>"))) return null;
    	StmtShadowMatch.makeArgumentsUniqueLocals(pos.getContainer(), current);
    	return new ConstructorCallShadowMatch(pos.getContainer(),current,null,siexpr,true);
    }

    public static ConstructorCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof NewStmtMethodPosition)) return matchesAt2(pos);
	if(abc.main.Debug.v().traceMatcher) System.err.println("ConstructorCall");

	NewStmtMethodPosition stmtMP=(NewStmtMethodPosition) pos;
	Stmt current=stmtMP.getStmt();
	Stmt next=stmtMP.getNextStmt();

	if(!(current instanceof AssignStmt)) return null;
	AssignStmt as = (AssignStmt) current;
	Value rhs = as.getRightOp();
	if(!(rhs instanceof NewExpr)) return null;

	if(!(next instanceof InvokeStmt)) { 
	    // FIXME : improve this behaviour?
	    throw new InternalCompilerError
		("Didn't find an InvokeStmt after a new: "
		 +pos.getContainer()+" "+current+" "+next);
	}
	InvokeExpr iexpr=((InvokeStmt) next).getInvokeExpr();
	if(!(iexpr instanceof SpecialInvokeExpr)) 
	    throw new InternalCompilerError
		("Invoke statement "+next+" after a new statement "+current+" in method "
		 +pos.getContainer()+" wasn't a special invoke");
	SpecialInvokeExpr siexpr=(SpecialInvokeExpr) (((InvokeStmt) next).getInvokeExpr());
	
	newcalls.add(next);
	
	StmtShadowMatch.makeArgumentsUniqueLocals(stmtMP.getContainer(), next);
	
	// We assume the method we just got must be a constructor, because
	// we've already done the moving stuff around thing.
	return new ConstructorCallShadowMatch(pos.getContainer(),current,next,siexpr,false);
    }

    public Host getHost() {
	if(stmt.hasTag("SourceLnPosTag") || stmt.hasTag("LineNumberTag") || isSuperCall) return stmt;
	return next;
    }

    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ( "constructor-call",
              "org.aspectj.lang.reflect.ConstructorSignature",
	      "makeConstructorSig",AbcSJPInfo.makeConstructorSigData(getMethodRef().resolve()),stmt);
    }

    protected AdviceApplication doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	NewStmtAdviceApplication aa=new NewStmtAdviceApplication(ad,residue,stmt);
	mal.addStmtAdvice(aa);
	return aa;
    }

    public ContextValue getTargetContextValue() {
	return null;
    }
    
    public ContextValue getThisContextValue() {
    	if (isSuperCall)
    		return null;
    	else
    		return super.getThisContextValue();
    }
    
    

    public ContextValue getReturningContextValue() {
	return new JimpleValue((Immediate) invoke.getBase());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Immediate) argsIt.next()));
	return ret;
    }

    public String joinpointName() {
	return "constructor call";
    }


}
