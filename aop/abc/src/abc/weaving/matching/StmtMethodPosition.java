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
import soot.*;
import soot.tagkit.Host;

/** Specifies matching at a particular statement
 *  @author Ganesh Sittampalam
 */

public class StmtMethodPosition extends MethodPosition {
    private Stmt stmt;

    public StmtMethodPosition(SootMethod container,Stmt stmt) {
        super(container);
        this.stmt=stmt;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public Host getHost() {
        return stmt;
    }

}
