/* abc - The AspectBench Compiler
 * Copyright (C) 2007 Reehan Shaikh
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
package abc.ra;

import java.util.Collection;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.ra.weaving.weaver.RAWeaver;
import abc.weaving.weaver.Weaver;

/**
 * Abc extension for relational aspects.
 * 
 * @author Eric Bodden
 */
public class AbcExtension extends abc.tm.AbcExtension {

	/**
	 * {@inheritDoc}
	 */
	protected void collectVersions(StringBuffer versions) {
		// first delegate up
		super.collectVersions(versions);
		// then add our two cents
		versions.append(" with relational aspects extension "
				+ new Version().toString() + 											 
				"\n"); 
	}

	/**
	 * {@inheritDoc}
	 */
	public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
			Collection aspect_sources) {
		return new abc.ra.ExtensionInfo(jar_classes, aspect_sources);
	}

	/**
	 * Adds <i>relational</i> keyword.
	 */
	public void initLexerKeywords(AbcLexer lexer) {
		// add all keywords for tm first
		super.initLexerKeywords(lexer);
		// then add ours
		lexer.addGlobalKeyword("relational", new LexerAction_c(new Integer(
				abc.ra.parse.sym.RELATIONAL)));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Weaver createWeaver() {
		return new RAWeaver();
	}
}
