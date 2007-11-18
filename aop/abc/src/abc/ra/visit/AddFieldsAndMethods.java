/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
 * Copyright (C) 2007 Eric Bodden
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
package abc.ra.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import abc.ra.ast.RelAspectDecl;

/**
 * Visitor which adds required fields and associate/release methods to relational aspects in the front-end.
 *
 * @author Reehan Shaikh
 * @author Eric Bodden
 */
public class AddFieldsAndMethods extends NodeVisitor
{
    private final NodeFactory nf;
	private final TypeSystem ts;

    public AddFieldsAndMethods(NodeFactory nf, TypeSystem ts)
    {
        this.nf = nf;
		this.ts = ts;
    }

    /**
     * Adds a field <code>T t</code> for every such relational aspect formal.
     * Also, adds associate and release methods.
     */
    public Node leave(Node old, Node n, NodeVisitor v) {
        if (n instanceof RelAspectDecl)
        {
			RelAspectDecl decl = (RelAspectDecl) n;
			// insert helper methods into relational aspect
			decl = decl.declareMethods(nf, ts);
			return super.leave(old, decl, v);
		}
        // if not a relational aspect, call parent
    	return super.leave(old, n, v);
    }
}