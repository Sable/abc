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
import abc.weaving.aspectinfo.AbcFactory;
import abc.aspectj.types.AJContext;
import abc.main.Debug;

/**
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class PCThis_c extends Pointcut_c implements PCThis
{
    protected Node pat; // ArgPattern, becomes TypeNode, Local or ArgStar

    public PCThis_c(Position pos, ArgPattern pat)  {
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
    	return true;
    }
    
	/** Reconstruct the pointcut. */
	protected PCThis_c reconstruct(Node pat) {
	 if (pat != this.pat) {
		   PCThis_c n = (PCThis_c) copy();
		   n.pat = pat;
		   return n;
		}
		return this;
	}

		/** Visit the children of the pointcut. */
	public Node visitChildren(NodeVisitor v) {
		Node pat = (Node) visitChild(this.pat, v);
		return reconstruct(pat);
	}
	


	/** type check the use of  this */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
	   TypeSystem ts = tc.typeSystem();
	   AJContext c = (AJContext) tc.context();
	   
	   	
		if (pat instanceof TPEUniversal)
			return this;
		
		if (pat instanceof ArgStar)
			return this;
		
		if (! ((pat instanceof Typed) && ((Typed)pat).type() instanceof ReferenceType))
		   throw new SemanticException("Argument of \"this\" must be of reference type",pat.position());
		   
		if (c.inDeclare() && !Debug.v().allowDynamicTests)
			throw new SemanticException("this(..) requires a dynamic test and cannot be used inside a \"declare\" statement",position());
		  
		return this;
	}
	
	public Collection mayBind() throws SemanticException {
		Collection result = new HashSet();
		if (pat instanceof Local) {
				String l = ((Local)pat).name();
				if (l == Pointcut_c.initialised)
							throw new SemanticException("cannot explicitly bind local \"" + l + "\"", pat.position());
				result.add(((Local)pat).name());
		}
		return result;
	}
   
	public Collection mustBind() {
		Collection result = new HashSet();
			if (pat instanceof Local)
				result.add(((Local)pat).name());
			 return result;
	}
 

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("this(");
        print(pat, w, tr);
        w.write(")");
    }
    
    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	if (pat instanceof Local) {
	    return new abc.weaving.aspectinfo.ThisVar
		(new abc.weaving.aspectinfo.Var(((Local)pat).name(),
						((Local)pat).position()),
		 position());
	} else if (pat instanceof TypeNode) {
	    return new abc.weaving.aspectinfo.ThisType
		(AbcFactory.AbcType(((TypeNode)pat).type()),
		 position());
	} else if (pat instanceof ArgStar) {
	    return new abc.weaving.aspectinfo.ThisAny(position());
	} else {
	    throw new RuntimeException("Unexpected pattern in this pointcut: "
				       +pat);
	}
    }
    

}
