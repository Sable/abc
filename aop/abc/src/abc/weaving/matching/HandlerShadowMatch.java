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

import soot.*;
import soot.jimple.*;
import soot.tagkit.Host;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.residues.*;

/** The results of matching at a handler shadow
 *  @author Ganesh Sittampalam
 *  @date 05-May-04
 */
public class HandlerShadowMatch extends StmtShadowMatch {
    
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
	return new HandlerShadowMatch(pos.getContainer(),stmt,trap.getException());
    }

    public Host getHost() {
	return stmt;
    }
    
    public SJPInfo makeSJPInfo() {
	return new SJPInfo
	    ("exception-handler",
             "org.aspectj.lang.reflect.CatchClauseSignature",
             "makeCatchClauseSig",
	     SJPInfo.makeHandlerSigData(container,sootexc,stmt),stmt);
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
	ret.add(new JimpleValue(((IdentityStmt) stmt).getLeftOp()));
	return ret;
    }

    public String joinpointName() {
	return "handler";
    }

}
