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
import abc.weaving.aspectinfo.Aspect;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.DeclareMessage;

/**
 * declare error : pointcut : message
 * 
 * @author Oege de Moor
 * @author Aske Simon Christensen
 */

public class DeclareError_c extends DeclareDecl_c 
    implements DeclareError, ContainsAspectInfo
{

    Pointcut pc;
    String text;

    public DeclareError_c(Position pos, 
                          Pointcut pc,
                          String text)
    {
	super(pos);
        this.pc   = pc;
        this.text = text;
    }

    protected DeclareError_c reconstruct(Pointcut pc) {
	if (pc != this.pc) {
	    DeclareError_c n = (DeclareError_c) copy();
	    n.pc = pc;
	    return n;
	}
	return this;
    }

    public Node visitChildren(NodeVisitor v) {
	Pointcut pc = (Pointcut) visitChild(this.pc, v);
	return reconstruct(pc);
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("declare error : ");
        print(pc, w, tr);
        w.write(" : ");
        w.write("\""+text+"\"");
        w.write(";");
    }

    public void update(GlobalAspectInfo gai, Aspect current_aspect) {
	gai.addDeclareMessage(new DeclareMessage
			      (DeclareMessage.ERROR,
			       pc.makeAIPointcut(),
			       text,
			       current_aspect,
			       position()));
    }

}
