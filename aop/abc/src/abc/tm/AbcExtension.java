/* abc - The AspectBench Compiler
 * Copyright (C) 2005 Julian Tibble
 * Copyright (C) 2005 Oege de Moor
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

package abc.tm;

import java.util.Collection;
import java.util.List;

import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import soot.Scene;
import soot.SootClass;
import abc.aspectj.parse.AbcLexer;
import abc.aspectj.parse.LexerAction_c;
import abc.main.CompileSequence;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.tm.weaving.aspectinfo.TMAdviceDecl;
import abc.tm.weaving.aspectinfo.TMGlobalAspectInfo;
import abc.tm.weaving.weaver.TMLoopExitRestructurer;
import abc.tm.weaving.weaver.TMWeaver;
import abc.tm.weaving.weaver.tmanalysis.OptFlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.OptQuickCheck;
import abc.tm.weaving.weaver.tmanalysis.dynainst.DynamicInstrumenter;
import abc.tm.weaving.weaver.tmanalysis.query.ReachableShadowFinder;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowGroupRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.WeavableMethods;
import abc.tm.weaving.weaver.tmanalysis.stages.CallGraphAbstraction;
import abc.tm.weaving.weaver.tmanalysis.stages.FlowInsensitiveAnalysis;
import abc.tm.weaving.weaver.tmanalysis.stages.QuickCheck;
import abc.tm.weaving.weaver.tmanalysis.stages.TMShadowTagger;
import abc.tm.weaving.weaver.tmanalysis.util.SymbolShadow;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.CflowSetup;
import abc.weaving.aspectinfo.DeclareMessage;
import abc.weaving.aspectinfo.DeclareSoft;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.AbstractReweavingAnalysis;
import abc.weaving.weaver.ReweavingAnalysis;
import abc.weaving.weaver.ReweavingPass;
import abc.weaving.weaver.Weaver;
import abc.weaving.weaver.ReweavingPass.ID;

/**
 * @author Julian Tibble
 * @author Oege de Moor
 * @author Eric Bodden
 */
public class AbcExtension extends abc.eaj.AbcExtension
{

    private static final ID PASS_TM_ANALYSIS_QUICK_CHECK = new ID("Tracematch analysis - quick check");
    private static final ID PASS_TM_ANALYSIS_FLOWINS = new ID("Tracematch analysis - flow-insensitive stage");
    private static final ID PASS_TM_ANALYSIS_INTRAPROC = new ID("Tracematch analysis - intraprocedural stage");
    private static final ID PASS_TM_ANALYSIS_FLOWINS_REITER = new ID("Tracematch analysis - reiteration of flow-insensitive stage");
    private static final ID PASS_TM_ANALYSIS_CLEANUP = new ID("Tracematch analysis - cleanup stage");
    private static final ID PASS_DYNAMIC_INSTRUMENTATION = new ID("Dynamic instrumentation");
    

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
 
    protected GlobalAspectInfo createGlobalAspectInfo()
    {
        return new TMGlobalAspectInfo();
    }

    public Weaver createWeaver()
    {
        return new TMWeaver();
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
        lexer.addAspectJKeyword("frequent", new LexerAction_c(
                            new Integer(abc.tm.parse.sym.FREQUENT)));
        lexer.addAspectJKeyword("filtermatch", new LexerAction_c(
				new Integer(abc.tm.parse.sym.FILTERMATCH)));
        lexer.addAspectJKeyword("skipmatch", new LexerAction_c(
				new Integer(abc.tm.parse.sym.SKIPMATCH)));
    }
    
