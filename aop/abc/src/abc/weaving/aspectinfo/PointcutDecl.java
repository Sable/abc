/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
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

package abc.weaving.aspectinfo;

import polyglot.util.Position;

import soot.*;

import java.util.*;

/** A pointcut declaration. 
 *  @author Aske Simon Christensen
 */
public class PointcutDecl extends InAspect {
    private String name;
    private List/*<Formal>*/ formals;
    private Pointcut pc;

    /** Create a pointcut declaration.
     *  @param name the name of the pointcut.
     *  @param formals a list of {@link abc.weaving.aspectinfo.Formal} objects
     *  @param pc the pointcut, or <code>null</code> if the declaration is abstract.
     */
    public PointcutDecl(String name, List formals, Pointcut pc, Aspect aspct, Position pos) {
	super(aspct, pos);
	this.name = name;
	this.formals = formals;
	this.pc = pc;
    }

    public String getName() {
	return name;
    }

    /** Get the formals of the pointcut declaration.
     *  @return a list of {@link abc.weaving.aspectinfo.Formal} objects.
     */
    public List getFormals() {
	return formals;
    }

    public boolean isAbstract() {
	return pc == null;
    }

    public Pointcut getPointcut() {
	return pc;
    }

    public String toString() {
	return "pointcut "+name+"(...): "+pc;
    }

}
