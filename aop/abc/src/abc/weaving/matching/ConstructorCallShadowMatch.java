/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
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

/** The results of matching at a new+constructor call shadow
 *  @author Ganesh Sittampalam
 */
public class ConstructorCallShadowMatch extends StmtShadowMatch {
    private Stmt next;
    private SpecialInvokeExpr invoke;
        
    private ConstructorCallShadowMatch(SootMethod container,Stmt stmt,Stmt next,SpecialInvokeExpr invoke) {
	super(container,stmt);
	this.next=next;
	this.invoke=invoke;
    }

    public SootMethodRef getMethodRef() {
	return invoke.getMethodRef();
    }

    public List/*<SootClass>*/ getExceptions() {
	return invoke.getMethodRef().resolve().getExceptions();
    }

    public static ConstructorCallShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof NewStmtMethodPosition)) return null;
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
	
	StmtShadowMatch.makeArgumentsUniqueLocals(stmtMP.getContainer(), next);
	
	// We assume the method we just got must be a constructor, because
	// we've already done the moving stuff around thing.
	return new ConstructorCallShadowMatch(pos.getContainer(),current,next,siexpr);
    }

    public Host getHost() {
	if(stmt.hasTag("SourceLnPosTag") || stmt.hasTag("LineNumberTag")) return stmt;
	return next;
    }

    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ( "constructor-call","ConstructorSignature",
	      "makeConstructorSig",SJPInfo.makeConstructorSigData(getMethodRef().resolve()),stmt);
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

    public ContextValue getReturningContextValue() {
	return new JimpleValue(invoke.getBase());
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	Iterator argsIt=invoke.getArgs().iterator();
	List ret=new LinkedList();
	while(argsIt.hasNext()) 
	    ret.add(new JimpleValue((Value) argsIt.next()));
	return ret;
    }

    public String joinpointName() {
	return "constructor call";
    }


}
