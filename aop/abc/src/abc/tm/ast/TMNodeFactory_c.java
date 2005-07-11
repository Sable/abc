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

package abc.tm.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * NodeFactory for TraceMatching extension.
 * @author Julian Tibble
 */
public class TMNodeFactory_c extends AJNodeFactory_c
                             implements TMNodeFactory
{
    public TMDecl
        TMDecl(Position pos, TMModsAndType mods_and_type,
                        String tracematch_name, List formals, List throwTypes,
                        List symbols, Regex regex, Block body)
    {
        return new TMDecl_c(pos, mods_and_type, tracematch_name,
                        formals, throwTypes, symbols, regex, body);
    }

    public TMModsAndType
        TMModsAndType(Flags flags, boolean isPerThread,
                                boolean isAround, TypeNode type)
    {
        return new TMModsAndType_c(flags, isPerThread, isAround, type);
    }

    public SymbolDecl SymbolDecl(Position pos, String name,
                            SymbolKind kind, Pointcut pc)
    {
        return new SymbolDecl_c(pos, name, kind, pc);
    }

    public SymbolKind BeforeSymbol(Position pos)
    {
        return new BeforeSymbol_c(pos);
    }

    public SymbolKind AfterSymbol(Position pos)
    {
        return new AfterSymbol_c(pos);
    }

    public SymbolKind AfterReturningSymbol(Position pos)
    {
        return new AfterReturningSymbol_c(pos);
    }

    public SymbolKind AfterReturningSymbol(Position pos, Local return_var)
    {
        return new AfterReturningSymbol_c(pos, return_var);
    }

    public SymbolKind AfterThrowingSymbol(Position pos, Local exception_var)
    {
        return new AfterThrowingSymbol_c(pos, exception_var);
    }

    public SymbolKind AroundSymbol(Position pos, List proceed_vars)
    {
        return new AroundSymbol_c(pos, proceed_vars);
    }

    public TMAdviceDecl TMAdviceDecl(Position pos, Flags flags,
                    AdviceSpec spec, List throwTypes, Pointcut pc, Block body)
    {
        return new TMAdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
    }

    public Regex RegexAlternation(Position pos, Regex a, Regex b)
    {
        return new RegexAlternation_c(pos, a, b);
    }

    public Regex RegexConjunction(Position pos, Regex a, Regex b)
    {
        return new RegexConjunction_c(pos, a, b);
    }
 
    public Regex RegexCount(Position pos, Regex a, int min, int max)
    {
        return new RegexCount_c(pos, a, min, max);
    }

    public Regex RegexPlus(Position pos, Regex a)
    {
        return new RegexPlus_c(pos, a);
    }

    public Regex RegexStar(Position pos, Regex a)
    {
        return new RegexStar_c(pos, a);
    }

    public Regex RegexSymbol(Position pos, String name)
    {
        return new RegexSymbol_c(pos, name);
    }
}
