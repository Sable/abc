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

import polyglot.ext.jl.ast.Node_c;

import java.util.*;

/**
 * 
 * @author Oege de Moor
 *
 */
public class PCBinary_c extends Pointcut_c implements PCBinary
{
    protected Pointcut left;
    protected Operator op;
    protected Pointcut right;
    protected Precedence precedence;

    public PCBinary_c(Position pos, Pointcut left, Operator op, Pointcut right)    {
		super(pos);
        this.left = left;
		this.op = op;
		this.right = right;
		this.precedence = op.precedence();
    }

    public Precedence precedence() {
        return precedence;
    }
    
	public Set pcRefs() {
		Set pcls = left.pcRefs();
		pcls.addAll(right.pcRefs());
		return pcls;
	}
	
	public boolean isDynamic() {
		return left.isDynamic() || right.isDynamic();
	}

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		printSubExpr(left, true, w, tr);
		w.write(" ");
		w.write(op.toString());
		w.allowBreak(2, " ");
		printSubExpr(right, false, w, tr);
    }
    
	protected PCBinary_c reconstruct(Pointcut left,
	                                 Pointcut right) {
		if (left != this.left || op != this.op || right != this.right) {
			PCBinary_c n = (PCBinary_c) copy();
			n.left = left;
			n.right = right;
			return n;
		}

		return this;
	}

	public Node visitChildren(NodeVisitor v) {
		Pointcut left = (Pointcut) visitChild(this.left,v);
		Pointcut right = (Pointcut) visitChild(this.right,v);
		return reconstruct(left,right);
	}

	public Collection mayBind() throws SemanticException {
		Collection result = left.mayBind();
		Collection result2 = right.mayBind();
  		for (Iterator i = result2.iterator(); i.hasNext(); ) {
			String pat = (String) i.next();
		    if (op == PCBinary.COND_AND && result.contains(pat))
			    throw new SemanticException("Repeated binding of \""+ pat +"\".",
																			   position()); // somewhat inaccurate position info
		    else result.add(pat);
		}
		return result;
	}
   
		public Collection mustBind() {
			Collection result = left.mustBind();
			if (op == PCBinary.COND_AND)
				result.addAll(right.mustBind());
			else if (op == PCBinary.COND_OR)
			    result.retainAll(right.mustBind());
			return result;
		}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (op == PCBinary.COND_AND) {
	    return abc.weaving.aspectinfo.AndPointcut.construct(left.makeAIPointcut(),right.makeAIPointcut(), position());
	} else if (op == PCBinary.COND_OR) {
	    return abc.weaving.aspectinfo.OrPointcut.construct(left.makeAIPointcut(),right.makeAIPointcut(), position());
	} else {
	    throw new InternalCompilerError("Unexpected binary pointcut operation: "+op,position());
	}
    }
}
