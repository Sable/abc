/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Eric Bodden
 * Copyright (C) 2007 Reehan Shaikh
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

package abc.ra.ast;

import java.util.LinkedList;
import java.util.List;

import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.Pointcut;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMNodeFactory;

/**
 * Symbol declaraton of a symbol generated from an advice.
 * If this symbol never matches, it gives a warning that the <i>advice</i> (instead of symbol) never matches.
 *
 * @author Eric Bodden
 */
public class AdviceSymbolDeclaration_c extends SymbolDecl_c {

	protected final boolean giveWarning;

	/**
	 * @param giveWarning if true, the warning is given, if false not
	 */
	public AdviceSymbolDeclaration_c(Position pos, String name, SymbolKind kind,
			Pointcut pc, boolean giveWarning) {
		super(pos, name, kind, pc);
		this.giveWarning = giveWarning;
	}
	
    public AdviceDecl generateSymbolAdvice(TMNodeFactory nf, List formals,
            TypeNode voidn, String tm_id, Position tm_pos)
	{
		// Generate AdviceSpec
		AdviceSpec spec = kind.generateAdviceSpec(nf, formals, voidn);
		
		// Generate an empty `throws' list
		List tlist = new LinkedList();
		
		// Generate the TMAdviceDecl
		return ((RANodeFactory) nf).CustomWarningPerSymbolAdviceDecl(position(), Flags.NONE, spec,
		            tlist, pc, body(nf, name, voidn),
		            tm_id, this, tm_pos,
		            giveWarning ? CustomWarningPerSymbolAdviceDecl.ADVICE : CustomWarningPerSymbolAdviceDecl.NONE
		);
	}

}
