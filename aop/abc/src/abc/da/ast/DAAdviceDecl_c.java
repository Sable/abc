/* abc - The AspectBench Compiler
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

package abc.da.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;
import abc.eaj.extension.EAJAdviceDecl_c;

/**
 * The declaration of a possibly dependent advice.
 * @author Eric Bodden
 */
public class DAAdviceDecl_c extends EAJAdviceDecl_c implements DAAdviceDecl
{	
    public DAAdviceDecl_c(Position pos, Flags flags,
                           AdviceSpec spec, List throwTypes,
                           Pointcut pc, Block body)
    {
        super(pos, flags, spec, throwTypes, pc, body);
    }

    /**
     * Extends type checking, validating that only dependent advice are named.
     */
    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
    	if(spec.ext() instanceof NameExtension) {    		
    		NameExtension nameExtension = (NameExtension) spec.ext();    		
			if(nameExtension.getName()!=null && !flags.intersects(DEPENDENT)) {
    			throw new SemanticException("Only dependent advice can be given a name."+position());
    		}    		
    	}
    	return super.typeCheck(tc);
    }
    
}
