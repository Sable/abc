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
public class PCExecution_c extends Pointcut_c implements PCExecution
{
    protected MethodConstructorPattern pat;

    public PCExecution_c(Position pos, MethodConstructorPattern pat)  {
	super(pos);
        this.pat = pat;
    }
    
	public Set pcRefs() {
		return new HashSet();
	}
	
	public boolean isDynamic() {
		return false;
	}

    public Precedence precedence() {
	return Precedence.LITERAL;
    }

    protected PCExecution_c reconstruct(MethodConstructorPattern pat) {
	if(pat != this.pat) {
	    PCExecution_c n=(PCExecution_c) copy();
	    n.pat=pat;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	MethodConstructorPattern pat
	    = (MethodConstructorPattern) visitChild(this.pat,v);
	return reconstruct(pat);
    }

	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if (pat instanceof ConstructorPattern) {
			ClassTypeDotNew name = ((ConstructorPattern) pat).getName();
			ClassnamePatternExpr base = name.base();
			if (base instanceof CPEName) {
				CPEName basename = (CPEName) base;
				NamePattern namepat = basename.getNamePattern();
				if (namepat instanceof SimpleNamePattern) {
					SimpleNamePattern snp = (SimpleNamePattern) namepat;
					String pat = snp.getPatternString();
					Context ctxt = tc.context();
					Named t = null;
					try {
						t = ctxt.find(pat);
					} catch (SemanticException e) {/* skip */ }
					if (t instanceof ClassType) {
						if (((ClassType)t).flags().isInterface()) {
							if (abc.main.Main.v() == null)
								System.out.println("1");
							if (abc.main.Main.v().error_queue ==null) {
								System.out.println("2");
							}
							abc.main.Main.v().error_queue.enqueue(ErrorInfo.WARNING,
							"Interface constructor execution is not a join point (consider (..)+ to capture constructors of implementors)", 
							position());
						}
					}	
				}
			}
		}
		return this;
	}
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
	w.write("execution (");
        print(pat, w, tr);
        w.write(")");
    }

    public abc.weaving.aspectinfo.Pointcut makeAIPointcut() {
	abc.weaving.aspectinfo.Pointcut withincode;
	if (pat instanceof MethodPattern) {
	    withincode=new abc.weaving.aspectinfo.WithinMethod
		(((MethodPattern)pat).makeAIMethodPattern(),
		 position());
	} else if (pat instanceof ConstructorPattern) {
	    withincode=new abc.weaving.aspectinfo.WithinConstructor
		(((ConstructorPattern)pat).makeAIConstructorPattern(),
		 position());
	} else {
	    throw new RuntimeException
		("Unexpected MethodConstructorPattern type in execution pointcut: "+pat);
	}
	return abc.weaving.aspectinfo.AndPointcut.construct
	    (withincode,
	     new abc.weaving.aspectinfo.Execution(position()),
	     position());
    }
}
