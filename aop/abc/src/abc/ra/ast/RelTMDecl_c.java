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

import java.util.Iterator;
import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.Formal;
import polyglot.ast.Node;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.Position;
import polyglot.visit.TypeChecker;
import abc.ra.types.RelAspectType;
import abc.tm.ast.Regex;
import abc.tm.ast.TMDecl;
import abc.tm.ast.TMDecl_c;
import abc.tm.ast.TMModsAndType;

/**
 * Declaration of a relational tracematch.
 * Such a tracematch has access to relational aspect formals.
 *
 * @author Eric Bodden
 */
public class RelTMDecl_c extends TMDecl_c implements RelTMDecl {

	final TMModsAndType mods_and_type;

	public RelTMDecl_c(Position pos, Position body_pos,
			TMModsAndType mods_and_type, String tracematch_name, List formals,
			List throwTypes, List symbols, List frequent_symbols, Regex regex,
			Block body) {
		super(pos, body_pos, mods_and_type, tracematch_name, formals, throwTypes,
				symbols, frequent_symbols, regex, body);
		this.mods_and_type = mods_and_type;
	}

	/**
	 * {@inheritDoc}
	 */
	public TMDecl genNormalTraceMatch(RelAspectDecl container, RANodeFactory nf, TypeSystem ts) {
		return new TMFromRelTMDecl_c(
				position,
				mods_and_type,
				tracematch_name,
				formals,
				throwTypes,
				symbols,
				frequent_symbols,
				regex,
				body,
				container,
				this,
				nf);
	}

	
	/**
	 * Typechecks this relational advice.
	 * The tracematch may only be contained in a relational aspect.
	 * Also, it may not declare any tracematch formals that have the same name as any
	 * of the relational aspect formals.
	 */
	public Node typeCheck(TypeChecker tc) throws SemanticException {
		RelAspectType at = (RelAspectType) tc.context().currentClass();
		if (!at.relational()) {
			throw new SemanticException(
					"Only relational aspects can contain relational tracematches.",
					position());
		} else {

			for (Iterator adviceFormalIter = formals.iterator(); adviceFormalIter.hasNext();) {
				Formal adviceFormal = (Formal) adviceFormalIter.next();
				for (Iterator aspectFormalIter = at.relationalAspectFormals().iterator();
					aspectFormalIter.hasNext();) {
					Formal aspectFormal = (Formal) aspectFormalIter.next();
					if(adviceFormal.name().equals(aspectFormal.name())) {
						throw new SemanticException(
								"Name of tracematch formal "+adviceFormal.name()+" clashes with relational aspect " +
								"formal of same name.",
								adviceFormal.position());
					}
				}
			}			
		}
		
		return this;
	}
	
}
