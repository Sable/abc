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
import abc.aspectj.types.AJContext;
import abc.main.Debug;

/**
 * 
 * @author Oege de Moor
 *
 */
public class PCCflowBelow_c extends Pointcut_c implements PCCflowBelow
{
    protected Pointcut pc;

    public PCCflowBelow_c(Position pos, Pointcut pc)  {
	super(pos);
        this.pc = pc;
    }

    public Precedence precedence() {
	return Precedence.LITERAL;
    }
    
	public Set pcRefs() {
		return pc.pcRefs();
	}

	public boolean isDynamic() {
		return true;
	}
	    

    /** Reconstruct the pointcut. */
    protected PCCflowBelow_c reconstruct(Pointcut pc) {
	if (pc != this.pc) {
	    PCCflowBelow_c n = (PCCflowBelow_c) copy();
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


    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
		w.write("cflowbelow(");
        print(pc, w, tr);
        w.write(")");
    }
    
	public Context enterScope(Context c) {
		Context nc = super.enterScope(c);
		return ((AJContext) nc).pushCflow(mustBind());
	}
	  
	public Node typeCheck(TypeChecker tc) throws SemanticException {
			AJContext c = (AJContext) tc.context();
			if (c.inDeclare() && !Debug.v().allowDynamicTests)
				throw new SemanticException("cflowbelow(..) requires a dynamic test and cannot be used inside a \"declare\" statement", position());
			return this;
		}
    
	public Collection mayBind() throws SemanticException {
		return pc.mayBind();
	}
   
	public Collection mustBind() {
		return pc.mustBind();
	}

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	return new abc.weaving.aspectinfo.CflowBelow
	    (pc.makeAIPointcut(),position());
    }

    public String toString() {
	return "cflowbelow("+pc+")";
    }
}
