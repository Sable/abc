/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Neil Ongkingco
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
 * Created on May 13, 2005
 *
 */

package abc.om;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import polyglot.util.Position;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.om.parse.OMAbcLexer;
import abc.om.parse.sym;
import abc.om.visit.ModuleStructure;
import abc.om.weaving.aspectinfo.OMGlobalAspectInfo;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.matching.AdviceApplication.ResidueConjunct;
import abc.weaving.residues.Residue;
import polyglot.types.SemanticException;
import soot.*;
import abc.weaving.matching.*;

/**
 * @author Neil Ongkingco
 *  
 */
public class AbcExtension extends abc.eaj.AbcExtension {
    //debug
    public static final boolean debug = false;
    private static boolean isLoaded = false;

    public static Position generated = new Position("openmod_generated:0");
    
    public AbcExtension() { 
        super();
        isLoaded = true;
    }
    
    public static boolean isLoaded() {
        return isLoaded;
    }

    public abc.aspectj.ExtensionInfo makeExtensionInfo(Collection jar_classes,
            Collection aspect_sources) {
        return new abc.om.ExtensionInfo(jar_classes, aspect_sources,this);
    }

    protected GlobalAspectInfo createGlobalAspectInfo() {
        return new OMGlobalAspectInfo();
    }
    
    public void initLexerKeywords(AbcLexer lexer) {
        // Add the base keywords
        super.initLexerKeywords(lexer);
        OMAbcLexer omLexer = (OMAbcLexer) lexer;

        omLexer.addJavaKeyword("root", new LexerAction_c(
                new Integer(sym.ROOT), new Integer(omLexer.java_state())));        
        omLexer.addJavaKeyword("module", new LexerAction_c(
                new Integer(sym.MODULE), new Integer(omLexer.module_state())));
        omLexer.addModuleKeyword("module", new LexerAction_c(
                new Integer(sym.MODULE), null));
        omLexer.addModuleKeyword("open", new LexerAction_c(new Integer(
                sym.OPEN), null));
        omLexer.addModuleKeyword("constrain", 
                new LexerAction_c(new Integer(sym.CONSTRAIN), 
                null));
        omLexer.addModuleKeyword("expose", new LexerAction_c(new Integer(
                sym.EXPOSE), new Integer(omLexer.pointcut_state())));
        omLexer.addModuleKeyword("friend", new LexerAction_c(new Integer(
                sym.FRIEND), null));
        omLexer.addModuleKeyword("advertise", new LexerAction_c(new Integer(
                sym.ADVERTISE), new Integer(omLexer.pointcut_state())));
        //TODO: try to fix this so that the transition to pointcut happens on "to"
        omLexer.addPointcutKeyword("to", new LexerAction_c(new Integer(
                sym.TO), null));
        
        //open class tokens
        omLexer.addModuleKeyword("openclass", new LexerAction_c(new Integer(
                sym.OPENCLASS), null));
        omLexer.addModuleKeyword("field", new LexerAction_c(new Integer(
                sym.FIELD), null));        
        omLexer.addModuleKeyword("method", new LexerAction_c(new Integer(
                sym.METHOD), null));
        omLexer.addModuleKeyword("parent", new LexerAction_c(new Integer(
                sym.PARENT), null));
        
        //overrride the class keyword
        omLexer.addGlobalKeyword("class", 
                new LexerAction_c(new Integer(sym.CLASS)) {
            
	            public int getToken(AbcLexer lexer) {
	                OMAbcLexer omLexer = (OMAbcLexer) lexer;
	                if (!omLexer.getLastTokenWasDot()) {
	                    int nextState;
	                    if (omLexer.currentState() == omLexer.aspectj_state()) {
	                        nextState = omLexer.aspectj_state();
	                    } else if (omLexer.currentState() == omLexer.module_state()) {
	                    	nextState = omLexer.pointcut_state();
	                    } else {
	                    	nextState = omLexer.java_state();
	                    }
	                    omLexer.enterLexerState(nextState);
	                }
	                return token.intValue();
	            }
        	}
        );

    }
    
    
    
    public List residueConjuncts(final AbstractAdviceDecl ad, 
            final Pointcut pc,
            final ShadowMatch sm, 
            final SootMethod method, 
            final SootClass cls, 
            final WeavingEnv we) {
        //complete rewrite, so that warnings will still be generated by
        //openModMatchesAt()
        List result = new ArrayList();
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
        	             	return ad.preResidue(sm);
        	             }});
        //replace matchesAt with openModMatchesAt
        result.add(new ResidueConjunct() {
        	             public Residue run() throws SemanticException {
							return ModuleStructure.v().openModMatchesAt(
							        ad.getPointcut(),
							        sm,
							        ad.getAspect(),
							        we,
							        cls,
							        method,
							        ad);
        	             }
                        });

       result.add(new ResidueConjunct() {
                        public Residue run() throws SemanticException {
                        	return ad.getAdviceSpec().matchesAt(we,sm,ad);
                        }
                       });
        result.add(new ResidueConjunct() {
        		        public Residue run() throws SemanticException {
        		        	return ad.postResidue(sm);
        		        }
                       });
        return result;
    }
    
    
    public static void debPrintln(String str) {
        if (debug) {
            System.out.println(str);
        }
    }

    public static void debPrint(String str) {
        if (debug) {
            System.out.print(str);
        }
    }
}
