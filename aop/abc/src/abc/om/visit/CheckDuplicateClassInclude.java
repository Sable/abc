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
 * Created on May 16, 2005
 *
 */
package abc.om.visit;

import java.util.Stack;

import abc.aspectj.ast.AspectBody;
import abc.aspectj.ast.AspectDecl;
import abc.aspectj.visit.PCNode;
import abc.aspectj.visit.PCStructure;
import abc.om.ExtensionInfo;
import abc.om.ast.ModuleDecl;
import abc.om.ast.OpenModNodeFactory;
import polyglot.ast.ClassBody;
import polyglot.ast.ClassDecl;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Checks if a class has been included in more than one module.
 * @author Neil Ongkingco
 */
public class CheckDuplicateClassInclude extends ContextVisitor {
    private ExtensionInfo ext;
    
    public CheckDuplicateClassInclude(Job job, TypeSystem ts, OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }
    
    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        //on encountering a class body
        if (n instanceof ClassBody && !(n instanceof AspectBody)) {
            PCNode node = PCStructure.v().getClass(context().currentClass());
            assert(node!=null) : "Node is null";
            if (ext.moduleStruct.hasMultipleOwners(node)) {
                throw new SemanticException(
                        "Class " + node.toString() + " included in more than one module.", 
                        n.position());
            }
        }
        return super.enterCall(parent, n);
    }
}
