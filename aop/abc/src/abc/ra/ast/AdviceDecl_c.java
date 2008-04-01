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

/*
 * Created on 17-May-07
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package abc.ra.ast;

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Node;
import polyglot.types.Flags;
import polyglot.types.SemanticException;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;
import abc.ra.types.RelAspectType;

/**
 * Custom advice declaration with enhanced type check.
 *
 * @author Eric Bodden
 */
public class AdviceDecl_c extends abc.aspectj.ast.AdviceDecl_c implements
		AdviceDecl {

	/**
	 * @see abc.aspectj.ast.AdviceDecl_c#AdviceDecl_c(Position, Flags, AdviceSpec, List, Pointcut, Block)
	 */
	public AdviceDecl_c(Position pos, Flags flags, AdviceSpec spec,
			List throwTypes, Pointcut pc, Block body) {
		super(pos, flags, spec, throwTypes, pc, body);
	}
	
	/**
	 * In addition to normal type checks, verifies that no {@link RelationalAround} spec is used as advice spec.
	 */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		if(spec instanceof RelationalAround) {
			throw new SemanticException("Proceed-variables are only allowed for *relational* around advice.",position());
		}
		return super.typeCheck(tc);
	}
}
