/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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

package abc.om.ast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.Position;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import abc.aspectj.ast.*;
import abc.om.visit.OpenClassFlagSet;

public class OpenClassMember_c extends Node_c implements OpenClassMember {
	protected ClassnamePatternExpr cpe;
	protected ClassnamePatternExpr toClauseCPE;
	
	protected List /*OpenClassMemberFlag*/ memberFlags;
	
	public OpenClassMember_c(List /*OpenClassMemberFlag*/ memberFlags, 
			ClassnamePatternExpr cpe, 
			ClassnamePatternExpr toClauseCPE,
			Position pos) {
		super(pos);
		this.memberFlags = memberFlags;
		this.cpe = cpe;
		this.toClauseCPE = toClauseCPE;
	}
	
	public OpenClassFlagSet getFlags() {
	    OpenClassFlagSet result = new OpenClassFlagSet(memberFlags);
	    return result; 
	}
	
	public ClassnamePatternExpr getCPE() {
	    return this.cpe;
	}
	
	public ClassnamePatternExpr getToClauseCPE() {
	    return this.toClauseCPE;
	}
	
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        // TODO Auto-generated method stub
        w.write("openclass ");
        for (Iterator i = memberFlags.iterator(); i.hasNext();) {
            OpenClassMemberFlag currFlag = (OpenClassMemberFlag) i.next();
            currFlag.prettyPrint(w, pp);
            w.write(", ");
        }
        w.write(" to ");
        toClauseCPE.prettyPrint(w, pp);
        w.write(" : ");
        cpe.prettyPrint(w, pp);
        w.newline();
        //super.prettyPrint(w, pp);
    }
    
    public OpenClassMember_c reconstruct(ClassnamePatternExpr cpe,
            ClassnamePatternExpr toClauseCPE,
            List memberFlags) {
        if (cpe != this.cpe || 
                toClauseCPE != this.toClauseCPE || 
                !CollectionUtil.equals(this.memberFlags, memberFlags)) {
            OpenClassMember_c n = (OpenClassMember_c) copy();
            n.memberFlags = memberFlags;
            n.cpe = cpe;
            n.toClauseCPE = toClauseCPE;
            return n;
        }
        return this;
    }

    
    public Node visitChildren(NodeVisitor v) {
        ClassnamePatternExpr newCPE = (ClassnamePatternExpr) visitChild(cpe,v);
        ClassnamePatternExpr newToClauseCPE = 
            (ClassnamePatternExpr) visitChild(toClauseCPE, v);
        List newMemberFlags = new LinkedList();
        
        for (Iterator i = memberFlags.iterator(); i.hasNext(); ) {
            OpenClassMemberFlag memberFlag = 
                (OpenClassMemberFlag) i.next();
            newMemberFlags.add(visitChild(memberFlag,v));
        }
        
        return reconstruct(newCPE, newToClauseCPE, memberFlags);
    }
}
