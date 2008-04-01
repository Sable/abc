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

package abc.ra.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.FormalPattern;
import abc.aspectj.ast.ModifierPattern;
import abc.aspectj.ast.Pointcut;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.SymbolDecl_c;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMNodeFactory;

public class StartSymbolDecl_c extends SymbolDecl_c implements SymbolDecl {

	private static final Position POS = Position.compilerGenerated();

	public StartSymbolDecl_c(Position pos, String name, TMNodeFactory nf) {
		super(pos,name,createKind(nf),createPC(nf));
	}

	/**
	 * Creates pointcut <code>execution(* *.main(String[]))</code>
	 */
	private static Pointcut createPC(TMNodeFactory nf) {
		List<ModifierPattern> mods = new LinkedList<ModifierPattern>();
		mods.add(nf.ModifierPattern(POS, Flags.PUBLIC, true));
		mods.add(nf.ModifierPattern(POS, Flags.STATIC, true));
		
		List<FormalPattern> formals = new LinkedList<FormalPattern>();
		formals.add(nf.TypeFormalPattern(POS, nf.TPEArray(POS, nf.TPERefTypePat(POS, nf.RTPName(POS, nf.SimpleNamePattern(POS, "String"))), 1)));
		return nf.PCExecution(
				POS,
				nf.MethodPattern(
						POS,
						mods,
						nf.TPEUniversal(POS),
						nf.ClassTypeDotId(POS, nf.CPEUniversal(POS), nf.SimpleNamePattern(POS, "main")),
						formals,
						Collections.emptyList()
				)
		);
	}

	private static SymbolKind createKind(TMNodeFactory nf) {
		return nf.BeforeSymbol(POS);
	}

}