	public void addBasicClassesToSoot()
	   {
		   super.addBasicClassesToSoot();
           // Need to add all standard library classes used in the codegen (minus some default ones)
		   Scene.v().addBasicClass("java.util.Iterator", SootClass.SIGNATURES);
		   Scene.v().addBasicClass("java.util.LinkedHashSet",
                                    SootClass.SIGNATURES);
		   Scene.v().addBasicClass("java.util.LinkedList", SootClass.SIGNATURES);
           Scene.v().addBasicClass("java.lang.ref.WeakReference", SootClass.SIGNATURES);
           Scene.v().addBasicClass("java.lang.ThreadLocal", SootClass.SIGNATURES);
           Scene.v().addBasicClass("java.util.Set", SootClass.SIGNATURES);
           Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.MyWeakRef", SootClass.SIGNATURES);
           Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.PersistentWeakRef", SootClass.SIGNATURES);
           Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.ClashWeakRef", SootClass.SIGNATURES);
           Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.ClashPersistentWeakRef", SootClass.SIGNATURES);
           Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.Lock", SootClass.SIGNATURES);
           if(abc.main.Debug.v().useCommonsCollections)
        	   Scene.v().addBasicClass("org.apache.commons.collections.map.ReferenceIdentityMap", SootClass.SIGNATURES);
           else {
        	   Scene.v().addBasicClass("java.util.NoSuchElementException", SootClass.SIGNATURES);
        	   Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.IdentityHashMap", SootClass.SIGNATURES);
        	   Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.WeakKeyIdentityHashMap", SootClass.SIGNATURES);
        	   Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.WeakKeyCollectingIdentityHashMap", SootClass.SIGNATURES);
        	   Scene.v().addBasicClass("java.util.Map$Entry", SootClass.SIGNATURES);
           }
           if(Debug.v().dynaInstr) {
               Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.IShadowSwitchInitializer", SootClass.SIGNATURES);
               Scene.v().addBasicClass("org.aspectbench.tm.runtime.internal.ShadowSwitch", SootClass.SIGNATURES);               
           }
	   }
    
    /** 
     * {@inheritDoc}
     */
    protected void createReweavingPasses(List passes) {
        super.createReweavingPasses(passes);
        if(OptionsParser.v().wp_tmopt()) {
            //we need instruction tags so that we can identify shadow IDs after weaving
            OptionsParser.v().set_tag_instructions(true);

            //Quick check
            ReweavingAnalysis quick = new OptQuickCheck();                
            passes.add( new ReweavingPass( PASS_TM_ANALYSIS_QUICK_CHECK, quick ) );
            
            final String laststage = OptionsParser.v().laststage();
            
            if(!laststage.equals("quick")) {
            
                ReweavingAnalysis intra = null;
                if(!laststage.equals("flowins") && Debug.v().firstUnnecessary) {
                    
                    //hook up intra-procedural analysis, if present (first iteration of unnecessary shadows)
                    try {
                        Class optClass = Class.forName("abc.tm.weaving.weaver.tmanalysis.OptIntraProcedural");              
                        intra = (ReweavingAnalysis) optClass.newInstance();
                        passes.add( new ReweavingPass( PASS_TM_ANALYSIS_INTRAPROC , intra ) );
                        System.out.println("Found and installed plug-in for intra-procedural static tracematch optimizations (first run, unnecessary-shadows only).");
                        
                        //need unique advice actuals for this analysis
                        TMShadowTagger.UNIQUE_ADVICE_ACTUALS = true;
                    } catch (ClassNotFoundException e) {
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    };
                }
                
                ReweavingAnalysis flowins = new OptFlowInsensitiveAnalysis();                
                passes.add( new ReweavingPass( PASS_TM_ANALYSIS_FLOWINS , flowins ) );
    
                if(!laststage.equals("flowins")) {
    
                    //hook up intraprocedural analysis, if present
                    try {
                        if(intra==null) {
            				Class optClass = Class.forName("abc.tm.weaving.weaver.tmanalysis.OptIntraProcedural");				
            	            intra = (ReweavingAnalysis) optClass.newInstance();
                        }
        	            passes.add( new ReweavingPass( PASS_TM_ANALYSIS_INTRAPROC , intra ) );
        	            System.out.println("Found and installed plug-in for intra-procedural static tracematch optimizations.");
                        
                        //need unique advice actuals for this analysis
                        TMShadowTagger.UNIQUE_ADVICE_ACTUALS = true;
                    } catch (ClassNotFoundException e) {
        			} catch (InstantiationException e) {
        			} catch (IllegalAccessException e) {
        			};

                    //hook up reiteration of flow-insensitive analysis, if present
                    try {
                        Class optClass = Class.forName("abc.tm.weaving.weaver.tmanalysis.OptReiterationFlowInsensitiveAnalysis");              
                        ReweavingAnalysis flowinsReIter = (ReweavingAnalysis) optClass.newInstance();
                        passes.add( new ReweavingPass( PASS_TM_ANALYSIS_FLOWINS_REITER , flowinsReIter ) );
                        System.out.println("Found and installed plug-in for reiteration of flow-insensitive analysis.");
                    } catch (ClassNotFoundException e) {
                    } catch (InstantiationException e) {
                    } catch (IllegalAccessException e) {
                    };
                }
            }
            
            //pass for dynamic instrumentation 

            if(Debug.v().dynaInstr) {
                ReweavingAnalysis dynaInstr = new AbstractReweavingAnalysis() {
                    @Override
                    public boolean analyze() {
                        DynamicInstrumenter.v().createClassesAndSetDynamicResidues();
                        return false;
                    }
                    @Override
                    public void cleanup() {                        
                        DynamicInstrumenter.v().insertDumpCall();
                    }
                };
                passes.add( new ReweavingPass( PASS_DYNAMIC_INSTRUMENTATION , dynaInstr ) );
            }

            //add a pass which just cleans up resources;
            //this is necessary in order to reset static fields for the test harness
            
            ReweavingAnalysis cleanup = new AbstractReweavingAnalysis() {

                @Override
                public boolean analyze() {
                    //disable all some and sync advice that became inactive
                    ShadowRegistry.v().disableAllUnneededSomeSyncAndBodyAdvice();
                    return false;
                }
                
                @Override
                public void cleanup() {
                    //dump shadows in the end
                    ShadowRegistry.v().dumpShadows();
                    //reset state
                    CallGraphAbstraction.reset();
                    FlowInsensitiveAnalysis.reset();
                    QuickCheck.reset();
                    ReachableShadowFinder.reset();
                    ShadowGroupRegistry.reset();
                    ShadowRegistry.reset();
                    TMShadowTagger.reset();
                    WeavableMethods.reset();
                    SymbolShadow.reset();
                }
            };
            passes.add( new ReweavingPass( PASS_TM_ANALYSIS_CLEANUP , cleanup ) );
        }
    }
    
