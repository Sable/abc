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
        Iterator i = formals.iterator();
        Collection must_bind = mustBind();

        while (i.hasNext()) {
            Formal f = (Formal) i.next();
            if (!must_bind.contains(f.name()))
                throw new SemanticException("Formal \"" + f.name() +
                    "\" is not necessarily bound by tracematch.", f.position());
        }

        return super.typeCheck(tc);
    }

    protected Collection mustBind()
    {
        // Get the symbols which must be matched, and therefore
        // the pointcut variables which must be bound.
        Collection matched = regex.mustContain();
        Collection pc_vars = new HashSet();
        Iterator i = symbols.iterator();

        while (i.hasNext()) {
            SymbolDecl sd = (SymbolDecl) i.next();
            if (matched.contains(sd.name()))
                pc_vars.addAll(sd.binds());
        }

        return pc_vars;
    }



    public List generateImplementationAdvice(TMNodeFactory nf, TypeNode voidn)
    {
        // generate advice declarations
        List advice = new LinkedList();
        Iterator j = symbols.iterator();

        while(j.hasNext()) {
            SymbolDecl sd = (SymbolDecl) j.next();
            List formals = new LinkedList(this.formals);
            advice.add( sd.generateAdviceDecl(nf, formals, voidn));
        }

        return advice;
    }
}
