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

import soot.Scene;
import soot.SootClass;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.da.weaving.aspectinfo.DAInfo;
import abc.da.weaving.weaver.dynainstr.DynamicInstrumenter;
import abc.da.weaving.weaver.tracing.CrossChecker;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * Abc extension for dependent advice. Exposes a quick check and a flow-insensitive analysis to resolve advice
 * dependencies at compile time.
 * @author Eric Bodden
 */
public class AbcExtension extends abc.eaj.AbcExtension implements HasDAInfo
{
    public static final ID DEPENDENT_ADVICE_QUICK_CHECK = new ReweavingPass.ID("quick-check for dependent-advice");
	public static final ID DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS = new ReweavingPass.ID("flow-insensitive analysis for dependent-advice");
    public static final ID DEPENDENT_ADVICE_INTRA_FLOWSENS = new ReweavingPass.ID("flow-sensitive intraprocedural analysis for dependent-advice");
    public static final ID DEPENDENT_ADVICE_DYNAMIC_INSTRUMENTATION = new ReweavingPass.ID("dynamic instrumentation for dependent-advice");
    public static final ID CROSS_CHECK = new ReweavingPass.ID("cross-check");
    public static final ID AFTER_ANALYSIS_CLEANUP = new ID("cleanup stage");

	/** The dependent advice info for this extension. This encapsulates all information about advice dependencies in the backend. */
	protected DAInfo daInfo;
	
	public AbcExtension() {
		if(!OptionsParser.v().laststage().equals("quick")) {
			//enable whole-program mode if we have any other stage but "quick"
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
    	
    	if(OptionsParser.v().runtime_trace()!=null) {
    		//do only cross-check
    		passes.add(new ReweavingPass(CROSS_CHECK,new AbstractReweavingAnalysis() {

				public boolean analyze() {
		    		CrossChecker.crossCheck();
					return false;
				}
    			
    		}));
    		return;
    	}
	    
    	//quick check
    	if(!OptionsParser.v().laststage().equals("none")) {
    		passes.add(new ReweavingPass(DEPENDENT_ADVICE_QUICK_CHECK,getDependentAdviceInfo().quickCheck()));
    	}
   		
   		//flow-insensitive analysis, if enabled
    	if(!OptionsParser.v().laststage().equals("none")
    	&& !OptionsParser.v().laststage().equals("quick")) {
    		passes.add(new ReweavingPass(DEPENDENT_ADVICE_FLOW_INSENSITIVE_ANALYSIS,getDependentAdviceInfo().flowInsensitiveAnalysis()));
    	}
    	
		if(!OptionsParser.v().laststage().equals("none")
		&& !OptionsParser.v().laststage().equals("quick")
		&& !OptionsParser.v().laststage().equals("flowins")) {
			passes.add(new ReweavingPass(DEPENDENT_ADVICE_INTRA_FLOWSENS,getDependentAdviceInfo().intraProceduralAnalysis()));			
		}
		
        if(Debug.v().dynaInstr) {
            ReweavingAnalysis dynaInstr = new AbstractReweavingAnalysis() {
                @Override
                public boolean analyze() {
                    DynamicInstrumenter.v().createClassesAndSetDynamicResidues();
                    return false;
                }
            };
            passes.add( new ReweavingPass( DEPENDENT_ADVICE_DYNAMIC_INSTRUMENTATION , dynaInstr ) );
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
                getDependentAdviceInfo().resetAnalysisDataStructures();
            }

        };
        
        if(!OptionsParser.v().laststage().equals("none")) {
        	passes.add( new ReweavingPass( AFTER_ANALYSIS_CLEANUP , cleanup ) );
        }
    }
    
	/**
	 * Creates the unique instance of the dependent advice info. Extensions may override this method in order
	 * to instantiate their own version of this class instead.
	 */
	protected DAInfo createDependentAdviceInfo() {
		return new DAInfo();
	}
	
	@Override
	public void addBasicClassesToSoot() {
		super.addBasicClassesToSoot();
		
        if(Debug.v().dynaInstr || Debug.v().shadowCount) {
        	Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.IShadowSwitchInitializer", SootClass.SIGNATURES);
        	Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.ShadowSwitch", SootClass.SIGNATURES);
        }
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
    
}
