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
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AdviceDecl;
import abc.aspectj.ast.AdviceSpec;
import abc.aspectj.ast.AspectBody;
import abc.aspectj.ast.Pointcut;
import abc.tm.ast.SymbolDecl;
import abc.tm.ast.SymbolKind;
import abc.tm.ast.TMNodeFactory;

/**
 * Node factory for relational aspects extension.
 * @author Eric Bodden
 */
public interface RANodeFactory extends TMNodeFactory {      
	
	/**
	 * Returns a new relational aspect declaration.
	 * @param formals relational aspect formals
	 * @see AJNodeFactory#AspectDecl(Position, boolean, Flags, String, TypeNode, List, abc.aspectj.ast.PerClause, AspectBody)
	 */
	public RelAspectDecl RelAspectDecl(Position pos, boolean is_privileged,	Flags flags, String name, TypeNode superClass, List interfaces,	List formals, AspectBody body);
	
	/**
	 * Returns an associate-symbol declaration.
	 * @param pos position in code
	 * @param name name of the symbol, should most often be <i>associate</i>
	 * @param tracematch_name name of surrounding tracematch 
	 * @param bindAspectInstanceInReturn is true, the newly created advice instance is boudn via after-returning
	 * @param container declaration of containing relational aspect
	 */
	public SymbolDecl AssociateSymbolDecl(Position pos, String name, String tracematch_name, boolean bindAspectInstanceInReturn, RelAspectDecl container);

	/**
	 * Returns a release-symbol declaration.
	 * @param pos position in code
	 * @param name name of the symbol, should most often be <i>release</i>
	 * @param tracematch_name name of surrounding tracematch 
	 * @param container declaration of containing relational aspect
	 */
	public SymbolDecl ReleaseSymbolDecl(Position pos, String name, String tracematch_name, RelAspectDecl container);

	/**
	 * Returns a symbol representing program start. It is established via <code>before execution(* *.main(String[]))</code>.
	 * @param pos position in the code
	 * @param name name of the symbol
	 */
	public SymbolDecl StartSymbolDecl(Position pos, String name);

	/**
	 * Returns an advice declaration which issues custom warnings in case it never matches. 
	 * @param warningType see {@link CustomWarningPerSymbolAdviceDecl}
	 * @see AJNodeFactory#AdviceDecl(Position, Flags, AdviceSpec, List, Pointcut, Block)
	 */
	public AdviceDecl CustomWarningPerSymbolAdviceDecl(Position position,
			Flags none, AdviceSpec spec, List tlist, Pointcut pc, Block body,
			String tm_id, SymbolDecl sym, Position tm_pos, int warningType);
	
	/**
	 * Generates a special {@link SymbolDecl} that gives the warning that the <i>advice</i> never matches, in case
	 * the synbol never matches.
	 * @param if <code>true</code>. the warning is given, if <code>false</code> it is not
	 * @see TMNodeFactory#SymbolDecl(Position, String, SymbolKind, Pointcut)
	 */
	public SymbolDecl AdviceSymbolDeclaration(Position pos, String name, SymbolKind kind, Pointcut pc, boolean giveWarning);
	
	/**
	 * Creates a relational around advice. In addition to its normal formal parameters, it can have a list
	 * of proceed-variables in the header.
	 */
	public RelationalAround RelationalAround(Position pos, TypeNode returnType, List formals, List proceedIdentifiers);

}
