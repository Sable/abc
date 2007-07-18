/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
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
import abc.eaj.ast.*;

import java.util.*;

/**
 * NodeFactory for TraceMatching extension.
 * @author Julian Tibble
 */
public class TMNodeFactory_c extends EAJNodeFactory_c
                             implements TMNodeFactory
{
    private final AJExtFactory extFactory;
    private final AJDelFactory delFactory;

    public TMNodeFactory_c()
    {
        this(new AJAbstractExtFactory_c() {},new TMAbstractDelFactory_c() {});
    }
    
    public TMNodeFactory_c(AJExtFactory extFactory, AJDelFactory delFactory) {
        this.extFactory = extFactory;
        this.delFactory = delFactory;
    }
    

    public TMDecl
        TMDecl(Position pos, Position body_pos, TMModsAndType mods_and_type,
                        String tracematch_name, List formals, List throwTypes,
                        List symbols, List freqent_symbols,
                        Regex regex, Block body)
    {
        return new TMDecl_c(pos, body_pos, mods_and_type, tracematch_name,
                        formals, throwTypes, symbols, freqent_symbols,
                        regex, body);
    }

    public TMModsAndType
        TMModsAndType(Flags flags, boolean isPerThread,
                      AdviceSpec before_or_around, AdviceSpec after,
                      boolean isAround, TypeNode type)
    {
        return new TMModsAndType_c(flags, isPerThread, before_or_around,
                                    after, isAround, type);
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

    public SymbolKind AfterThrowingSymbol(Position pos)
    {
        return new AfterThrowingSymbol_c(pos);
    }

    public SymbolKind AfterThrowingSymbol(Position pos, Local exception_var)
    {
        return new AfterThrowingSymbol_c(pos, exception_var);
    }

    public SymbolKind AroundSymbol(Position pos, List proceed_vars)
    {
        return new AroundSymbol_c(pos, proceed_vars);
    }

    public TMAdviceDecl PerSymbolAdviceDecl(Position pos, Flags flags,
                            AdviceSpec spec, List throwTypes, Pointcut pc,
                            Block body, String tm_id, SymbolDecl sym, Position tm_pos)
    {
        return new PerSymbolAdviceDecl_c(pos, flags, spec, throwTypes,
                                            pc, body, tm_id, sym, tm_pos);
    }

    public TMAdviceDecl PerEventAdviceDecl(Position pos, Flags flags,
                            AdviceSpec before_spec, Pointcut before_pc,
                            AdviceSpec after_spec, Pointcut after_pc,
                            Block body, String tm_id, Position tm_pos,
                            int kind)
    {
        return new PerEventAdviceDecl_c(pos, flags, before_spec, before_pc,
                                        after_spec, after_pc, body, tm_id,
                                        tm_pos, kind);
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

    public Regex RegexSkipSeq(Position pos, Regex before,
                              List prohibited, Regex after)
    {
        return new RegexSkipSeq_c(pos, before, prohibited, after);
    }

    public ClosedPointcut ClosedPointcut(Position pos, List formals,
                                            Pointcut pc)
    {
        return new ClosedPointcut_c(pos, formals, pc);
    }

    // override Local
    public Local Local(Position pos, String name)
    {
        Local n = super.Local(pos, name);
        n = (Local)n.ext(extFactory.extLocal());
        n = (Local)n.del(delFactory.delLocal());
        return n;
    }
}
