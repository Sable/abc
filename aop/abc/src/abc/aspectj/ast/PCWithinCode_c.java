/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
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

package abc.aspectj.ast;

import polyglot.ast.*;

import polyglot.types.*;
import polyglot.util.*;
import polyglot.visit.*;
import java.util.*;

/**
 * 
 * @author Oege de Moor
 *
 */
public class PCWithinCode_c extends Pointcut_c implements PCWithinCode
{
    protected MethodConstructorPattern pat;

    public PCWithinCode_c(Position pos, MethodConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

	public Set pcRefs() {
		return new HashSet();
	}
	
	public boolean isDynamic() {
		return false;
	}
	
    protected PCWithinCode_c reconstruct(MethodConstructorPattern pat) {
	if (pat != this.pat) {
	    PCWithinCode_c n = (PCWithinCode_c) copy();
	    n.pat = pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	MethodConstructorPattern pat=
	    (MethodConstructorPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("withincode(");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (pat instanceof MethodPattern) {
	    return new abc.weaving.aspectinfo.WithinMethod
		(((MethodPattern)pat).makeAIMethodPattern(),
		 position());
	} else if (pat instanceof ConstructorPattern) {
	    return new abc.weaving.aspectinfo.WithinConstructor
		(((ConstructorPattern)pat).makeAIConstructorPattern(),
		 position());
	} else {
	    throw new RuntimeException
		("Unexpected MethodConstructorPattern type in withincode pointcut: "+pat);
	}
    }
}
