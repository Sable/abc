/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Aske Simon Christensen
 * Copyright (C) 2004 Laurie Hendren
 * Copyright (C) 2004 Ganesh Sittampalam
 * Copyright (C) 2004 Sascha Kuzins
 * Copyright (C) 2004 Ondrej Lhotak
 * Copyright (C) 2004 Jennifer Lhotak
 * Copyright (C) 2004 Julian Tibble
 * Copyright (C) 2004 Pavel Avgustinov
 * Copyright (C) 2004 Oege de Moor
 * Copyright (C) 2004 Damien Sereni
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Job;
import polyglot.frontend.Pass;
import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import soot.CompilationDeathException;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Timers;
import soot.Transform;
import soot.javaToJimple.AbstractJBBFactory;
import soot.javaToJimple.AbstractJimpleBodyBuilder;
import soot.javaToJimple.AccessFieldJBB;
import soot.javaToJimple.InitialResolver;
import soot.javaToJimple.JimpleBodyBuilder;
import soot.jimple.toolkits.base.ExceptionChecker;
import soot.jimple.toolkits.base.ExceptionCheckerError;
import soot.jimple.toolkits.base.ExceptionCheckerErrorReporter;
import abc.aspectj.visit.NoSourceJob;
import abc.aspectj.visit.OncePass;
import abc.aspectj.visit.PatternMatcher;
import abc.main.options.ArgList;
import abc.main.options.OptionsParser;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AspectJExceptionChecker;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.aspectinfo.DeclareParents;
import abc.weaving.aspectinfo.DeclareParentsExt;
import abc.weaving.aspectinfo.DeclareParentsImpl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.tagkit.InstructionInlineCountTagAggregator;
import abc.weaving.tagkit.InstructionInlineTagsAggregator;
import abc.weaving.tagkit.InstructionKindTagAggregator;
import abc.weaving.tagkit.InstructionProceedTagAggregator;
import abc.weaving.tagkit.InstructionShadowTagAggregator;
import abc.weaving.tagkit.InstructionSourceTagAggregator;
import abc.weaving.weaver.AdviceInliner;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.DeclareParentsWeaver;
import abc.weaving.weaver.InterprocConstantPropagator;
import abc.weaving.weaver.IntertypeAdjuster;
import abc.weaving.weaver.UnusedMethodsRemover;
import abc.weaving.weaver.Weaver;

/** The main class of abc. Responsible for parsing command-line arguments,
 *  initialising Polyglot and Soot, and driving the compilation process.
 *
 *  @author Aske Simon Christensen
 *  @author Laurie Hendren
 *  @author Ganesh Sittampalam
 *  @author Sascha Kuzins
 *  @author Ondrej Lhotak
 *  @author Jennifer Lhotak
 *  @author Julian Tibble
 *  @author Pavel Avgustinov
 *  @author Oege de Moor
 *  @author Damien Sereni
 */

public class Main {
    private static Main v=null;
    public static Main v() {
        return v;
    }

    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ jar_classes = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classes_destdir = ""; // TODO: LJH - fixed with -d option?

    public ErrorQueue error_queue; // For reporting errors and warnings

    // delegate all behaviour that might be added to or modified
    // by an extension to an extension-specific class
    private AbcExtension abcExtension = null;

    public AbcExtension getAbcExtension()
    {
        return abcExtension;
    }

    /** reset all static information so main can be called again */
    public static void reset() {
        soot.G.reset(); // reset all of Soot's global info
        // TODO: add a call here to the reset method for any class that
        //  needs static information reset for repeated calls to main
        abc.main.Debug.reset();
        abc.main.AbcTimer.reset();
        abc.main.Options.reset();
        abc.soot.util.Restructure.reset();
        abc.aspectj.visit.OncePass.reset();
        abc.aspectj.visit.PCStructure.reset();
        abc.aspectj.visit.AspectInfoHarvester.reset();
        abc.aspectj.parse.Lexer_c.reset();
        abc.weaving.aspectinfo.AbcFactory.reset();
        abc.weaving.weaver.around.AroundWeaver.reset();
        abc.weaving.matching.StmtShadowMatch.reset();
        abc.weaving.matching.ExecutionShadowMatch.reset();
        abc.weaving.aspectinfo.GlobalCflowSetupFactory.reset();
        abc.soot.util.SwitchFolder.reset();
        //abc.weaving.weaver.AroundInliner.reset();
        //abc.weaving.weaver.AfterBeforeInliner.reset();
        abc.weaving.weaver.AdviceInliner.reset();
        abc.soot.util.LocalGeneratorEx.reset();
        abc.main.options.OptionsParser.reset();
        abc.weaving.weaver.WeavingState.reset();
        abc.weaving.weaver.CflowCodeGenUtils.reset();
		abc.weaving.weaver.around.Util.reset();
		
        v=null;
    }

