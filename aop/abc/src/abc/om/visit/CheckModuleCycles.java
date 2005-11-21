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
 * Created on May 15, 2005
 *
 */
package abc.om.visit;

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import abc.om.ExtensionInfo;
import abc.om.ast.ModuleBody;
import abc.om.ast.ModuleDecl;
import abc.om.ast.OpenModNodeFactory;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Checks for any cycles in the module hierarchy. Rather inefficient.
 * 
 * @author Neil Ongkingco
 */
public class CheckModuleCycles extends ContextVisitor {

    private ExtensionInfo ext;

    public CheckModuleCycles(Job job, TypeSystem ts, OpenModNodeFactory nf,
            ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        if (n instanceof ModuleDecl) {
            ModuleDecl decl = (ModuleDecl) n;
            ModuleNode module = ext.moduleStruct.getNode(decl.name(),
                    ModuleNode.TYPE_MODULE);
            assert(module != null) : "Node is null"; //should have already been caught in
                                    // previous passes

            Stack nodeParents = new Stack();
            nodeParents.push(module);
            while (module.getParent() != null) {
                module = module.getParent();
                if (nodeParents.contains(module)) {
                    throw new SemanticException(decl.name()
                            + " involved in circular module inclusion.", decl
                            .namePos());

                }
                nodeParents.add(decl);
            }
        }
        return super.enterCall(parent, n);
    }

}
