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

import abc.aspectj.visit.ContainsAspectInfo;
import abc.aspectj.visit.AspectInfoHarvester;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Aspect;

import abc.weaving.aspectinfo.AbcFactory;

/**
 *  declare soft : <exceptiontype> : <pointcut>
 * 
 *  @author Oege de Moor
 *  @author Aske Simon Christensen
 *
 */
public class DeclareSoft_c extends DeclareDecl_c 
    implements DeclareSoft, ContainsAspectInfo
{

    TypeNode type;
    Pointcut pc;

    public DeclareSoft_c(Position pos, 
                         TypeNode type,
                         Pointcut pc)
    {
	super(pos);
        this.type = type;
        this.pc   = pc;
    }

    protected DeclareSoft_c reconstruct(TypeNode type, Pointcut pc) {
	if (type != this.type || pc != this.pc) {
	    DeclareSoft_c n = (DeclareSoft_c) copy();
	    n.type = type;
	    n.pc = pc;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	TypeNode type = (TypeNode) visitChild(this.type, v);
	Pointcut pc = (Pointcut) visitChild(this.pc, v);
	return reconstruct(type, pc);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare soft : ");
        print(type,w,tr);
        w.write(" : ");
        print(pc, w, tr);
        w.write(";");
    }

    public void update(GlobalAspectInfo gai, Aspect currrent_aspect) {
	abc.weaving.aspectinfo.DeclareSoft ds = new abc.weaving.aspectinfo.DeclareSoft
	    (AbcFactory.AbcType(type.type()),
	     pc.makeAIPointcut(),
	     currrent_aspect,
	     position());
	gai.addDeclareSoft(ds);
    }

}
