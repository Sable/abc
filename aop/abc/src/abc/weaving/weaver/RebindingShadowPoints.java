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

package abc.weaving.weaver;

/** A different version of {@link ShadowPoints} which uses
 *  nop statements that were inserted before the unweaver
 *  saves copies of methods. Therefore, to retrieve the
 *  correct nops during weaving, the stored nops need to
 *  be mapped to the new versions.
 *  @author Ganesh Sittampalam
 */

import soot.SootMethod;
import soot.jimple.Stmt;

public class RebindingShadowPoints extends ShadowPoints {
    public RebindingShadowPoints(SootMethod container,Stmt b, Stmt e) {
        super(container,b,e);
    }

    public Stmt getBegin(){
        return (Stmt)Weaver.rebind(super.getBegin());
    }

    public Stmt getEnd(){
        return (Stmt)Weaver.rebind(super.getEnd());
    }

    public String toString(){
        return ("RebindingShadowPoint< begin:" + super.getBegin() + " end:" + super.getEnd() + " >");
    }

}
