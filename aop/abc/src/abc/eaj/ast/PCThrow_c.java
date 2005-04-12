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
import polyglot.visit.*;
import polyglot.util.*;

import abc.aspectj.ast.*;

import java.util.*;

/**
 * @author Julian Tibble
 */
public class PCThrow_c extends Pointcut_c
                      implements PCThrow
{
    protected TypePatternExpr type_pattern;

    public PCThrow_c(Position pos, TypePatternExpr type_pattern)
    {
        super(pos);
        this.type_pattern = type_pattern;
    }

    public Set pcRefs() {
        return new HashSet();
    }
	
    public Precedence precedence()
    {
        return Precedence.LITERAL;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp)
    {
        w.write("throw(");
        print(type_pattern, w, pp);
        w.write(")");
    }

    protected PCThrow_c reconstruct(TypePatternExpr type_pattern)
    {
        if (type_pattern != this.type_pattern) {
            PCThrow_c n = (PCThrow_c) copy();
            n.type_pattern = type_pattern;
            return n;
        }
        return this;
    }

    public Node visitChildren(NodeVisitor v)
    {
        TypePatternExpr type_pattern =
            (TypePatternExpr) visitChild(this.type_pattern, v);
        return reconstruct(type_pattern);
    }


    public boolean isDynamic() {
        return false;
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut()
    {
        return new abc.eaj.weaving.aspectinfo.Throw
              (type_pattern.makeAITypePattern(), position());
    }
}