    /** within a single tracematch, normal precedence rules apply for recognition of symbols.
         the "some" advice has higher precedence than all symbols in the same tracematch
         if it is after advice; it has lower precedence than all symbols if it is before advice
         
         the "synch" advice always has higher precedence than anything else in the same tracematch */
	public int tmGetPrec(TMAdviceDecl tma,TMAdviceDecl tmb) {
        if (tma.getTraceMatchID().equals(tmb.getTraceMatchID())) {

            int tma_first;
            int tma_second;
            int tmb_first;
            int tmb_second;

            if (tma.getAdviceSpec().isAfter())
            {
                tma_first = GlobalAspectInfo.PRECEDENCE_SECOND;
                tma_second = GlobalAspectInfo.PRECEDENCE_FIRST;
            } else {
                tma_first = GlobalAspectInfo.PRECEDENCE_FIRST;
                tma_second = GlobalAspectInfo.PRECEDENCE_SECOND;
            }

            if (tmb.getAdviceSpec().isAfter())
            {
                tmb_first = GlobalAspectInfo.PRECEDENCE_SECOND;
                tmb_second = GlobalAspectInfo.PRECEDENCE_FIRST;
            } else {
                tmb_first = GlobalAspectInfo.PRECEDENCE_FIRST;
                tmb_second = GlobalAspectInfo.PRECEDENCE_SECOND;
            }

            if (tma.isBody() && !tmb.isBody())
                return tma_second;
            if (!tma.isBody() && tmb.isBody())
                return tmb_first;
            if (tma.isBody() && tmb.isBody())
                // we have tma==tmb, as there is at most one piece
                // of "body" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;

            if (tma.isSynch() && !tmb.isSynch())
                return tma_first;
            if (!tma.isSynch() && tmb.isSynch())
                return tmb_second;
            if (tma.isSynch() && tmb.isSynch())
                // we have tma==tmb, as there is at most one piece
                // of "synch" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;

            if (tma.isSome() && !tmb.isSome())
                return tma_second;
            if (!tma.isSome() && tmb.isSome())
                return tmb_first;
            if (tma.isSome() && tmb.isSome())
                // we have tma==tmb, as there is at most one piece
                // of "some" advice
                return GlobalAspectInfo.PRECEDENCE_NONE;
		    	 
		    	 
		    	 
				int lexicalfirst,lexicalsecond;
				if  (tma.getAdviceSpec().isAfter() || tmb.getAdviceSpec().isAfter() ) {
					lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
					lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
				} else {
					lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
					lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
				}
	    	    // neither is "some" advice, so just compare positions
				if(tma.getPosition().line() < tmb.getPosition().line())
					return lexicalfirst;
				if(tma.getPosition().line() > tmb.getPosition().line())
					return lexicalsecond;
				// both pieces of advice are on the same line, compare columns
				if(tma.getPosition().column() < tmb.getPosition().column())
					return lexicalfirst;
				if(tma.getPosition().column() > tmb.getPosition().column())
					return lexicalsecond;
				// we have a==b
				return GlobalAspectInfo.PRECEDENCE_NONE;
	       }
	       // do the comparison via the containing tracematches
	       return getPrec(tma,tmb);
	}
	
	
	protected int getPrec(AdviceDecl a,AdviceDecl b) {
			// We know that we are in the same aspect
			// and *not* within the same tracematch

			int lexicalfirst,lexicalsecond;

			// not sure about this: do we want to ignore advice type when it's
			// in a trace match?
			if( (a.getAdviceSpec().isAfter()  && !(a instanceof TMAdviceDecl)) || 
			     (b.getAdviceSpec().isAfter()  && !(b instanceof TMAdviceDecl))) {
				lexicalfirst=GlobalAspectInfo.PRECEDENCE_SECOND;
				lexicalsecond=GlobalAspectInfo.PRECEDENCE_FIRST;
			} else {
				lexicalfirst=GlobalAspectInfo.PRECEDENCE_FIRST;
				lexicalsecond=GlobalAspectInfo.PRECEDENCE_SECOND;
			}
			
			// as a and b are *not* within the same tracematch, we use the positions
			// of the containing tracematches for precedence comparison
			Position ap = ((a instanceof TMAdviceDecl) ? 
			                       ((TMAdviceDecl)a).getTraceMatchPosition() : 
			                       a.getPosition());
			Position bp = ((b instanceof TMAdviceDecl) ? 
			                       ((TMAdviceDecl)b).getTraceMatchPosition() : 
			                       b.getPosition());
        
        
			if(ap.line() < bp.line())
				return lexicalfirst;
			if(ap.line() > bp.line())
				return lexicalsecond;

			if(ap.column() < bp.column())
				return lexicalfirst;
			if(ap.column() > bp.column())
				return lexicalsecond;

			// Trying to compare the same advice, I guess... (modulo inlining behaviour)
			return GlobalAspectInfo.PRECEDENCE_NONE;
    }
	   
