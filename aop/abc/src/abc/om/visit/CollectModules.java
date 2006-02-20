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
 * Created on May 14, 2005
 *
 */
package abc.om.visit;

import abc.om.ExtensionInfo;
import abc.om.ast.ModuleBody;
import abc.om.ast.ModuleDecl;
import abc.om.ast.OpenModNodeFactory;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;

/**
 * Adds all module names to ExtensionInfo.modules. Throws an error if a duplicate
 * module name is found.
 * 
 * @author Neil Ongkingco
 *
 */
public class CollectModules extends ContextVisitor {
    private ExtensionInfo ext;
    
    public CollectModules(Job job, TypeSystem ts, OpenModNodeFactory nf, ExtensionInfo ext) {
        super(job, ts, nf);
        this.ext = ext;
    }

    protected NodeVisitor enterCall(Node parent, Node n)
            throws SemanticException {
        //adds module to ModuleStruct, throws an error if there is a duplicate.
        if (n instanceof ModuleDecl) {
            ModuleDecl decl = (ModuleDecl) n;
            ModuleNode node = 
                ext.moduleStruct.addModuleNode(decl.name(), decl.isRoot(), decl.position());
            if (node == null) {
                throw new SemanticException("Duplicate module name", decl.namePos());
            }
            
        }
        return super.enterCall(parent, n);
    }
    
}
