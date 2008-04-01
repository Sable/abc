/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
 * Copyright (C) 2008 Torbjorn Ekman
 * Copyright (C) 2008 Julian Tibble
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

package abc.main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.base.ExceptionChecker;
import soot.jimple.toolkits.base.ExceptionCheckerError;
import soot.jimple.toolkits.base.ExceptionCheckerErrorReporter;
import abc.aspectj.visit.NoSourceJob;
import abc.aspectj.visit.OncePass;
import abc.aspectj.visit.PatternMatcher;
import abc.main.options.OptionsParser;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AspectJExceptionChecker;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.DeclareParents;
import abc.weaving.aspectinfo.DeclareParentsExt;
import abc.weaving.aspectinfo.DeclareParentsImpl;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.DeclareParentsWeaver;
import abc.weaving.weaver.InterprocConstantPropagator;
import abc.weaving.weaver.IntertypeAdjuster;
import abc.weaving.weaver.UnusedMethodsRemover;


public class CompileSequence {
	protected AbcExtension abcExt = null;
	
    protected ErrorQueue error_queue; // For reporting errors and warnings
    
    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ jar_classes = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classes_destdir = ""; // TODO: LJH - fixed with -d option?


	public CompileSequence(AbcExtension ext) {
		this.abcExt = ext;
	}
	
	public void passOptions(Collection/*<String>*/ aspect_sources,
			Collection/*<String>*/ jar_classes, List/*<String>*/ soot_args,
			List/*<String>*/ polyglot_args, String classes_destdir) {
		this.aspect_sources = aspect_sources;
		this.jar_classes = jar_classes;
		this.soot_args = soot_args;
		this.polyglot_args = polyglot_args;
		this.classes_destdir = classes_destdir;
	}
	
	public void runSequence() throws CompilerFailedException {
		try {
	        // if something to compile
	        compile(); // Timers marked inside compile()
	        // The compile method itself aborts if there is an error
		} catch(InternalCompilerError e) {
            // Polyglot adds something to the error queue for
            // InternalCompilerErrors,
            // and we only want to report the error if there are *other* errors
            abortIfErrors(1);
            throw e;
		} catch(Throwable e) {
			abortIfErrors();
			throw new InternalCompilerError("unhandled exception during compilation", e);
		}
        
		try {
	        if(!abcExt.getGlobalAspectInfo().getWeavableClasses().isEmpty()) {
	    		weave();
	
	    		abortIfErrors();
	
	            if (!abc.main.Debug.v().dontCheckExceptions) {
	                checkExceptions();
	                AbcTimer.mark("Exceptions check");
	                Debug.phaseDebug("Exceptions check");
	            }
	
	            inlineAdvice();
	            
	            abortIfErrors();
	
                optimize();
	            
	            // UnusedMethodsRemover.removeUnusedMethods(); // run it again after opts.
	
	            AbcTimer.mark("Soot Packs");
	            Debug.phaseDebug("Soot Packs");
	
	            output();
	            AbcTimer.mark("Soot Writing Output");
	            Debug.phaseDebug("Soot Writing Output");
	        }
		} catch(Throwable e) {
	    	abortIfErrors();
	    	throw new InternalCompilerError("unhandled exception during weaving/optimisation", e); 
	    }
	}
	
    /** reset all static information so main can be called again */
    public void reset() {
        soot.G.reset(); // reset all of Soot's global info
        // TODO: add a call here to the reset method for any class that
        //  needs static information reset for repeated calls to main
        abc.soot.util.Restructure.reset();
        abc.aspectj.visit.OncePass.reset();
        abc.aspectj.visit.PCStructure.reset();
        abc.aspectj.visit.AspectInfoHarvester.reset();
        abc.aspectj.parse.Lexer_c.reset();
        abc.weaving.aspectinfo.AbcFactory.reset();
        abc.weaving.weaver.around.AroundWeaver.reset();
        abc.weaving.matching.StmtShadowMatch.reset();
        abc.weaving.matching.ConstructorCallShadowMatch.reset();
        abc.weaving.matching.ExecutionShadowMatch.reset();
        abc.weaving.aspectinfo.GlobalCflowSetupFactory.reset();
        abc.soot.util.SwitchFolder.reset();
        //abc.weaving.weaver.AroundInliner.reset();
        //abc.weaving.weaver.AfterBeforeInliner.reset();
        abc.weaving.weaver.AdviceInliner.reset();
        abc.soot.util.LocalGeneratorEx.reset();
        abc.weaving.weaver.WeavingState.reset();
        abc.weaving.weaver.CflowCodeGenUtils.reset();
        abc.weaving.weaver.around.Util.reset();
        abc.weaving.weaver.around.AdviceApplicationInfo.reset();
    }
    
