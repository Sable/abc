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

package abc.eaj;

import abc.eaj.weaving.matching.*;
import abc.weaving.matching.SJPInfo;

import abc.aspectj.parse.*;

import soot.Scene;
import soot.SootClass;
import soot.tagkit.Host;

import java.util.*;

/*
 * @author Julian Tibble
 *
 */
public class AbcExtension extends abc.main.AbcExtension
{
    protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with EAJ " +
                        new abc.eaj.Version().toString() +
                        "\n");
    }

    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection jar_classes,
                              Collection aspect_sources)
    {
        return new abc.eaj.ExtensionInfo(jar_classes, aspect_sources);
    }

    protected List/*<ShadowType>*/ listShadowTypes()
    {
        List/*<ShadowType*/ shadowTypes = super.listShadowTypes();

        shadowTypes.add(CastShadowMatch.shadowType());
        shadowTypes.add(ThrowShadowMatch.shadowType());

        return shadowTypes;
    }

    public void addBasicClassesToSoot()
    {
        super.addBasicClassesToSoot();

        Scene.v().addBasicClass("org.aspectbench.eaj.runtime.reflect.EajFactory",
                                SootClass.SIGNATURES);
    }

    public String runtimeSJPFactoryClass() {
        return "org.aspectbench.eaj.runtime.reflect.EajFactory";
    }

    /**
	 * Create a (compile-time) static join point information object that
         * generates code to initialize static join point fields with
         * reflective information about a join point.
	 */
    public SJPInfo createSJPInfo(String kind, String signatureTypeClass,
            String signatureType, String signature, Host host) {
        return new ExtendedSJPInfo(kind, signatureTypeClass, signatureType,
                signature, host);
    }


    /* (non-Javadoc)
     * @see abc.main.AbcExtension#initLexerKeywords(abc.aspectj.parse.AbcLexer)
     */
    public void initLexerKeywords(AbcLexer lexer) {
                // Add the base keywords
                super.initLexerKeywords(lexer);

        // keyword for the "cast" pointcut extension
        lexer.addPointcutKeyword("cast", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CAST)));

        // keyword for the "throw" pointcut extension
        lexer.addPointcutKeyword("throw", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_THROW)));

        // keyword for the "global pointcut" extension
        lexer.addGlobalKeyword("global", new LexerAction_c(new Integer(abc.eaj.parse.sym.GLOBAL),
                            new Integer(lexer.pointcut_state())));

        // keyword for the "cflowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowdepth", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CFLOWDEPTH)));
        
        // keyword for the "cflowbelowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowbelowdepth", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CFLOWBELOWDEPTH)));

        // keyword for the "let" pointcut extension
        lexer.addPointcutKeyword("let", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_LET)));
    }
}
