/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
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

package abc.tm;

import abc.aspectj.parse.*;

import soot.Scene;
import soot.SootClass;

import java.util.*;

/*
 * @author Chris Allan
 *
 */
public class AbcExtension extends abc.main.AbcExtension
{
    protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with TraceMatching " +
                        new abc.tm.Version().toString() +
                        "\n");
    }

    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection jar_classes,
                              Collection aspect_sources)
    {
        return new abc.tm.ExtensionInfo(jar_classes, aspect_sources);
    }
    
    public void initLexerKeywords(AbcLexer lexer)
    {
        // Add the base keywords
        super.initLexerKeywords(lexer);
		
        // keyword for the "cast" pointcut extension
        lexer.addAspectJKeyword("tracematch", new LexerAction_c(
                            new Integer(abc.tm.parse.sym.TRACEMATCH)));
        lexer.addAspectJKeyword("sym", new LexerAction_c(
                            new Integer(abc.tm.parse.sym.SYM)));
        lexer.addAspectJKeyword("perthread", new LexerAction_c(
                            new Integer(abc.tm.parse.sym.PERTHREAD)));
    }
}