    public void inlineAdvice() {
        if (OptionsParser.v().O()!=0) {
            
       		if (OptionsParser.v().around_inlining() || OptionsParser.v().before_after_inlining()) {
            	abcExt.getWeaver().doInlining();
            
            	AbcTimer.mark("Advice inlining");
                Debug.phaseDebug("Advice inlining");
            
                InterprocConstantPropagator.inlineConstantArguments();
            
            	AbcTimer.mark("Interproc. constant propagator");
                Debug.phaseDebug("Interproc. constant propagator");
          
            	abcExt.getWeaver().runBoxingRemover();
            	
            	AbcTimer.mark("Boxing remover");
                Debug.phaseDebug("Boxing remover");
                
                if (!Debug.v().disableDuplicatesRemover) {
                	AdviceInliner.v().removeDuplicateInlineMethods();
                
                	AbcTimer.mark("Duplicates remover");
                	Debug.phaseDebug("Duplicates remover");
                }
                
            	UnusedMethodsRemover.removeUnusedMethods();
                
                AbcTimer.mark("Removing unused methods");
                Debug.phaseDebug("Removing unused methods");
                
                AdviceInliner.v().specializeReturnTypesOfInlineMethods();
                
                AbcTimer.mark("Specializing return types");
                Debug.phaseDebug("Specializing return types");
       		}
       }
    }
    
    private class GotCheckedExceptionError implements ExceptionCheckerErrorReporter {
        public void reportError(ExceptionCheckerError err) {
            SootClass exctype=err.excType();

            ErrorInfo e=ErrorInfoFactory.newErrorInfo
                (ErrorInfo.SEMANTIC_ERROR,
                 "The exception "+exctype+" must be either caught "+
                 "or declared to be thrown",
                 err.method(),
                 err.throwing());

            error_queue.enqueue(e);
        }
    }

    public void checkExceptions() {
        ExceptionChecker exccheck=new AspectJExceptionChecker(new GotCheckedExceptionError());
        HashMap options=new HashMap();
        options.put("enabled","true");

        for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {

            final AbcClass cl = (AbcClass) clIt.next();

            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {

                final SootMethod method = (SootMethod) methodIt.next();

                if(!method.isConcrete()) continue;
                if(method.getName().equals(SootMethod.staticInitializerName))
                    continue;
                try {
                    //FIXME: is "jtp.jec" sensible?
                    exccheck.transform(method.getActiveBody(),"jtp.jec",options);
                } catch(InternalCompilerError e) {
                    throw e;
                } catch(Throwable e) {
                    throw new InternalCompilerError("Exception while checking exceptions in "+method,e);
                }
            }
        }
    }

