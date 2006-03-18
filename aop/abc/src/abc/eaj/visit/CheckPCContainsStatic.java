/* abc - The AspectBench Compiler
 * Copyright (C) 2006 Neil Ongkingco
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

package abc.eaj.visit;

import abc.aspectj.ast.Pointcut;
import abc.eaj.ast.EAJNodeFactory;
import abc.eaj.ast.GlobalPointcutDecl;
import abc.eaj.ast.PCContains;
import abc.eaj.ast.PCContains_c;
import abc.eaj.types.EAJTypeSystem;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/**
 * Checks if the parameter of a contains pointcut is static
 * @author Neil Ongkingco
 *
 */
public class CheckPCContainsStatic extends ContextVisitor {
    
    public CheckPCContainsStatic(Job job, EAJTypeSystem ts, EAJNodeFactory nf)
    {
        super(job, ts, nf);
    }
    
    protected NodeVisitor enterCall(Node parent, Node n) 
    		throws SemanticException {
        if (n instanceof PCContains_c) {
            Pointcut param = ((PCContains)n).getParam();
            if (param.isDynamic()) {
	            throw new SemanticException(
	                    "The parameter of the contains() pointcut must be a static pointcut", 
	                    n.position());
            }
        }
        return super.enterCall(parent, n);
    }
}
