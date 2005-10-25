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

/*
 * Created on May 30, 2005
 *
 */
package abc.om.ast;

import abc.aspectj.ast.MethodConstructorPattern;
import abc.aspectj.ast.PCCall_c;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ext.jl.ast.Node_c;
import polyglot.util.CodeWriter;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A method signature member (only here to support the version of the 
 * code found in the MSc dissertation. Unused with in the newer (advertise/expose)
 * syntax.)
 * @author Neil Ongkingco
 *
 */
public class SigMemberMethodDecl_c extends Node_c implements
        SigMemberMethodDecl {

    private abc.aspectj.ast.Pointcut pc;
    private boolean isPrivate = false;
    
    public SigMemberMethodDecl_c(polyglot.util.Position pos, 
            MethodConstructorPattern methodPattern, boolean isPrivate) {
        super(pos);
        //store the call pointcut that represents the method
        this.pc = new PCCall_c(pos, methodPattern);
        this.isPrivate = isPrivate;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }

    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        pc.prettyPrint(w, pp);
        w.newline();
        //super.prettyPrint(w, pp);
    }    
    
    public SigMemberMethodDecl_c reconstruct(abc.aspectj.ast.Pointcut pc) {
        if (pc != this.pc) {
            SigMemberMethodDecl_c n = (SigMemberMethodDecl_c)copy();
            n.pc = pc;
            return n;
        }
        return this;
    }
    
    public Node visitChildren(NodeVisitor v) {
        abc.aspectj.ast.Pointcut pc = 
            (abc.aspectj.ast.Pointcut)visitChild(this.pc, v);
        return reconstruct(pc);
    }

    /* (non-Javadoc)
     * @see abc.openmod.ast.SigMember#getAIPointcut()
     */
    public Pointcut getAIPointcut() {
        // Only returns the AI pointcut for the call pointcut
        // The pass CheckModuleMembers should conjoin the !within pointcut
        return this.pc.makeAIPointcut();
    }
}