    public void validate() {
        for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            abc.soot.util.Validate.validate(cl.getSootClass());
        }
    }

    public void optimize(){
        PackManager.v().runBodyPacks();
    }

    public void output() {
        // Write classes
        PackManager.v().writeOutput();
    }

    public void compile() throws CompilerFailedException, IllegalArgumentException {
        // Invoke polyglot
        try {
            abc.aspectj.ExtensionInfo ext =
                abcExt.makeExtensionInfo(jar_classes, aspect_sources);
            Options options = ext.getOptions();
            options.assertions = true;
            options.serialize_type_info = false;
            options.classpath = OptionsParser.v().classpath();
            if (polyglot_args.size() > 0) {
                String[] polyglot_argv = (String[]) polyglot_args.toArray(new String[0]);
                Set sources = new HashSet(aspect_sources);
                options.parseCommandLine(polyglot_argv, sources);
                // FIXME: Use updated source set?
            }
            Options.global = options;
            
            Compiler compiler;
            error_queue = abcExt.getErrorQueue();
            if(error_queue == null) {
            	compiler = createCompiler(ext);
            	error_queue = compiler.errorQueue();
            } else {
            	compiler = createCompiler(ext, error_queue);
            }

            AbcTimer.mark("Create polyglot compiler");
            Debug.phaseDebug("Create polyglot compiler");
            try {
                if(Debug.v().printWeavableClasses) {
                    System.err.println( "aspect_sources are "+aspect_sources );
                }
                if (!aspect_sources.isEmpty()) {
                    if (!compiler.compile(aspect_sources)) {
                        throw new CompilerFailedException("Compilation failed.");
                    }
                } else {
                    // No source files. Run all once-passes.
                    Job job = new NoSourceJob(ext);
                    List passes = ext.passes(job);
                    Iterator pi = passes.iterator();
                    while (pi.hasNext()) {
                        Pass p = (Pass)pi.next();
                        if (p instanceof OncePass) {
                            ((OncePass)p).run();
                        }
                    }
                }
            } finally {
                // we need the error queue in the frontend, too, to generate warnings,
                // so this assignment was moved up.
                // error_queue = compiler.errorQueue();
            }
            Scene.v().loadDynamicClasses();
            abortIfErrors();

            AbcTimer.mark("Polyglot phases");
            Debug.phaseDebug("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

                if(Debug.v().printWeavableClasses) {
                    System.err.println( "WeavableClasses are "+abcExt.getGlobalAspectInfo().getWeavableClasses() );
                }
            for( Iterator clsIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clsIt.hasNext(); ) {
                final AbcClass cls = (AbcClass) clsIt.next();
                SootClass scls = cls.getSootClass();
                scls.setApplicationClass();
                Scene.v().loadClass(scls.getName(), SootClass.BODIES);
            }
            AbcTimer.mark("Initial Soot resolving");
            Debug.phaseDebug("Initial Soot resolving");

            // Make sure that anything mentioned on the RHS of a declare parents
            // clause is resolved to HIERARCHY, so that the declare parents
            // weaver knows what to do with it
            for( Iterator dpIt = abcExt.getGlobalAspectInfo().getDeclareParents().iterator(); dpIt.hasNext(); ) {
                final DeclareParents dp = (DeclareParents) dpIt.next();
                if(dp instanceof DeclareParentsImpl) {
                    final DeclareParentsImpl dpi = (DeclareParentsImpl) dp;
                    for( Iterator iIt = dpi.getInterfaces().iterator(); iIt.hasNext(); ) {
                        final AbcClass i = (AbcClass) iIt.next();
                        Scene.v().loadClass(i.getSootClass().getName(),SootClass.HIERARCHY);
                    }
                } else if(dp instanceof DeclareParentsExt) {
                    final DeclareParentsExt dpe = (DeclareParentsExt) dp;
                    Scene.v().loadClass(dpe.getParent().getSootClass().getName(),
                                        SootClass.HIERARCHY);
                } else throw new InternalCompilerError("Unknown kind of declare parents");
            }

            Scene.v().setMainClassFromOptions();
            AbcTimer.mark("Soot resolving");
            Debug.phaseDebug("Soot resolving");

            abcExt.getGlobalAspectInfo().buildAspectHierarchy();
            AbcTimer.mark("Aspect inheritance");
            Debug.phaseDebug("Aspect inheritance");

        } catch (polyglot.main.UsageError e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
        }

        // Output the aspect info
        if (abc.main.Debug.v().aspectInfo)
            abcExt.getGlobalAspectInfo().print(System.err);
    }

    protected Compiler createCompiler(ExtensionInfo ext) {
        return new Compiler(ext);
    }
    
    protected Compiler createCompiler(ExtensionInfo ext, ErrorQueue eq) {
    	return new Compiler(ext, eq);
    }

    public void weave() throws CompilerFailedException {
        try {
            // Perform the declare parents
            new DeclareParentsWeaver().weave();
            // FIXME: put re-resolving here, from declareparents weaver
            AbcTimer.mark("Declare Parents");
            Debug.phaseDebug("Declare Parents");
            Scene.v().setDoneResolving();

            // Adjust Soot types for intertype decls
            IntertypeAdjuster ita = new IntertypeAdjuster();
            ita.adjust();
            AbcTimer.mark("Intertype Adjuster");
            Debug.phaseDebug("Intertype Adjuster");

            // Retrieve all bodies
            for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                final AbcClass cl = (AbcClass) clIt.next();
                if(Debug.v().showWeavableClasses) System.err.println("Weavable class: "+cl);
                for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                    final SootMethod method = (SootMethod) methodIt.next();
                    try {
                        if( !method.isConcrete() ) continue;
                        // System.out.println("retrieve "+method+ " from "+cl);
                        method.retrieveActiveBody();
                    } catch(InternalCompilerError e) {
                        throw e;
                    } catch(Throwable e) {
                        throw new InternalCompilerError("Exception while processing "+method.getSignature(),e);
                    }
                }
            }
            AbcTimer.mark("Jimplification");
            Debug.phaseDebug("Jimplification");
            
            //call hook for (optional) restructuring of methods
            abcExt.doMethodRestructuring();

            // Fix up constructors in binary classes with newly declared parents
            new DeclareParentsConstructorFixup().weave();
            AbcTimer.mark("Fix up constructor calls");
            Debug.phaseDebug("Fix up constructor calls");

            PatternMatcher.v().updateWithAllSootClasses();
            // evaluate the patterns the third time (depends on re-resolving)
            PatternMatcher.v().recomputeAllMatches();
            AbcTimer.mark("Update pattern matcher");
            Debug.phaseDebug("Update pattern matcher");

            // any references made by itd initialisers will appear in a delegate method,
            // and thus have already been processed by j2j; all resolving ok.
            ita.initialisers(); // weave the field initialisers into the constructors
            AbcTimer.mark("Weave Initializers");
            Debug.phaseDebug("Weave Initializers");

            if (!Debug.v().testITDsOnly) {
                // Make sure that all the standard AspectJ shadow types are loaded
                AbcTimer.mark("Load shadow types");
                Debug.phaseDebug("Load shadow types");

                // for each shadow in each weavable class, compute list of applicable advice
                abcExt.getGlobalAspectInfo().computeAdviceLists();
                AbcTimer.mark("Compute advice lists");
                Debug.phaseDebug("Compute advice lists");                

                if(Debug.v().printAdviceApplicationCount) {
                	int adviceApplCount=0;
                	
                    for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                	
                        final AbcClass cl = (AbcClass) clIt.next();
                        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next(); 
                            MethodAdviceList list=abcExt.getGlobalAspectInfo().getAdviceList(method);
                            if (list==null)
                            	continue;
                            List allAdvice=list.allAdvice();
                            adviceApplCount += allAdvice.size();                           	
                        }
                    }                   
                    System.out.println("Number of advice applications: " + adviceApplCount);
                }
                if(Debug.v().matcherTest) {
                	System.err.println("--- BEGIN ADVICE LISTS ---");
                    // print out matching information for testing purposes
                    for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                        final AbcClass cl = (AbcClass) clIt.next();
                        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next();
                            final StringBuffer sb=new StringBuffer(1000);
                            sb.append("method: "+method.getSignature()+"\n");
                            abcExt.getGlobalAspectInfo().getAdviceList(method).debugInfo(" ",sb);
                            System.err.println(sb.toString());
                        }
                    }         
                    System.err.println("--- END ADVICE LISTS ---");
                }

                if(abc.main.options.OptionsParser.v().warn_unused_advice()) {
                    for( Iterator adIt = abcExt.getGlobalAspectInfo().getAdviceDecls().iterator(); adIt.hasNext(); ) {
                        final AbstractAdviceDecl ad = (AbstractAdviceDecl) adIt.next();

                        if(ad instanceof AdviceDecl && ad.getApplWarning() != null)
                            error_queue.enqueue(ErrorInfo.WARNING,
                                                ad.getApplWarning(),
                                                ad.getPosition());
                    }
                }

                //Weaver weaver = new Weaver();
                abcExt.getWeaver().weave(); // timer marks inside weave() */
            }
            // the intertype adjuster has put dummy fields into interfaces,
            // which now have to be removed
            ita.removeFakeFields();
        } catch(SemanticException e) {
            error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,e.getMessage(),e.position());
        }
    }

    private void abortIfErrors() throws CompilerFailedException {
        abortIfErrors(0);
    }

    private void abortIfErrors(int n) throws CompilerFailedException {
        if(error_queue!=null && error_queue.errorCount()>n) {
            error_queue.flush();
            throw new CompilerFailedException("There were errors.");
        }
    }
}