    public static void compilerOptionIgnored(String option, String message)
    { G.v().out.println( "*** Option " + option + " ignored: " + message);
    }

    public void abcPrintVersion()
    {
        // display the abc version along with the version
        // of any loaded extension
        G.v().out.print(getAbcExtension().versions());

        G.v().out.println("... using Soot toolkit version " +
                soot.Main.v().versionString);
        G.v().out.println("... using Polyglot compiler toolkit version " +
                new polyglot.ext.jl.Version());
        G.v().out.println("For usage,  abc -help");
        G.v().out.println("-------------------------------------------------------------------------------");
        G.v().out.println("Copyright (C) 2004 The abc development team. All rights reserved.");
        G.v().out.println("See the file CREDITS for a list of contributors.");
        G.v().out.println("See individual source files for details.");
        G.v().out.println("");
        G.v().out.println("Soot is Copyright (C) 1997-2004 Raja Vallee-Rai and others.");
        G.v().out.println("Polyglot is Copyright (C) 2000-2004 Polyglot project group, Cornell University.");
        G.v().out.println("");
        G.v().out.println("abc is distributed in the hope that it will be useful, and comes with");
        G.v().out.println("ABSOLUTELY NO WARRANTY; without even the implied warranty of MERCHANTABILITY");
        G.v().out.println("or FITNESS FOR A PARTICULAR PURPOSE.");
        G.v().out.println("");
        G.v().out.println("abc is free software; you can redistribute it and/or modify it under the");
        G.v().out.println("terms of the GNU Lesser General Public License as published by the Free");
        G.v().out.println("Software Foundation; either version 2.1 of the License, or (at your option)");
        G.v().out.println("any later version. See the file LESSER-GPL for details.");
        G.v().out.println("-------------------------------------------------------------------------------");
    }


    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (CompilerAbortedException e) {
            // Encountered one of the conditions that make compilation impossible, e.g. no options given
            System.exit(0);
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            System.out.println("Illegal arguments: "+e.getMessage());
            System.exit(1);
        } catch (CompilerFailedException e) {
            //System.out.println(e.getMessage());
            System.exit(5);
        }
    }

    public Main(String[] args) throws IllegalArgumentException, CompilerAbortedException {
        parseArgs(args);
        v=this;
    }


    public void parseArgs(String[] argArray) throws IllegalArgumentException, CompilerAbortedException {
        ArgList args = new ArgList(argArray);
        boolean noArguments = args.isEmpty();
        OptionsParser.v().set_classpath(System.getProperty("java.class.path"));

        // The following Soot args need to go at the beginning, so that they
        // may be overridden by explicit command-line options.
        soot_args.add("-p");
        soot_args.add("cg");
        soot_args.add("enabled:true");
        soot_args.add("-p");
        soot_args.add("cg.paddle");
        soot_args.add("enabled:true");
        soot_args.add("-p");
        soot_args.add("cg.paddle");
        soot_args.add("backend:javabdd");

        while(!args.isEmpty())
        { 

            if (args.top().equals("-noImportError"))
                // don't report unresolved imports
            { // nothing to do, because we don't do it anyway.
            }

            // -Xlint, -Xlint:ignore, -Xlint:warning, -Xlint:errror
            else if (args.top().equals("-Xlint") ||
                    args.top().equals("-Xlint:warning") ||
                    args.top().equals("-Xlint:error") ||
                    args.top().equals("-Xlint:ignore"))
            { compilerOptionIgnored(args.top(),
                    "abc does not support Xlint");
                if (args.top().equals("Xlint") || args.top().equals("Xlint:warning"))
                    abc.main.Options.v().Xlint = abc.main.Options.WARNING;
                else if (args.top().equals("Xlint:error"))
                    abc.main.Options.v().Xlint = abc.main.Options.ERROR;
                else
                    abc.main.Options.v().Xlint = abc.main.Options.IGNORE;
            }

            // -1.3, -1.4
            else if (args.top().equals("-1.3"))
                abc.main.Debug.v().java13=true;
            else if (args.top().equals("-1.4"))
                abc.main.Debug.v().java13=false;

            // -target 1.1,  -target 1.2,  -target 1.3, -target 1.4
            else if (args.top().equals("-target"))
            {
                String arg = args.argTo();
                if (arg.equals("1.1") || arg.equals("1.2") ||
                        arg.equals("1.3") || arg.equals("1.4"))
                    compilerOptionIgnored("-target " + arg,
                            "abc-generated code should run on any 1.1 - 1.4 VM.");
                else
                    compilerOptionIgnored("-target " + arg,
                            arg + " is not a known target number.");
            }

            // -source 1.3,  -source 1.4
            else if (args.top().equals("-source"))
            {
                String arg = args.argTo();
                if (arg.equals("1.3"))
                    compilerOptionIgnored("-source 1.3",
                            "abc treats asserts as keywords as in 1.4");
                else if (arg.equals("1.4"))
                { // that's what we do ... so ok
                }
                else
                    compilerOptionIgnored("-source " + arg,
                            arg + " is not a known source number.");
            }

            // -nowarn, -warn:items  where items is a comma-delmited list
            else if (args.top().equals("-nowarn"))
            { // TODO: remove following line when compiler looks at flag
                compilerOptionIgnored(args.top(), "warnings not disabled.");
                abc.main.Options.v().warn = abc.main.Options.NOWARNINGS;
            }
            else if (args.top().startsWith("-warn:"))
            { // TODO: remove following line when compiler looks at flag
                compilerOptionIgnored(args.top(),
                        " warning flags not implemented yet.");
                // special case of -warn:none
                if (args.top().equals("-warn:none"))
                    abc.main.Options.v().warn = abc.main.Options.NOWARNINGS;
                else
                { String kindList = args.top().substring(6); // strip off "-warn:"
                    StringTokenizer kinds = new StringTokenizer(kindList,",");
                    // iterate through rest of list, adding them if they are allowed
                    { while (kinds.hasMoreTokens())
                        { String nextKind = kinds.nextToken();
                            if (abc.main.Options.v().isValidWarningName(nextKind))
                                abc.main.Options.v().addWarning(nextKind);
                            else
                                compilerOptionIgnored("-warn:" + nextKind,
                                        "is not a valid warning kind.");
                        }
                    }
                }
            }

            // -g, -g:none, -g:{items} where items can
            //              contain lines,vars,source
            else if (args.top().equals("-g"))
                compilerOptionIgnored(args.top(),
                        "abc does not yet support creating debug info.");
            else if (args.top().startsWith("-g:"))
                compilerOptionIgnored(args.top(),
                        "abc does not yet support creating debug info");

            /* -------- ABC-SPECIFIC OPTIONS, NO AJC EQUIVALENTS ----------*/
            // abc-specific options which have no ajc equivalents

            // TODO: should actually list only soot options useful for abc
            else if (args.top().equals("-help:soot"))
            { G.v().out.println(soot.options.Options.v().getUsage());
                throw new CompilerAbortedException("Acted on -help:soot option.");
            }

            // TODO; should actually list only polyglot options useful for abc
            else if (args.top().equals("-help:polyglot"))
            { abc.aspectj.ExtensionInfo ext =
                new abc.aspectj.ExtensionInfo(null, null);
                Options options = ext.getOptions();
                options.usage(G.v().out);
                throw new CompilerAbortedException("Acted on -help:polyglot option.");
            }

            // -debug and -nodebug flags in abc options
            else if (args.top().equals("-debug") || args.top().equals("-nodebug"))
            { String debug_no_debug = args.top();
                String debug_name = args.argTo();
                try {
                    Field f = abc.main.Debug.class.getField(debug_name);
                    f.setBoolean(abc.main.Debug.v(), debug_no_debug.equals("-debug"));
                }
                catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                            "No such debug option: "+debug_name);
                } catch (Exception e) {
                    throw new InternalCompilerError(
                            "Error setting debug field "+debug_name, e);
                }
            }

            /* -------------------  SOOT OPTIONS -------------------------*/
            // handle soot-specific options, must be between +soot -soot
            else if (args.top().equals("+soot"))
            { args.shift(); // skip +soot
                while (!args.isEmpty() && !args.top().equals("-soot"))
                { if (args.top().equals("-d")) // get the Soot -d option
                    OptionsParser.v().set_d(args.argTo());
                    else if (args.top().equals("-O"))
                        OptionsParser.v().set_O(1);
                    else
                        if(!args.top().equals("-keep-line-number") &&
                                !args.top().equals("-xml-attributes"))
                            soot_args.add(args.top());
                    args.shift();
                }
                if (args.isEmpty())
                    throw new IllegalArgumentException("no matching -soot found");
            } // +soot .... -soot optoins

            /* -------------------  POLYGLOT OPTIONS -------------------------*/
            // handle polyglot-specific options,
            //   must be between +polyglot -polyglot
            else if (args.top().equals("+polyglot"))
            {
                while (!args.isEmpty() && !args.top().equals("-polyglot"))
                { polyglot_args.add(args.top());
                    args.shift();
                }

                if (args.isEmpty())
                    throw new IllegalArgumentException(
                            "no matching -polyglot found");
            } // +polyglot ... -polyglot options

            else if(OptionsParser.v().parse(args)) continue;

            /* ---------  UNKNOWN OPTION ---------------------------------- */
            else if (args.top().startsWith("-"))
            { throw new IllegalArgumentException("Unknown option "+args.top());
            }

            /* ---------  FILE NAME  ---------------------------------- */
            else
            { aspect_sources.add(args.top());
            } // must be file name

            args.shift();
        } // for each arg
        if (OptionsParser.v().help()) {
            abc.main.Usage.abcPrintHelp();
            throw new CompilerAbortedException("Acted on -help option.");
        }

        if(OptionsParser.v().outjar() != null) {
            soot_args.add("-outjar");
            soot_args.add("-d");
            soot_args.add(OptionsParser.v().outjar());
        } else if(OptionsParser.v().d() != null) {
            soot_args.add("-d");
            soot_args.add(OptionsParser.v().d());
        } else {
            soot_args.add("-d");
            soot_args.add(".");
        }

        if(OptionsParser.v().O()>0) {
            soot_args.add("-O");
        }
        if(OptionsParser.v().O()>=3) {
            soot_args.add("-w");
        }
        if(OptionsParser.v().main_class() != null) {
            soot_args.add("-main-class");
            soot_args.add(OptionsParser.v().main_class());
        }
        if(OptionsParser.v().dava()) {
            soot_args.add("-f");
            soot_args.add("dava");
        }
        soot_args.add("-keep-line-number");

        if(OptionsParser.v().time()) {
            abc.main.Debug.v().abcTimer=true;
            abc.main.Debug.v().polyglotTimer=true;
            abc.main.Debug.v().sootResolverTimer=true;
        }

        // now we have parsed the arguments we know which AbcExtension
        // to load
        loadAbcExtension(OptionsParser.v().ext());

        if (noArguments)
        {
            abcPrintVersion();
            throw new CompilerAbortedException("No arguments provided.");
        }

        if (OptionsParser.v().version())
        {
            abcPrintVersion();
            throw new CompilerAbortedException("Acted on -version option.");
        }

	// -O3 optimisations require -main-class option
        if(OptionsParser.v().main_class() == null && OptionsParser.v().O() >= 3) {
	    throw new IllegalArgumentException("Interprocedural analyses (-O3 " +
		  "and above) require specifying the main class for the control-flow " +
					       "analysis with the -main-class option.");
	}
    }

    
    public void run() throws CompilerFailedException {
        try {
            // Timer start stuff
            Date abcstart = new Date(); // wall clock time start
            if(OptionsParser.v().verbose())
                G.v().out.println("abc started on " + abcstart);
            else
                soot.G.v().out=new java.io.PrintStream
                    (new java.io.OutputStream() {
                        public void write(int b) { }
                    });
            if (soot.options.Options.v().time())
                Timers.v().totalTimer.start(); // Soot timer start

            // Main phases

            AbcTimer.start(); // start the AbcTimer

            addJarsToClasspath();
            initSoot();
            AbcTimer.mark("Init. of Soot");
            phaseDebug("Init. of Soot");

            loadJars();
            loadSourceRoots();
            AbcTimer.mark("Loading Jars");
            phaseDebug("Loading Jars");


            // if something to compile
            compile(); // Timers marked inside compile()
            // The compile method itself aborts if there is an error

            if (!getAbcExtension().getGlobalAspectInfo().getWeavableClasses().isEmpty()) {
                weave(); // Timers marked inside weave()

                abortIfErrors();

                if (!abc.main.Debug.v().dontCheckExceptions) {
                    checkExceptions();
                    AbcTimer.mark("Exceptions check");
                    phaseDebug("Exceptions check");
                }
                
               //AroundWeaver.reset();
               //AbcTimer.mark("Freeing around memory");
               //phaseDebug("Freeing around memory");
                
               if (OptionsParser.v().O()!=0) {
                
               		if (OptionsParser.v().around_inlining() || OptionsParser.v().before_after_inlining()) {
	                	getAbcExtension().getWeaver().doInlining();
	                
	                	AbcTimer.mark("Advice inlining");
	                    phaseDebug("Advice inlining");
	                
	                    InterprocConstantPropagator.inlineConstantArguments();
	                
	                	AbcTimer.mark("Interproc. constant propagator");
	                    phaseDebug("Interproc. constant propagator");
	              
	                	getAbcExtension().getWeaver().runBoxingRemover();
	                	
	                	AbcTimer.mark("Boxing remover");
	                    phaseDebug("Boxing remover");
	                    
	                    if (!Debug.v().disableDuplicatesRemover) {
	                    	AdviceInliner.v().removeDuplicateInlineMethods();
	                    
	                    	AbcTimer.mark("Duplicates remover");
	                    	phaseDebug("Duplicates remover");
	                    }
	                    
	                	UnusedMethodsRemover.removeUnusedMethods();
	                    
	                    AbcTimer.mark("Removing unused methods");
	                    phaseDebug("Removing unused methods");
	                    
	                    AdviceInliner.v().specializeReturnTypesOfInlineMethods();
	                    
	                    AbcTimer.mark("Specializing return types");
	                    phaseDebug("Specializing return types");
               		}
               }
                
                abortIfErrors();

                optimize();
                
                // UnusedMethodsRemover.removeUnusedMethods(); // run it again after opts.

                AbcTimer.mark("Soot Packs");
                phaseDebug("Soot Packs");

                output();
                AbcTimer.mark("Soot Writing Output");
                phaseDebug("Soot Writing Output");
            }

            // Timer end stuff
            Date abcfinish = new Date(); // wall clock time finish
            if(OptionsParser.v().verbose()) {
                G.v().out.print("abc finished on " + abcfinish + ".");
                long runtime = abcfinish.getTime() - abcstart.getTime();
                G.v().out.println(" ( " + (runtime / 60000) + " min. " + ((runtime % 60000) / 1000) + " sec. )");
            }

            // Print out Soot time stats, if Soot -time flag on.
            if (soot.options.Options.v().time()) {
                Timers.v().totalTimer.end();
                Timers.v().printProfilingInformation();
            }

            // Print out abc timer information
            AbcTimer.report();
        } catch (CompilerFailedException e) {
            throw e;
        } catch (OutOfMemoryError e) {
            System.err.println("An OutOfMemoryError occured. This means there wasn't enough available");
            System.err.println("memory to complete the compilation.");
            System.err.println("");
            System.err.println("If your computer has additional RAM, you can try increasing the size of");
            System.err.println("the heap available to abc by invoking it as");
            System.err.println("\tjava -Xmx512M abc.main.Main [...]");
            System.err.println("You can also further increase the size of the heap (in the example, ");
            System.err.println("512MB was specified). If this doesn't resolve your problem, please");
            System.err.println("contact the abc team at http://abc.comlab.ox.ac.uk with the relevant ");
            System.err.println("details.");
           // System.err.println("Stack trace:");
            //e.printStackTrace();
            System.err.println("Allocated heap size:" + 
            		NumberFormat.getNumberInstance().format(Runtime.getRuntime().totalMemory()));
            throw e;
        }
        catch (InternalCompilerError e) {
            // Polyglot adds something to the error queue for
            // InternalCompilerErrors,
            // and we only want to ignore the error if there are *other* errors
            abortIfErrors(1);
            throw e;
        } catch (Throwable e) {
            /*System.err.println("stack trace " + e.getMessage() + " " + e.getClass().getName());
              try {
              e.printStackTrace(System.err);
              }catch (Throwable e2) {
              System.err.println("/stack trace ex " + e2.getClass().getName());
              System.err.println(e.hashCode() + ": " + e2.hashCode());
              e2.printStackTrace();
              }
              System.err.println("/stack trace ");*/
            abortIfErrors();
            throw new InternalCompilerError("unhandled exception during compilation", e);
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


    public void addJarsToClasspath() {
        StringBuffer sb = new StringBuffer();
        Iterator jari = OptionsParser.v().injars().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            sb.append(jar);
            sb.append(File.pathSeparator);
        }
        jari = OptionsParser.v().inpath().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            sb.append(jar);
            sb.append(File.pathSeparator);
        }
        sb.append(OptionsParser.v().classpath());

        sb.append(File.pathSeparator);
        sb.append(System.getProperty("java.home"));
        sb.append(File.separator);
        sb.append("lib");
        sb.append(File.separator);
        sb.append("rt.jar");

        if(System.getProperty("abc.home")!=null) {
            sb.append(File.pathSeparator);
            sb.append(System.getProperty("abc.home"));
            sb.append(File.separator);
            sb.append("lib");
            sb.append(File.separator);
            sb.append("abc-runtime.jar");
        }

        OptionsParser.v().set_classpath(sb.toString());
    }

    public void initSoot() throws IllegalArgumentException {
        if(!OptionsParser.v().verbose())
            soot.G.v().out=new java.io.PrintStream(new java.io.OutputStream() {
                public void write(int b) { }
            });

        getAbcExtension().addJimplePacks();

        if(OptionsParser.v().tag_instructions()) {
            PackManager.v().getPack("tag").add(new Transform("tag.kindtag", new InstructionKindTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.sourcetag", new InstructionSourceTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.shadowtag", new InstructionShadowTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.inlinecounttag", new InstructionInlineCountTagAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.inlinetag", new InstructionInlineTagsAggregator()));
            PackManager.v().getPack("tag").add(new Transform("tag.proceedtag", new InstructionProceedTagAggregator()));
        }
        
        String[] soot_argv = (String[]) soot_args.toArray(new String[0]);
        //System.out.println(classpath);
        if (!soot.options.Options.v().parse(soot_argv)) {
            throw new IllegalArgumentException("Soot usage error");
        }

        InitialResolver.v().setJBBFactory(new AbstractJBBFactory(){
            protected AbstractJimpleBodyBuilder createJimpleBodyBuilder(){
                JimpleBodyBuilder jbb = new JimpleBodyBuilder();
                AccessFieldJBB afjbb = new AccessFieldJBB();
                afjbb.ext(jbb);
                return afjbb;
            }
        });

        Scene.v().setSootClassPath(OptionsParser.v().classpath());
        getAbcExtension().addBasicClassesToSoot();

        // FIXME: make ClassLoadException in soot, and catch it here
        // and check what was wrong
        Scene.v().loadBasicClasses();
        if(abc.main.Debug.v().doValidate) soot.options.Options.v().set_validate(true);
    }

    private void findSourcesInDir(String dir, Collection sources) throws IllegalArgumentException {
        File file = new File(dir);
        File[] files = file.listFiles();
        if( files == null ) {
            throw new IllegalArgumentException( "Sourceroot "+dir+" is not a directory");
        }
        for( int i = 0; i < files.length; i++ ) {
            if(files[i].isDirectory()) findSourcesInDir(files[i].getAbsolutePath(), sources);
            else {
                String fileName = files[i].getAbsolutePath();
                if( fileName.endsWith(".java") || fileName.endsWith(".aj") ) {
                    sources.add(fileName);
                }
            }
        }
    }
    public void loadSourceRoots() throws IllegalArgumentException {
        // Load the classes in all given roots
        Iterator rooti = OptionsParser.v().sourceroots().iterator();
        while (rooti.hasNext()) {
            String root = (String)rooti.next();
            findSourcesInDir(root, aspect_sources);
        }
    }
    
    
    private boolean isJar(String path) {
    	File f = new File(path);	
    	if(f.isFile() && f.canRead()) { 		
    	    if(path.endsWith("zip") || path.endsWith("jar")) {
    		return true;
    	    } else {
    		throw new IllegalArgumentException("Warning: the following -injars entry is not a supported archive file (must be .zip or .jar): " + path);
    	    }
    	}  
    	return false;
        }
    
    
    
    public List getClassesUnder(String aPath) {
        List fileNames = new ArrayList();
        if (isJar(aPath)) {
    	    try {
    		ZipFile archive = new ZipFile(aPath);
    		for (Enumeration entries = archive.entries(); 
    		     entries.hasMoreElements(); ) {
    		    ZipEntry entry = (ZipEntry) entries.nextElement();
    		    String entryName = entry.getName();
    		    int extensionIndex = entryName.lastIndexOf('.');
    		    if (extensionIndex >= 0) {
    			String entryExtension = entryName.substring(extensionIndex);
    			if (entryExtension.equals(".class")) {
    			    entryName = entryName.substring(0, extensionIndex);
    			    entryName = entryName.replace('/', '.');
    			    fileNames.add(entryName);
    			}
    		    }
    		}
    	    } catch(IOException e) {
    	    	throw new IllegalArgumentException("Error reading "+aPath + ":" +e.toString());
    	    }
    	} else {   
	    File file = new File(aPath);
	    File[] files = file.listFiles();
	    if (files == null) {
		  files = new File[1];
		  files[0] = file;
	    }
	    for (int i = 0; i < files.length; i++) {
		if (files[i].isDirectory()) {
		    List l =
			getClassesUnder(aPath + File.separatorChar + files[i].getName());
		    Iterator it = l.iterator();
		    while (it.hasNext()) {
			String s = (String) it.next();
			fileNames.add(files[i].getName() + "." + s);
		    }
		} else {
		    String fileName = files[i].getName();
		    if (fileName.endsWith(".class")) {
			int index = fileName.lastIndexOf(".class");
			fileNames.add(fileName.substring(0, index));
		    }
		}
	    }
    	}
        return fileNames;
    }

    public void loadJars() throws CompilerFailedException {
        // Load the classes in all given jars
        Iterator jari = OptionsParser.v().injars().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            List/*String*/ this_jar_classes = soot.SourceLocator.v().getClassesUnder(jar);
            jar_classes.addAll(this_jar_classes);
        }
        // System.out.println("before loop: "+jar_classes);
        jari = OptionsParser.v().inpath().iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            List/*String*/ this_jar_classes = getClassesUnder(jar);
            jar_classes.addAll(this_jar_classes);
        }
        // System.out.println(jar_classes);
    }

    public void compile() throws CompilerFailedException, IllegalArgumentException {
        // Invoke polyglot
        try {
            abc.aspectj.ExtensionInfo ext =
                getAbcExtension().makeExtensionInfo(jar_classes, aspect_sources);
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
            Compiler compiler = createCompiler(ext);
            error_queue = compiler.errorQueue();

            AbcTimer.mark("Create polyglot compiler");
            phaseDebug("Create polyglot compiler");
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
            phaseDebug("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

                if(Debug.v().printWeavableClasses) {
                    System.err.println( "WeavableClasses are "+abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses() );
                }
            for( Iterator clsIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clsIt.hasNext(); ) {
                final AbcClass cls = (AbcClass) clsIt.next();
                SootClass scls = cls.getSootClass();
                scls.setApplicationClass();
                Scene.v().loadClass(scls.getName(), SootClass.BODIES);
            }
            AbcTimer.mark("Initial Soot resolving");
            phaseDebug("Initial Soot resolving");

            // Make sure that anything mentioned on the RHS of a declare parents
            // clause is resolved to HIERARCHY, so that the declare parents
            // weaver knows what to do with it
            for( Iterator dpIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getDeclareParents().iterator(); dpIt.hasNext(); ) {
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
            phaseDebug("Soot resolving");

            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().buildAspectHierarchy();
            AbcTimer.mark("Aspect inheritance");
            phaseDebug("Aspect inheritance");

        } catch (polyglot.main.UsageError e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
        }

        // Output the aspect info
        if (abc.main.Debug.v().aspectInfo)
            abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().print(System.err);
    }

    protected Compiler createCompiler(ExtensionInfo ext) {
        return new Compiler(ext);
    }

    public void weave() throws CompilerFailedException {
        try {
            // Perform the declare parents
            new DeclareParentsWeaver().weave();
            // FIXME: put re-resolving here, from declareparents weaver
            AbcTimer.mark("Declare Parents");
            phaseDebug("Declare Parents");
            Scene.v().setDoneResolving();

            // Adjust Soot types for intertype decls
            IntertypeAdjuster ita = new IntertypeAdjuster();
            ita.adjust();
            AbcTimer.mark("Intertype Adjuster");
            phaseDebug("Intertype Adjuster");

            // Retrieve all bodies
            for( Iterator clIt = abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
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
            phaseDebug("Jimplification");

            // Fix up constructors in binary classes with newly declared parents
            new DeclareParentsConstructorFixup().weave();
            AbcTimer.mark("Fix up constructor calls");
            phaseDebug("Fix up constructor calls");

            PatternMatcher.v().updateWithAllSootClasses();
            // evaluate the patterns the third time (depends on re-resolving)
            PatternMatcher.v().recomputeAllMatches();
            AbcTimer.mark("Update pattern matcher");
            phaseDebug("Update pattern matcher");

            // any references made by itd initialisers will appear in a delegate method,
            // and thus have already been processed by j2j; all resolving ok.
            ita.initialisers(); // weave the field initialisers into the constructors
            AbcTimer.mark("Weave Initializers");
            phaseDebug("Weave Initializers");

            if (!Debug.v().testITDsOnly) {
                // Make sure that all the standard AspectJ shadow types are loaded
                AbcTimer.mark("Load shadow types");
                phaseDebug("Load shadow types");

                // for each shadow in each weavable class, compute list of applicable advice
                abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().computeAdviceLists();
                AbcTimer.mark("Compute advice lists");
                phaseDebug("Compute advice lists");                

                if(Debug.v().printAdviceApplicationCount) {
                	int adviceApplCount=0;
                	
                    for( Iterator clIt = getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                	
                        final AbcClass cl = (AbcClass) clIt.next();
                        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next(); 
                            MethodAdviceList list=getAbcExtension().getGlobalAspectInfo().getAdviceList(method);
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
                    for( Iterator clIt = getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                        final AbcClass cl = (AbcClass) clIt.next();
                        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next();
                            final StringBuffer sb=new StringBuffer(1000);
                            sb.append("method: "+method.getSignature()+"\n");
                            getAbcExtension().getGlobalAspectInfo().getAdviceList(method).debugInfo(" ",sb);
                            System.err.println(sb.toString());
                        }
                    }         
                    System.err.println("--- END ADVICE LISTS ---");
                }

                if(abc.main.options.OptionsParser.v().warn_unused_advice()) {
                    for( Iterator adIt = getAbcExtension().getGlobalAspectInfo().getAdviceDecls().iterator(); adIt.hasNext(); ) {
                        final AbstractAdviceDecl ad = (AbstractAdviceDecl) adIt.next();

                        if(ad instanceof AdviceDecl && ad.getApplWarning() != null)
                            error_queue.enqueue(ErrorInfo.WARNING,
                                                ad.getApplWarning(),
                                                ad.getPosition());
                    }
                }

                //Weaver weaver = new Weaver();
                getAbcExtension().getWeaver().weave(); // timer marks inside weave() */
            }
            // the intertype adjuster has put dummy fields into interfaces,
            // which now have to be removed
            ita.removeFakeFields();
        } catch(SemanticException e) {
            error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,e.getMessage(),e.position());
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

        for( Iterator clIt = getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {

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
        for( Iterator clIt = getAbcExtension().getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
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

    private void loadAbcExtension(String abcExtensionPackage)
    {
        Class abcExt;

        try {
            abcExt = Class.forName(abcExtensionPackage + ".AbcExtension");
            abcExtension = (AbcExtension) abcExt.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot load AbcExtension from " + abcExtensionPackage);
        }
    }

    /** Parse a path.separator separated path into the separate directories. */
    private void parsePath(String path, Collection paths) {
        String[] jars = path.split(System.getProperty("path.separator"));
        for(int j = 0; j < jars.length; j++) {
            // Do we need a sanity check here? !jars[j].equals("") or something like that?
            paths.add(jars[j]);
        }
    }

    public static void phaseDebug(String s) {
        if( Debug.v().debugPhases ) { 
        	String m="Done phase: "+s;        	
        	if (Debug.v().debugMemUsage) {
        		System.gc();        		
        		long bytes=(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        		
        		
        		NumberFormat numberFormatter=NumberFormat.getNumberInstance();       		
        	
        		String mem= numberFormatter.format(bytes) + " used. " + 
					numberFormatter.format(Runtime.getRuntime().totalMemory()) + " heap.";
        		int padding=79-m.length()-mem.length();
        		String p="";
        		while(padding-->0)
        			p+=" ";
        		m=m+ p + mem;
        	}
        	System.err.println(m);
        }
    }
}
