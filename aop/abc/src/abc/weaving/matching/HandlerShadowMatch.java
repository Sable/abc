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

import java.util.ArrayList;
import java.util.List;

import polyglot.util.InternalCompilerError;
import soot.Immediate;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JNopStmt;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.ContextValue;
import abc.weaving.residues.JimpleValue;
import abc.weaving.residues.Residue;
import abc.weaving.weaver.ConstructorInliningMap;

/** The results of matching at a handler shadow
 *  @author Ganesh Sittampalam
 *  @author Ondrej Lhotak
 *  @date 05-May-04
 */
public class HandlerShadowMatch extends StmtShadowMatch {
    
    public ShadowMatch inline(ConstructorInliningMap cim) {
        ShadowMatch ret = cim.map(this);
        if(ret != null) return ret;
        if( cim.inlinee() != container ) throw new InternalCompilerError(
                "inlinee "+cim.inlinee()+" doesn't match container "+container);
        ret = new HandlerShadowMatch(cim.target(), cim.map(stmt), sootexc);
        cim.add(this, ret);
        if(sp != null) ret.sp = sp.inline(cim);
        return ret;
    }

    private SootClass sootexc;

    
    private HandlerShadowMatch(SootMethod container,Stmt stmt,SootClass sootexc) {
	super(container,stmt);
	this.sootexc=sootexc;
    }

    public SootClass getException() {
	return sootexc;
    }

    public static HandlerShadowMatch matchesAt(MethodPosition pos) {
	if(!(pos instanceof TrapMethodPosition)) return null;
	if(abc.main.Debug.v().traceMatcher) System.err.println("Handler");

	Trap trap=((TrapMethodPosition) pos).getTrap();
	Stmt stmt=(Stmt) trap.getHandlerUnit();
	//due to method restructuring, the identity statement "ex = @caughtexception"
	//might be preceded by nop-statements; hence, consume those...
	PatchingChain<Unit> units = pos.getContainer().getActiveBody().getUnits();
	while(stmt instanceof JNopStmt) {
		stmt = (Stmt) units.getSuccOf(stmt);
	}	
	assert stmt instanceof IdentityStmt;
	return new HandlerShadowMatch(pos.getContainer(),stmt,trap.getException());
    }

    
    public SJPInfo makeSJPInfo() {
	return abc.main.Main.v().getAbcExtension().createSJPInfo
	    ("exception-handler",
             "org.aspectj.lang.reflect.CatchClauseSignature",
             "makeCatchClauseSig",
	     AbcSJPInfo.makeHandlerSigData(container,sootexc,stmt),stmt);
    }

    public AdviceApplication  doAddAdviceApplication
	(MethodAdviceList mal,AbstractAdviceDecl ad,Residue residue) {

	HandlerAdviceApplication aa=new HandlerAdviceApplication(ad,residue,stmt);
        mal.addStmtAdvice(aa);
	return aa;
    }

    public boolean supportsAfter() {
	return false;
    }

    public ContextValue getTargetContextValue() {
	return null;
    }

    public List/*<ContextValue>*/ getArgsContextValues() {
	ArrayList ret=new ArrayList(1);
	ret.add(new JimpleValue((Immediate)((IdentityStmt) stmt).getLeftOp()));
	return ret;
    }

    public String joinpointName() {
	return "handler";
    }

}