	/** amended for tracematches */
	public int getPrecedence(AbstractAdviceDecl a,AbstractAdviceDecl b) {
		   // a quick first pass to assist in separating out the major classes of advice
		   // consider delegating this
		   int aprec=getPrecNum(a),bprec=getPrecNum(b);
		   if(aprec>bprec) return GlobalAspectInfo.PRECEDENCE_FIRST;
		   if(aprec<bprec) return GlobalAspectInfo.PRECEDENCE_SECOND;

		   // CflowSetup needs to be compared by depth first
		   if(a instanceof CflowSetup && b instanceof CflowSetup)
			   return CflowSetup.getPrecedence((CflowSetup) a,(CflowSetup) b);

		   if(!a.getDefiningAspect().getName().equals(b.getDefiningAspect().getName()))
			   return abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getPrecedence(a.getDefiningAspect(),b.getDefiningAspect());

	       // change for tracematches starts here
			   if (a instanceof TMAdviceDecl && b instanceof TMAdviceDecl)
			   	   return tmGetPrec((TMAdviceDecl)a,(TMAdviceDecl)b);
			   	   
			   if(a instanceof AdviceDecl && b instanceof AdviceDecl)
				   return getPrec((AdviceDecl) a,(AdviceDecl) b);
		   // and ends here

		   if(a instanceof DeclareSoft && b instanceof DeclareSoft)
			   return DeclareSoft.getPrecedence((DeclareSoft) a,(DeclareSoft) b);

		   // We don't care about precedence since these won't ever get woven
		   if(a instanceof DeclareMessage && b instanceof DeclareMessage)
			   return GlobalAspectInfo.PRECEDENCE_NONE;

		   throw new InternalCompilerError
			   ("case not handled when comparing "+a+" and "+b);
	   }
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public CompileSequence createCompileSequence() {
		return new CompileSequence(this) {
			@Override
			public void reset() {
				super.reset();
				//reset static nmembers for tracematches
		        abc.tm.weaving.aspectinfo.TraceMatch.reset();
			}
		};
	}
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void doMethodRestructuring() {
        super.doMethodRestructuring();
        
        String laststage = OptionsParser.v().laststage();
        if(OptionsParser.v().wp_tmopt() && !laststage.equals("quick") && !laststage.equals("flowins")) {
            TMLoopExitRestructurer.apply();
        }
    }
   
}
