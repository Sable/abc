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

import soot.jimple.*;
import soot.SootMethod;
import soot.tagkit.Host;

/** Specifies matching at a particular new statement
 *  @author Ganesh Sittampalam
 */


public class NewStmtMethodPosition extends MethodPosition {
    private Stmt stmt;
    private Stmt nextStmt;

    public NewStmtMethodPosition(SootMethod container,Stmt stmt,Stmt nextStmt) {
        super(container);
        this.stmt=stmt;
        this.nextStmt=nextStmt;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public Stmt getNextStmt() {
        return nextStmt;
    }

    public Host getHost() {
        return stmt;
    }

}
