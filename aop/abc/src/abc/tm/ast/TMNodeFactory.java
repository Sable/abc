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
public interface TMNodeFactory extends AJNodeFactory
{
    TMDecl
        TMDecl(Position pos, TMModsAndType mods_and_type,
                        String tracematch_name, List formals, List throwTypes,
                        List symbols, Regex regex, Block body);

    TMModsAndType
        TMModsAndType(Flags flags, boolean isPerThread,
                                boolean isAround, TypeNode type);

    SymbolDecl SymbolDecl(Position pos, String name,
                            SymbolKind kind, Pointcut pc);

    SymbolKind BeforeSymbol(Position pos);
    SymbolKind AfterSymbol(Position pos);
    SymbolKind AfterReturningSymbol(Position pos);
    SymbolKind AfterReturningSymbol(Position pos, Local return_var);
    SymbolKind AfterThrowingSymbol(Position pos, Local exception_var);
    SymbolKind AroundSymbol(Position pos, List proceed_vars);

    TMAdviceDecl TMAdviceDecl(Position pos, Flags flags, AdviceSpec spec,
                                List throwTypes, Pointcut pc, Block body,
                                String tm_id, Position tm_pos, boolean is_some);

    Regex RegexAlternation(Position pos, Regex a, Regex b);
    Regex RegexConjunction(Position pos, Regex a, Regex b);
    Regex RegexCount(Position pos, Regex a, int min, int max);
    Regex RegexPlus(Position pos, Regex a);
    Regex RegexStar(Position pos, Regex a);
    Regex RegexSymbol(Position pos, String name);

    ClosedPointcut ClosedPointcut(Position pos, List formals, Pointcut pc);
}
