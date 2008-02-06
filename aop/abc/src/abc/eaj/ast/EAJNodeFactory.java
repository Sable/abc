/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.eaj.ast;

import polyglot.ast.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 * @author Julian Tibble
 * @author Pavel Avgustinov
 * @author Eric Bodden
 */
public interface EAJNodeFactory extends AJNodeFactory {
    // TODO: Declare any factory methods for new AST nodes.

    public PCCast PCCast(Position pos, TypePatternExpr type_pattern);
    
    public PCThrow PCThrow(Position pos, TypePatternExpr type_pattern);

    public PCLocalVars PCLocalVars(Position pos, List varlist, Pointcut pc);

    public GlobalPointcutDecl GlobalPointcutDecl(
                                    Position pos,
                                    ClassnamePatternExpr aspect_pattern,
                                    Pointcut pc);

    public PCCflowDepth PCCflowDepth(Position pos, Local var, Pointcut pc);
    public PCCflowBelowDepth PCCflowBelowDepth(Position pos, Local var, Pointcut pc);

    public PCLet PCLet(Position pos, Local var, Expr expr);
    public PCContains PCContains(Position pos, Pointcut param);
    
    public PCArrayGet PCArrayGet(Position pos);
    public PCArraySet PCArraySet(Position pos);
    
    public PCLock PCLock(Position pos);
    public PCUnlock PCUnlock(Position pos);
    public PCMaybeShared PCMaybeShared(Position pos);
}
