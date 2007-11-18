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

import abc.aspectj.ast.AdviceDecl;

/**
 * A special {@link AdviceDecl} which generates customized warnings, based on the waring type.
 *
 * @author Eric Bodden
 */
public interface CustomWarningPerSymbolAdviceDecl extends AdviceDecl {

	/** Warns that the symbol never matches (default)*/
	public static final int SYMBOL = 0;
	/** Warns that the advice never matches */
	public static final int ADVICE = 1;
	/** Warns that the relational aspect is never associated. */
	public static final int REL_ASPECT = 2;
	/** Does not warn at all */
	public static final int NONE = 3;

}
