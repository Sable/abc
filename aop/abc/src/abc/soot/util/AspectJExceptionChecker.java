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

package abc.soot.util;

import java.util.*;
import polyglot.util.InternalCompilerError;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.base.*;

/** An AspectJ specific exception checker that understands information passed
 *  by the weaver about redirecting or suppressing exception checks
 *  @author Ganesh Sittampalam
 */
public class AspectJExceptionChecker extends ExceptionChecker {
    public AspectJExceptionChecker(ExceptionCheckerErrorReporter r) {
	super(r);
    }
    
    private Body realBody;

    private static class Context {
	public Context(Body body,Stmt stmt) {
	    this.body=body;
	    this.stmt=stmt;
	}

	public Body body;
	public Stmt stmt;
    }

    private Stack/*<Context>*/ contextStack=new Stack();

    protected void internalTransform(Body b, String phaseName, Map options) {
		if (b.getMethod().hasTag(DisableExceptionCheckTag.name))
			return;
		realBody = b;
		doCheck(b, b.getUnits().iterator());
		realBody = null;
		if (!contextStack.empty())
			throw new InternalCompilerError(
					"Uneven use of stack in AspectJExceptionChecker");
	}

    protected void doCheck(Body b, Iterator it) {
		while (it.hasNext()) {
			Stmt s = (Stmt) it.next();
			if (s.hasTag(DisableExceptionCheckTag.name)) {
				
			} else if (s.hasTag(RedirectedExceptionSpecTag.name)) {
				RedirectedExceptionSpecTag redir = (RedirectedExceptionSpecTag) s
						.getTag(RedirectedExceptionSpecTag.name);
				contextStack.push(new Context(b, s));
				doCheck(redir.body, redir.stmts.iterator());
				contextStack.pop();
			} else if (s instanceof ThrowStmt) {
				ThrowStmt ts = (ThrowStmt) s;
				checkThrow(b, ts);
			} else if (s instanceof InvokeStmt) {
				InvokeStmt is = (InvokeStmt) s;
				checkInvoke(b, is);
			} else if ((s instanceof AssignStmt)
					&& (((AssignStmt) s).getRightOp() instanceof InvokeExpr)) {
				InvokeExpr ie = (InvokeExpr) ((AssignStmt) s).getRightOp();
				checkInvokeExpr(b, ie, s);
			}
		}
	}
    
    protected boolean isThrowDeclared(Body b, SootClass throwClass) {
	return super.isThrowDeclared(realBody,throwClass);
    }

    protected boolean isExceptionCaught(Body b, Stmt s, RefType throwType) {
	if(super.isExceptionCaught(b,s,throwType)) return true;
	Iterator contextIt=contextStack.iterator();
	while(contextIt.hasNext()) {
	    Context context=(Context) contextIt.next();
	    if(super.isExceptionCaught(context.body,context.stmt, throwType)) return true;
	}
	return false;
    }

}
