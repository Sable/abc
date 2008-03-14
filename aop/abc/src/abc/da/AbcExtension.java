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

package abc.da;

import java.util.Collection;
import java.util.List;

import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceFlowInsensitiveAnalysis;
import abc.da.weaving.weaver.depadviceopt.DependentAdviceQuickCheck;
import abc.da.weaving.weaver.depadviceopt.ds.WeavableMethods;
import abc.main.options.OptionsParser;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * Abc extension for dependent advice. Exposes a quick check anda flow-insensitive analysis to resolve advice
 * dependencies at compile time.
 * @author Eric Bodden
 */
public class AbcExtension extends abc.eaj.AbcExtension implements HasDAInfo
{
    protected static final ID DEPENDENT_ADVICE_QUICK_CHECK = new ReweavingPass.ID("quick-check for dependent-advice");
	protected static final ID DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS = new ReweavingPass.ID("flow-insensitive analysis for dependent-advice");
    protected static final ID AFTER_ANALYSIS_CLEANUP = new ID("cleanup stage");

	/** The dependent advice info for this extension. This encapsulates all information about advice dependencies in the backend. */
	protected DAInfo daInfo;
	
	/** The quick check for dependent advice. */
	protected DependentAdviceQuickCheck quickCheck;

	/** The flow-insensitive analysis for dependent advice. */
	protected DependentAdviceFlowInsensitiveAnalysis flowInsensitiveAnalysis;

	public AbcExtension() {
		if(!OptionsParser.v().laststage().equals("quick")) {
			//enable whole-program mode if we have anoy other stage but "quick"
			OptionsParser.v().set_w(true);
		}
	}
	
	/**
	 * @inheritDoc
	 */
	protected void collectVersions(StringBuffer versions)
    {
        super.collectVersions(versions);
        versions.append(" with DA " +
                        new abc.da.Version().toString() +
                        "\n");
    }

    /**
     * @inheritDoc
     */
    public ExtensionInfo makeExtensionInfo(Collection<String> jar_classes, Collection<String> aspect_sources) {
        return new ExtensionInfo(jar_classes, aspect_sources);
    }

    /**
     * @inheritDoc
     */
    public void initLexerKeywords(AbcLexer lexer) {
        // Add the base keywords
        super.initLexerKeywords(lexer);

        lexer.addAspectJKeyword("dependency", new LexerAction_c(abc.da.parse.sym.DEPENDENCY));
        lexer.addAspectJKeyword("dependent", new LexerAction_c(abc.da.parse.sym.DEPENDENT));
        lexer.addAspectJKeyword("strong", new LexerAction_c(abc.da.parse.sym.STRONG,lexer.pointcut_state()));
        lexer.addAspectJKeyword("weak", new LexerAction_c(abc.da.parse.sym.WEAK,lexer.pointcut_state()));
    }
    
    /**
     * Adds reweaving passes for the quick check, the flow-insensitive analysis (if enabled)
     * and for cleanup.
     */
    public void createReweavingPasses(List<ReweavingPass> passes) {
    	super.createReweavingPasses(passes);
    	
    	//quick check
   		passes.add(new ReweavingPass(DEPENDENT_ADVICE_QUICK_CHECK,quickCheck()));
   		
   		//flow-insensitive analysis, if enabled
    	if(!OptionsParser.v().laststage().equals("quick")) {
    		passes.add(new ReweavingPass(DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS,flowInsensitiveAnalysis()));
    	}
    	
        //add a pass which just cleans up resources;
        //this is necessary in order to reset static fields for the test harness        
        ReweavingAnalysis cleanup = new AbstractReweavingAnalysis() {

            @Override
            public boolean analyze() {
            	//do nothing
                return false;
            }
            
            @Override
            public void cleanup() {
                //reset state
                resetAnalysisDataStructures();
            }

        };
        passes.add( new ReweavingPass( AFTER_ANALYSIS_CLEANUP , cleanup ) );
    }
    
	/**
	 * Creates the unique instance of the quick check. Extensions may override this method in order
	 * to instantiate their own version of a quick check instead.
	 */
	protected DependentAdviceQuickCheck createQuickCheck() {
		return new DependentAdviceQuickCheck();
	}

	/**
	 * Creates the unique instance of the flow-insensitive analysis. Extensions may override this method in order
	 * to instantiate their own version of the analysis instead.
	 */
	protected DependentAdviceFlowInsensitiveAnalysis createFlowInsensitiveAnalysis() {
		return new DependentAdviceFlowInsensitiveAnalysis();
	}
	
	/**
	 * Creates the unique instance of the dependent advice info. Extensions may override this method in order
	 * to instantiate their own version of this class instead.
	 */
	protected DAInfo createDependentAdviceInfo() {
		return new DAInfo();
	}
	
    /**
     * @inheritDoc
     */
    public DAInfo getDependentAdviceInfo()
    {
        if (daInfo == null)
        	daInfo = createDependentAdviceInfo();
        return daInfo;
    }
    

	/**
	 * @inheritDoc
	 */
	public DependentAdviceQuickCheck quickCheck() {
		if(quickCheck==null)
			quickCheck = createQuickCheck();
		return quickCheck;
	}
	
	/**
	 * @inheritDoc
	 */
	public DependentAdviceFlowInsensitiveAnalysis flowInsensitiveAnalysis() {
		if(flowInsensitiveAnalysis==null)
			flowInsensitiveAnalysis = createFlowInsensitiveAnalysis();
		return flowInsensitiveAnalysis;
	}

	/**
	 * Resets all static data structures used for static tracematch optimizations.
	 */
	public void resetAnalysisDataStructures() {
        WeavableMethods.reset();
	}
	
}
