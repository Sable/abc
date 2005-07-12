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
import polyglot.visit.*;

import abc.aspectj.ast.*;
import abc.aspectj.extension.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class TMDecl_c extends AJMethodDecl_c
                              implements TMDecl
{
    protected boolean isPerThread;
    protected boolean isAround;
    protected String tracematch_name;
    protected List symbols;
    protected Regex regex;

    public TMDecl_c(Position pos,
                    TMModsAndType mods_and_type,
                    String tracematch_name,
                    List formals,
                    List throwTypes,
                    List symbols,
                    Regex regex,
                    Block body)
    {
        super(pos, mods_and_type.getFlags(), mods_and_type.getReturnType(),
                tracematch_name + "$body", formals, throwTypes, body);

        isPerThread = mods_and_type.isPerThread();
        isAround = mods_and_type.isAround();
        this.tracematch_name = tracematch_name;
        this.symbols = symbols;
        this.regex = regex;
    }

    //
    // visitor handling code
    //
    protected Node reconstruct(Node n, List symbols)
    {
        if (symbols != this.symbols) {
            TMDecl_c new_n = (TMDecl_c) n.copy();
            new_n.symbols = symbols;

            return new_n;
        }
        return n;
    }

    public Node visitChildren(NodeVisitor v)
    {
        Node n = super.visitChildren(v);
        List symbols = visitList(this.symbols, v);
        return reconstruct(n, symbols);
    }
 

    public Node typeCheck(TypeChecker tc) throws SemanticException
    {
        checkAroundSymbols();
        checkBinding();
        return super.typeCheck(tc);
    }

    protected void checkAroundSymbols() throws SemanticException
    {
        Iterator i = symbols.iterator();
        Collection non_final_syms = regex.nonFinalSymbols();
        Collection final_syms = regex.finalSymbols();

        while (i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();

            if (!isAround && sd.kind() == SymbolKind.AROUND)
                throw new SemanticException(
                    "Around symbols may only appear in around tracematches.",
                    sd.position());

            if (sd.kind() == SymbolKind.AROUND
                    && non_final_syms.contains(sd.name()))
                throw new SemanticException(
                    "Around symbol \"" + sd.name() +
                    "\" may match in the middle of a trace.",
                    regex.position());

            if (isAround && sd.kind() != SymbolKind.AROUND
                    && final_syms.contains(sd.name()))
               throw new SemanticException(
                    "Around tracematches must have around final symbols.",
                    sd.position());
        }
    }

    protected void checkBinding() throws SemanticException
    {
        Iterator i = formals.iterator();
        Collection must_bind = mustBind();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (!must_bind.contains(f.name()))
                throw new SemanticException("Formal \"" + f.name() +
                    "\" is not necessarily bound by tracematch.", f.position());
        }

    }

    protected Collection mustBind()
    {
        // create a map from symbol names to the names of pointcut
        // variables that the corresponding pointcut binds
        Map sym_to_vars = new HashMap();
        Iterator i = symbols.iterator();

        while(i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();
            sym_to_vars.put(sd.name(), sd.binds());
        }

        // return the set of pointcut variables which must be bound.
        return regex.mustBind(sym_to_vars);
    }

    public List generateImplementationAdvice(TMNodeFactory nf, TypeNode voidn)
    {
        List advice = new LinkedList();
        List closed_pointcuts = new LinkedList();
        Iterator j = symbols.iterator();
	   	    	
        while(j.hasNext()) {
            SymbolDecl sd = (SymbolDecl) j.next();

            advice.add(sd.generateAdviceDecl(nf, formals, voidn,
                                    tracematch_name, position()));
            closed_pointcuts.add(sd.generateClosedPointcut(nf, formals));
        }

        makeSomeAdvice(nf, advice, closed_pointcuts, voidn);

        return advice;
    }

    protected void makeSomeAdvice(TMNodeFactory nf, List advice,
                                    List pointcuts, TypeNode voidn)
    {
        Map kind_to_pointcut = new HashMap();
        Map kind_to_a_symbol = new HashMap();

        Iterator syms = symbols.iterator();
        Iterator pcs = pointcuts.iterator();

        while (syms.hasNext()) {
            SymbolDecl sd = (SymbolDecl) syms.next();
            Pointcut pc = (Pointcut) pcs.next();

            if (kind_to_pointcut.containsKey(sd.kind()))
                pc = nf.PCBinary(position(),
                                 (Pointcut) kind_to_pointcut.get(sd.kind()),
                                 PCBinary.COND_OR,
                                 pc);

            kind_to_pointcut.put(sd.kind(), pc);
            kind_to_a_symbol.put(sd.kind(), sd);
        }

        Iterator kinds = kind_to_pointcut.keySet().iterator();
        while (kinds.hasNext()) {
            SymbolDecl sd = (SymbolDecl) kind_to_a_symbol.get(kinds.next());
            Pointcut pc = (Pointcut) kind_to_pointcut.get(sd.kind());

            advice.add(sd.generateSomeAdvice(nf, pc, voidn, returnType(),
                                    tracematch_name, position()));
        }
    }
}
