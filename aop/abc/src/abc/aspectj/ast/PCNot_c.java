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
 * Negation of a pointcut.
 * @author Oege de Moor
 *
 */
public class PCNot_c extends Pointcut_c implements PCNot
{
    protected Pointcut pc;

    public PCNot_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.UNARY;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("!");
        printSubExpr(pc, true, w, tr);
    }
    
	public Set pcRefs() {
		return pc.pcRefs();
	}
	
	public boolean isDynamic() {
		return pc.isDynamic();
	}
	
	/** Reconstruct the pointcut. */
	protected PCNot_c reconstruct(Pointcut pc) {
		 if (pc != this.pc) {
			   PCNot_c n = (PCNot_c) copy();
			   n.pc = pc;
			   return n;
		 }

		 return this;
	}

	/** Visit the children of the pointcut. */
	public Node visitChildren(NodeVisitor v) {
		 Pointcut pc = (Pointcut) visitChild(this.pc, v);
		 return reconstruct(pc);
	}

	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		Collection pcmaybind = pc.mayBind();
		for (Iterator i = pcmaybind.iterator(); i.hasNext(); ) {
		    String l = (String) i.next();
		    throw new SemanticException("Cannot bind variable "+l+" under negation",position());
		}
		return result;
	}
   
	public Collection mustBind() {
		return new HashSet();
	}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return abc.weaving.aspectinfo.NotPointcut.construct
	    (pc.makeAIPointcut(),
	     position());
    }
}
