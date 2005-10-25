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
 * Created on May 13, 2005
 *
 */
package abc.om.ast;

import java.util.*;

import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.MakesAspectMethods;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.visit.AspectMethods;
import abc.weaving.aspectinfo.Pointcut;
import polyglot.ast.Node;
import polyglot.ast.Term;
import polyglot.ext.jl.ast.Node_c;
import polyglot.ext.jl.ast.Term_c;
import polyglot.util.CodeWriter;
import polyglot.util.Position;
import polyglot.visit.CFGBuilder;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * A pointcut signature member (only here to support the version of the 
 * code found in the MSc dissertation. Unused with in the newer (advertise/expose)
 * syntax.)
 * @author Neil Ongkingco 
 *
 */
public class SigMemberPCDecl_c extends Node_c implements SigMemberPCDecl, MakesAspectMethods {

    private abc.aspectj.ast.Pointcut pc;
    private boolean isPrivate = false;
    
    public SigMemberPCDecl_c(polyglot.util.Position pos, abc.aspectj.ast.Pointcut pc, boolean isPrivate) {
        super(pos);
        this.pc = pc;
        this.isPrivate = isPrivate;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public Pointcut getAIPointcut() {
        return this.pc.makeAIPointcut();
    }
    public void prettyPrint(CodeWriter w, PrettyPrinter pp) {
        pc.prettyPrint(w, pp);
        w.newline();
        //super.prettyPrint(w, pp);
    }    
    
    public SigMemberPCDecl_c reconstruct(abc.aspectj.ast.Pointcut pc) {
        if (pc != this.pc) {
            SigMemberPCDecl_c n = (SigMemberPCDecl_c)copy();
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

    public void aspectMethodsEnter(AspectMethods visitor) {
        //push an empty list of formals. needed for If pointcuts
        visitor.pushFormals(Collections.unmodifiableList(new LinkedList()));
    }
    public Node aspectMethodsLeave(AspectMethods visitor, AJNodeFactory nf,
            AJTypeSystem ts) {
        return this;
    }
}
