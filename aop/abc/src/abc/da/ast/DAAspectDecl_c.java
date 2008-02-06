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

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AspectBody;
import abc.aspectj.ast.AspectDecl_c;
import abc.aspectj.ast.PerClause;
import abc.da.types.DAAspectType;

/**
 * An aspect declaration with possibly dependent advice declarations.
 * Has additional type checks.
 * @author Eric Bodden
 */
public class DAAspectDecl_c extends AspectDecl_c implements DAAspectDecl {

	public DAAspectDecl_c(Position pos, boolean is_privileged, Flags flags,
			String name, TypeNode superClass, List interfaces, PerClause per,
			AspectBody body) {
		super(pos, is_privileged, flags, name, superClass, interfaces, per,
				body);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		DAAspectType type = (DAAspectType) type();
		//check that no advice name occurs twice
		type.checkDuplicateAdviceNames();
		return super.typeCheck(tc);
	}

}
