/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Aske Simon Christensen
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

import abc.aspectj.visit.AspectInfoHarvester;
import abc.aspectj.types.AJContext;
import abc.main.Debug;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class PCArgs_c extends Pointcut_c implements PCArgs
{
    protected List pats;

    public PCArgs_c(Position pos, List pats)  {
		super(pos);
        this.pats = copyList(pats);
    }
    
	private List copyList(List xs) {
		return new LinkedList(xs);
	}

	public Set pcRefs() {
		return new HashSet();
	}
	
	public boolean isDynamic() {
		return true;
	}
	
    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCArgs_c reconstruct(List pats) {
		if (! CollectionUtil.equals(pats,this.pats)) {
			PCArgs_c n = (PCArgs_c) copy();
			n.pats =  copyList(pats); // may become a list of TypeNode, Local, ArgStar and ArgDotDot
			return n;
		}
 		return this;
	}

	public Node visitChildren(NodeVisitor v) {
		List pats = visitList(this.pats, v);
		return reconstruct(pats);
	}
	
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		AJContext c = (AJContext) tc.context();
		if (c.inDeclare() && !Debug.v().allowDynamicTests)
			throw new SemanticException("args(..) requires a dynamic test and cannot be used inside a \"declare\" statement", position());
		int count = 0;
		for (Iterator patIt = pats.iterator(); patIt.hasNext(); ) {
			if (patIt.next() instanceof ArgDotDot)
				count++;
		}
		if (count > 1)
			throw new SemanticException("args() may contain at most one occurrence of \"..\".", position());
		return this;
	}
	
	
	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		for (Iterator i = pats.iterator(); i.hasNext(); ) {
			Node pat = (Node) i.next();
			if (pat instanceof Local) {
				String l = ((Local) pat).name();
				 if (result.contains(l))
				     throw new SemanticException("repeated binding of \"" + l +"\"",
				                                                               pat.position());
				 else if (l == Pointcut_c.initialised)
				 			throw new SemanticException("cannot explicitly bind local \"" + l + "\"", pat.position());
				          else result.add(l);
			}
		}
		return result;
	}
   
		public Collection mustBind() {
			Collection result = new HashSet();
			for (Iterator i = pats.iterator(); i.hasNext(); ) {
				Node pat = (Node) i.next();
				if (pat instanceof Local)
					 result.add(((Local)pat).name());
			}
			return result;
		}
   
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("args(");
        for (Iterator i = pats.iterator(); i.hasNext(); ) {
	        Node fp = (Node) i.next();
 		    print(fp, w, tr);

		if (i.hasNext()) {
		    w.write(",");
		    w.allowBreak(4, " ");
		}
	    }
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	List args = AspectInfoHarvester.convertArgPatterns(pats);
	return new abc.weaving.aspectinfo.Args(args,position());
    }

}
