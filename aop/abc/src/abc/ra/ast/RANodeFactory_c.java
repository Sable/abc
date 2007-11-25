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

import java.util.List;

import polyglot.ast.Block;
import polyglot.ast.TypeNode;
import polyglot.types.Flags;
import polyglot.util.Position;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.AspectBody;
import abc.aspectj.ast.PerClause;
import abc.aspectj.ast.Pointcut;
import abc.ra.ExtensionInfo;
import abc.tm.ast.Regex;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMDecl;
import abc.tm.ast.TMModsAndType;
import abc.tm.ast.TMNodeFactory_c;

/**
 * Node factory for relational aspects extension.
 * @author Eric Bodden
 */
public class RANodeFactory_c extends TMNodeFactory_c implements RANodeFactory {

	/**
	 * {@inheritDoc}
	 */
	public RelAspectDecl RelAspectDecl(Position pos,
			boolean is_privileged, Flags flags, String name,
			TypeNode superClass, List interfaces, List formals,
			AspectBody body) {
		//relational aspects always have "singleton" semantics w.r.t. per-clauses
		PerClause per = IsSingleton(pos);
		return new RelAspectDecl_c(pos,is_privileged,flags,name,superClass,interfaces,per,formals,body);
	}

	/**
	 * Returns a {@link RelAdviceDecl} if the <code>relational</code> modifier is set or an {@link AdviceDecl} otherwise.
	 */
	public AdviceDecl AdviceDecl(Position pos, Flags flags,	AdviceSpec spec, List throwTypes, Pointcut pc,Block body) {
		if(flags.contains(ExtensionInfo.RELATIONAL_MODIFIER)) {
			return new RelAdviceDecl_c(pos,flags,spec,throwTypes,pc, body);
		} else {
			return new AdviceDecl_c(pos, flags, spec, throwTypes, pc, body);
		}
	}
	
	/**
	 * Returns a {@link RelTMDecl} if the <code>relational</code> modifier is set or a {@link TMDecl} otherwise.
	 */
	public TMDecl TMDecl(Position pos, Position body_pos,
			TMModsAndType mods_and_type, String tracematch_name, List formals,
			List throwTypes, List symbols, List freqent_symbols, Regex regex,
			Block body) {
		if(mods_and_type.getFlags().contains(ExtensionInfo.RELATIONAL_MODIFIER)) {
			return new RelTMDecl_c(pos, body_pos, mods_and_type, tracematch_name, formals,
					throwTypes, symbols, freqent_symbols, regex, body);
		} else {
			return super.TMDecl(pos, body_pos, mods_and_type, tracematch_name, formals,
					throwTypes, symbols, freqent_symbols, regex, body);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SymbolDecl AssociateSymbolDecl(Position pos, String name,
			String tracematch_name, boolean bindAspectInstanceInReturn, RelAspectDecl container) {
		return new AssociateSymbolDecl_c(pos,name,tracematch_name,bindAspectInstanceInReturn,container, this);
	}

	/**
	 * {@inheritDoc}
	 */
	public SymbolDecl ReleaseSymbolDecl(Position pos, String name,
			String tracematch_name, RelAspectDecl container) {
		return new ReleaseSymbolDecl_c(pos,name,tracematch_name,container,this);
	}

	/** 
	 * {@inheritDoc}
	 */
	public SymbolDecl StartSymbolDecl(Position pos, String name) {
		return new StartSymbolDecl_c(pos,name,this);
	}

	/**
	 * {@inheritDoc}
	 */
	public AdviceDecl CustomWarningPerSymbolAdviceDecl(
			Position position, Flags flags, AdviceSpec spec, List tlist,
			Pointcut pc, Block body, String tm_id, SymbolDecl sym,
			Position tm_pos, int warningType) {
        return new CustomWarningPerSymbolAdviceDecl_c(position, flags, spec, tlist,
                pc, body, tm_id, sym, tm_pos, warningType);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SymbolDecl AdviceSymbolDeclaration(Position pos, String name, SymbolKind kind,
			Pointcut pc, boolean giveWarning) {
		return new AdviceSymbolDeclaration_c(pos,name,kind,pc,giveWarning);
	}

	/**
	 * {@inheritDoc}
	 */
	public RelationalAround RelationalAround(Position pos,
			TypeNode returnType, List formals, List proceedIdentifiers) {
		return new RelationalAround_c(pos,returnType,formals,proceedIdentifiers);
	}

}
