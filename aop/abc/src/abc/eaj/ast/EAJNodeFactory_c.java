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
import polyglot.types.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import abc.eaj.extension.*;

import java.util.*;

/**
 * NodeFactory for Extended AspectJ extension.
 * @author Julian Tibble
 */
public class EAJNodeFactory_c extends AJNodeFactory_c
                              implements EAJNodeFactory
{
    // TODO:  Implement factory methods for new AST nodes.
    // TODO:  Override factory methods for overriden AST nodes.
    // TODO:  Override factory methods for AST nodes with new extension nodes.

    public PCCast PCCast(Position pos, TypePatternExpr type_pattern)
    {
        return new PCCast_c(pos, type_pattern);
    }
    
    public PCThrow PCThrow(Position pos, TypePatternExpr type_pattern)
    {
    	
    	return new PCThrow_c(pos, type_pattern);
    }

    public PCLocalVars PCLocalVars(Position pos,
                                   List varlist,
                                   Pointcut pc)
    {
        return new PCLocalVars_c(pos, varlist, pc);
    }

    public GlobalPointcutDecl GlobalPointcutDecl(
                                    Position pos,
                                    ClassnamePatternExpr aspect_pattern,
                                    Pointcut pc)
    {
        return new GlobalPointcutDecl_c(pos, aspect_pattern,pc);
    }

    public AdviceDecl AdviceDecl(Position pos, Flags flags,
                                 AdviceSpec spec, List throwTypes,
                                 Pointcut pc, Block body)
    {
        return new EAJAdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
    }

    public PCCflowDepth PCCflowDepth(Position pos, Local var, Pointcut pc) {
        return new PCCflowDepth_c(pos,pc,var);
    }

    public PCCflowBelowDepth PCCflowBelowDepth(Position pos, Local var, Pointcut pc) {
        return new PCCflowBelowDepth_c(pos,pc,var);
    }

    public PCLet PCLet(Position pos, Local var, Expr expr)
    {
        return new PCLet_c(pos, var, expr);
    }
    
    public PCContains PCContains(Position pos, Pointcut param) {
        return new PCContains_c(pos, param);
    }
}
