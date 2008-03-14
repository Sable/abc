/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Julian Tibble
 * Copyright (C) 2004 Pavel Avgustinov
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

package abc.eaj;

import java.util.Collection;
import java.util.List;

import polyglot.util.ErrorInfo;

import soot.Scene;
import soot.SootClass;
import soot.tagkit.Host;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.eaj.weaving.matching.ArrayGetShadowMatch;
import abc.eaj.weaving.matching.ArraySetShadowMatch;
import abc.eaj.weaving.matching.CastShadowMatch;
import abc.eaj.weaving.matching.ExtendedSJPInfo;
import abc.eaj.weaving.matching.LockShadowMatch;
import abc.eaj.weaving.matching.ThrowShadowMatch;
import abc.eaj.weaving.matching.UnlockShadowMatch;
import abc.eaj.weaving.weaver.SyncWarningWeaver;
import abc.eaj.weaving.weaver.SynchronizedMethodRestructurer;
import abc.eaj.weaving.weaver.maybeshared.TLOAnalysisManager;
import abc.main.Debug;
import abc.weaving.matching.SJPInfo;
import abc.weaving.matching.ShadowType;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * @author Julian Tibble
 * @author Pavel Avgustinov
 * @author Eric Bodden
 */
public class AbcExtension extends abc.main.AbcExtension
{
    protected static final ID THREAD_LOCAL_OBJECTS_ANALYSIS = new ReweavingPass.ID("thread-local obejcts analysis");
    
    protected boolean lexerSawMaybeSharedPointcut = false;

	protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with EAJ " +
                        new abc.eaj.Version().toString() +
                        "\n");
    }

    public abc.aspectj.ExtensionInfo
            makeExtensionInfo(Collection<String> jar_classes,
                              Collection<String> aspect_sources)
    {
        return new abc.eaj.ExtensionInfo(jar_classes, aspect_sources);
    }

    protected List<ShadowType> listShadowTypes()
    {
        List<ShadowType> shadowTypes = super.listShadowTypes();

        shadowTypes.add(CastShadowMatch.shadowType());
        shadowTypes.add(ThrowShadowMatch.shadowType());
        shadowTypes.add(ArrayGetShadowMatch.shadowType());
        shadowTypes.add(ArraySetShadowMatch.shadowType());
        if(Debug.v().enableLockPointcuts) {
	        shadowTypes.add(LockShadowMatch.shadowType());
	        shadowTypes.add(UnlockShadowMatch.shadowType());
        }
        return shadowTypes;
    }

    public abc.weaving.weaver.AdviceInliner makeAdviceInliner()
    {
	return new abc.eaj.weaving.weaver.AdviceInliner();
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

    protected Weaver createWeaver() {
    	//hook up extended weaver that warns about
    	//restructured methods
    	return new SyncWarningWeaver();
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
        if(!Debug.v().noGlobalPointcut)
        	lexer.addGlobalKeyword("global", new LexerAction_c(new Integer(abc.eaj.parse.sym.GLOBAL),
                            new Integer(lexer.pointcut_state())));

        // keyword for the "cflowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowdepth", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CFLOWDEPTH)));
        
        // keyword for the "cflowbelowdepth" pointcut extension
        lexer.addPointcutKeyword("cflowbelowdepth", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CFLOWBELOWDEPTH)));

        // keyword for the "let" pointcut extension
        lexer.addPointcutKeyword("let", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_LET),
                new Integer(lexer.pointcutifexpr_state())));
        
        // keywords for the "monitorenter/monitorexit" pointcut extension
        if(Debug.v().enableLockPointcuts) {
	        lexer.addPointcutKeyword("lock", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_LOCK)));
	        lexer.addPointcutKeyword("unlock", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_UNLOCK)));
        }

    	lexer.addPointcutKeyword("maybeShared", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_MAYBE_SHARED)) {
    		public int getToken(AbcLexer lexer) {
    			if(!Debug.v().optimizeMaybeSharedPointcut && !lexerSawMaybeSharedPointcut) {
    				reportError(ErrorInfo.WARNING,
    						"Pointcut maybeShared() was used but optimization of this pointcut is disabled! " +
    						"Enable via option '-debug optimizeMaybeSharedPointcut'.", null);
    			}
    			lexerSawMaybeSharedPointcut = true;
    			return super.getToken(lexer);
    		}
    	});

        if(!Debug.v().noContainsPointcut) {
        	//keyword for the "contains" pointcut extension
        	lexer.addPointcutKeyword("contains", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_CONTAINS)));
        }
        
        // Array set/get pointcut keywords
        lexer.addPointcutKeyword("arrayget", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_ARRAYGET)));
        lexer.addPointcutKeyword("arrayset", new LexerAction_c(new Integer(abc.eaj.parse.sym.PC_ARRAYSET)));
    }
    
    public void doMethodRestructuring() {
    	if(Debug.v().enableLockPointcuts) {
    	    //restructuring of synchronized methods for lock/unlock pointcuts;
    	    //currently generates synchronized blocks which dava cannot deal with
    		new SynchronizedMethodRestructurer().apply();
    	}
    	super.doMethodRestructuring();
    }
    
    protected void createReweavingPasses(List<ReweavingPass> passes) {
    	super.createReweavingPasses(passes);
    	
    	if(Debug.v().optimizeMaybeSharedPointcut) {	    	
	   		passes.add(new ReweavingPass(THREAD_LOCAL_OBJECTS_ANALYSIS,TLOAnalysisManager.v()));	   		
    	}
    }
}
