/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Eric Bodden
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
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.AspectDecl;
import abc.aspectj.ast.PerClause;
import abc.aspectj.ast.Pointcut;
import abc.eaj.ast.EAJNodeFactory_c;

/**
 * NodeFactory for Dependent Advice extension.
 * @author Eric Bodden
 */
public class DANodeFactory_c extends EAJNodeFactory_c
                              implements DANodeFactory
{

    public DANodeFactory_c() {
        super(new DAExtFactory_c(), new DADelFactory_c());
    }
    
    public DANodeFactory_c(DAExtFactory_c nextExtFactory) {
        super(nextExtFactory, new DADelFactory_c());
    }
    
    public DANodeFactory_c(DAExtFactory_c nextExtFactory, DADelFactory_c nextDelFactory) {
        super(nextExtFactory, nextDelFactory);
    }

	public AdviceDependency AdviceDependency(Position pos,
			List<abc.da.ast.AdviceNameAndParams> strongAdvice,
			List<abc.da.ast.AdviceNameAndParams> weakAdvice) {
    	AdviceDependency n = new AdviceDependency_c(pos,strongAdvice,weakAdvice);
    	n = (AdviceDependency)n.ext(((DAExtFactory)extFactory()).extAdviceDependency());
    	n = (AdviceDependency)n.del(((DADelFactory)delFactory()).delAdviceDependency());
		return n;
	}

	public AdviceNameAndParams AdviceNameAndParams(Position pos, String name,
			List<String> argumentOrder) {
    	AdviceNameAndParams n = new AdviceNameAndParams_c(pos,name,argumentOrder);
    	n = (AdviceNameAndParams)n.ext(((DAExtFactory)extFactory()).extAdviceNameAndParam());
    	n = (AdviceNameAndParams)n.del(((DADelFactory)delFactory()).delAdviceNameAndParam());
		return n;
	}
	
	public AdviceName AdviceName(Position pos, String name) {
    	AdviceName n = new AdviceName_c(pos,name);
    	n = (AdviceName)n.ext(((DAExtFactory)extFactory()).extAdviceName());
    	n = (AdviceName)n.del(((DADelFactory)delFactory()).delAdviceName());
    	return n;
	}
	
	@Override
	public AspectDecl AspectDecl(Position pos,
			boolean is_privileged, Flags flags, String name,
			TypeNode superClass, List interfaces, PerClause per,
			abc.aspectj.ast.AspectBody body) {
    	AspectDecl n = new DAAspectDecl_c(pos, is_privileged, flags, name, superClass,
				interfaces, per, body);
    	n = (AspectDecl)n.ext(((DAExtFactory)extFactory()).extAspectDecl());
    	n = (AspectDecl)n.del(((DADelFactory)delFactory()).delAspectDecl());
		return n;
	}
	
	@Override
	public AdviceDecl AdviceDecl(Position pos, Flags flags, AdviceSpec spec,
			List throwTypes, Pointcut pc, Block body) {
    	AdviceDecl n = new DAAdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
    	n = (AdviceDecl)n.ext(((DAExtFactory)extFactory()).extAdviceDecl());
    	n = (AdviceDecl)n.del(((DADelFactory)delFactory()).delAdviceDecl());
		return n;
	}
}
