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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.LinkedList;

import java.io.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.frontend.Pass;
import polyglot.frontend.Job;
import polyglot.main.Options;
import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Timers;
import soot.Transform;
import soot.Value;
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
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AspectJExceptionChecker;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.DeclareParentsWeaver;
import abc.weaving.weaver.IntertypeAdjuster;
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
    public Collection/*<String>*/ in_jars = new ArrayList();
    public Collection/*<String>*/ source_roots = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classpath = System.getProperty("java.class.path");
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
        abc.main.AbcTimer.reset();
        abc.main.Options.reset();
        abc.soot.util.Restructure.reset();
        abc.aspectj.visit.OncePass.reset();
        abc.aspectj.visit.PCStructure.reset();
        abc.aspectj.visit.AspectInfoHarvester.reset();
        abc.aspectj.parse.Lexer_c.reset();
        abc.weaving.aspectinfo.AbcFactory.reset();
        abc.weaving.aspectinfo.GlobalAspectInfo.reset();
        abc.weaving.weaver.AroundWeaver.reset();
        abc.weaving.matching.StmtShadowMatch.reset();
        abc.weaving.matching.ExecutionShadowMatch.reset();
        abc.weaving.aspectinfo.GlobalCflowSetupFactory.reset();
                abc.weaving.weaver.CflowIntraproceduralAnalysis.reset();
                abc.weaving.weaver.CflowIntraAggregate.reset();

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
        G.v().out.println("For usage,  abc --help");
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
            System.out.println(e.getMessage());
            System.exit(5);
        }
    }

    public Main(String[] args) throws IllegalArgumentException, CompilerAbortedException {
        parseArgs(args);
        v=this;
    }

    static class ArgList extends LinkedList {
        ArgList(String[] args) {
            for(int i = 0; i < args.length; i++) add(args[i]);
        }
        /** Return the current arg. */
        String top() { return (String) getFirst(); }
        /** Return the argument of the current argument, or throw an exception
         * if there isn't one. */
        String argTo() {
            String top = top();
            shift();
            if(isEmpty())
                throw new IllegalArgumentException("Missing argument to " + top);
            return top();
        }
        /** Move to the next argument. */
        void shift() { removeFirst(); }
        /** Add arg to the front of the arg list. */
        void push( String arg ) { addFirst(arg); }
    }

    public void parseArgs(String[] argArray) throws IllegalArgumentException, CompilerAbortedException {
        ArgList args = new ArgList(argArray);
        String outputdir=".";
        boolean optflag=true;
        boolean outputIsJar=false;
        boolean noArguments = args.isEmpty();
        boolean seenVersionFlag = false;

        String abcExtensionPackage = "abc.main";

        for(; !args.isEmpty(); args.shift())
        { /* --------FULLY IMPLEMENTED AJC-COMPLIANT OPTIONS ----------*/
            // abc options that we handle completely,
            //     and correspond to ajc options

            // TODO: -help needs to be filled in
            if (args.top().equals("-help") || args.top().equals("--help") ||
                    args.top().equals("-h"))
            { abc.main.Usage.abcPrintHelp();
                throw new CompilerAbortedException("Acted on -help option.");
            }
            else if (args.top().equals("-version") || args.top().equals("--version") ||
                    args.top().equals("-v"))
            {
                seenVersionFlag = true;
            }
            else if (args.top().equals("-injars")||args.top().equals("-inpath"))
            {
                // a class-path-delimiter separated list should follow -injars
                parsePath(args.argTo(), in_jars);
            } // injars
            else if (args.top().equals("-sourceroots"))
            {
                // a class-path-delimiter separated list should follow -sourceroots
                parsePath(args.argTo(), source_roots);
            } // sourceroots
            else if (args.top().equals("-outjar")) {
                outputdir = args.argTo();
                outputIsJar = true;
            }
            else if (args.top().equals("-classpath") || args.top().equals("-cp"))
            {
                classpath = args.argTo();
            } // classpath
            else if (args.top().equals("-d"))  // -d flag in abc options
            {
                outputdir = args.argTo();
                outputIsJar = false;
            } // output directory
            else if (args.top().equals("-noImportError"))
                // don't report unresolved imports
            { // nothing to do, because we don't do it anyway.
            }
            else if (args.top().equals("-nested-comments") || args.top().equals("-nested-comments:true")
                    || args.top().equals("-nested-comments:on"))
            {
                //allow nested comments for this compiler run
                abc.main.Debug.v().allowNestedComments = true;
            }
            else if(args.top().equals("-nested-comments:false") || args.top().equals("-nested-comments:off"))
            {
                abc.main.Debug.v().allowNestedComments = false;
            }
            else if (args.top().equals("-time"))
            { abc.main.Debug.v().abcTimer=true;
                abc.main.Debug.v().polyglotTimer=true;
                abc.main.Debug.v().sootResolverTimer=true;
            }

            /* -------- PARTIALLY-IMPLEMENTED AJC-COMPLIANT OPTIONS -------*/
            // abc options that we handle incompletely, but don't
            //    want to stop compilation

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

            // optimization settings
            else if (args.top().equals("-O") || (args.top().equals("-O1")))
                optflag=true;                 //   this is the default
            else if (args.top().equals("-O0")) // -O0 turns opt off
                optflag=false;

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
            else if (args.top().equals("-ext"))
            {
                abcExtensionPackage = args.argTo();
            }
            else if (args.top().equals("-verbose") || args.top().equals("--verbose")) {
                abc.main.Debug.v().verbose=true;
            }
            else if (args.top().startsWith("@") || args.top().equals("-argfile")) {
                String fileName;
                if(args.top().startsWith("@")) {
                    fileName = args.top().substring(1);
                } else {
                    fileName = args.argTo();
                }
                args.shift();
                BufferedReader br;
                try {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
                } catch(IOException e) {
                    throw new IllegalArgumentException("Couldn't open argfile "+fileName);
                }
                LinkedList newArgs = new LinkedList();
                try {
                    while(true) {
                        String s = br.readLine();
                        if(s == null) break;
                        newArgs.addFirst(s);
                    }
                } catch(IOException e) {
                    throw new IllegalArgumentException("Error reading from argfile "+fileName);
                }
                for( Iterator argIt = newArgs.iterator(); argIt.hasNext(); ) {
                    final String arg = (String) argIt.next();
                    args.push(arg);
                }
                // push filename back on, so that after we advance, we're on the
                // first arg from the argfile
                args.push(fileName);
            }

            /* -------------------  SOOT OPTIONS -------------------------*/
            // handle soot-specific options, must be between +soot -soot
            else if (args.top().equals("+soot"))
            { args.shift(); // skip +soot
                while (!args.isEmpty() && !args.top().equals("-soot"))
                { if (args.top().equals("-d")) // get the Soot -d option
                    outputdir = args.argTo();
                    else if (args.top().equals("-O"))
                        optflag = true;
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

            /* ---------  UNKNOWN OPTION ---------------------------------- */
            else if (args.top().startsWith("-"))
            { throw new IllegalArgumentException("Unknown option "+args.top());
            }

            /* ---------  FILE NAME  ---------------------------------- */
            else
            { aspect_sources.add(args.top());
            } // must be file name
        } // for each arg

        // always want line number info
        soot_args.add("-keep-line-number");
        // handle output directory, -d . is default
        soot_args.add("-d");
        soot_args.add(outputdir);
        if(outputIsJar) soot_args.add("-outjar");
        if(optflag) {
            soot_args.add("-O");
        }

        // now we have parsed the arguments we know which AbcExtension
        // to load
        loadAbcExtension(abcExtensionPackage);

        if (noArguments)
        {
            abcPrintVersion();
            throw new CompilerAbortedException("No arguments provided.");
        }

        if (seenVersionFlag)
        {
            abcPrintVersion();
            throw new CompilerAbortedException("Acted on -version option.");
        }
    }


    public void run() throws CompilerFailedException {
        try {
            // Timer start stuff
            Date abcstart = new Date(); // wall clock time start
            if(abc.main.Debug.v().verbose)
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

            loadJars();
            loadSourceRoots();
            AbcTimer.mark("Loading Jars");


            // if something to compile
            compile(); // Timers marked inside compile()
            // The compile method itself aborts if there is an error

            if (!GlobalAspectInfo.v().getWeavableClasses().isEmpty()) {
                weave(); // Timers marked inside weave()

                abortIfErrors();

                if (!abc.main.Debug.v().dontCheckExceptions) {
                    checkExceptions();
                    AbcTimer.mark("Exceptions check");
                }

                abortIfErrors();

                if (Debug.v().doValidate)
                    validate();
                AbcTimer.mark("Validate jimple");

                optimize();

                AbcTimer.mark("Soot Packs");

                output();
                AbcTimer.mark("Soot Writing Output");
            }

            // Timer end stuff
            Date abcfinish = new Date(); // wall clock time finish
            if(abc.main.Debug.v().verbose) {
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
            throw new CompilerFailedException("Compiler failed.");
        }
    }


    public void addJarsToClasspath() {
        StringBuffer sb = new StringBuffer();
        Iterator jari = in_jars.iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            sb.append(jar);
            sb.append(File.pathSeparator);
        }
        sb.append(classpath);

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

        classpath = sb.toString();
    }

    public void initSoot() throws IllegalArgumentException {
        if(!abc.main.Debug.v().verbose)
            soot.G.v().out=new java.io.PrintStream(new java.io.OutputStream() {
                public void write(int b) { }
            });

        getAbcExtension().addJimplePacks();

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

        Scene.v().setSootClassPath(classpath);
        getAbcExtension().addBasicClassesToSoot();
        Scene.v().loadDynamicClasses();

        // FIXME: make ClassLoadException in soot, and catch it here
        // and check what was wrong
        Scene.v().loadBasicClasses();
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
        Iterator rooti = source_roots.iterator();
        while (rooti.hasNext()) {
            String root = (String)rooti.next();
            findSourcesInDir(root, aspect_sources);
        }
    }

    public void loadJars() throws CompilerFailedException {
        // Load the classes in all given jars
        Iterator jari = in_jars.iterator();
        while (jari.hasNext()) {
            String jar = (String)jari.next();
            List/*String*/ this_jar_classes = soot.SourceLocator.v().getClassesUnder(jar);
            jar_classes.addAll(this_jar_classes);
        }
    }

    public void compile() throws CompilerFailedException, IllegalArgumentException {
        // Invoke polyglot
        try {
            abc.aspectj.ExtensionInfo ext =
                getAbcExtension().makeExtensionInfo(jar_classes, aspect_sources);
            Options options = ext.getOptions();
            options.assertions = true;
            options.serialize_type_info = false;
            options.classpath = classpath;
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
            try {
                if (!aspect_sources.isEmpty()) {
                    if (!compiler.compile(aspect_sources)) {
                        throw new CompilerFailedException("Compiler failed.");
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
            abortIfErrors();

            AbcTimer.mark("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

            for( Iterator clsIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clsIt.hasNext(); ) {

                final AbcClass cls = (AbcClass) clsIt.next();
                SootClass scls = cls.getSootClass();
                scls.setApplicationClass();
                Scene.v().loadClass(scls.getName(), SootClass.BODIES);
            }
            AbcTimer.mark("Soot resolving");

            GlobalAspectInfo.v().buildAspectHierarchy();
            AbcTimer.mark("Aspect inheritance");

        } catch (polyglot.main.UsageError e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
        }

        // Output the aspect info
        if (abc.main.Debug.v().aspectInfo)
            GlobalAspectInfo.v().print(System.err);
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
            Scene.v().setDoneResolving();

            // Adjust Soot types for intertype decls
            IntertypeAdjuster ita = new IntertypeAdjuster();
            ita.adjust();
            AbcTimer.mark("Intertype Adjuster");

            // Retrieve all bodies
            for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
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

            PatternMatcher.v().updateWithAllSootClasses();
            // evaluate the patterns the third time (depends on re-resolving)
            PatternMatcher.v().recomputeAllMatches();
            AbcTimer.mark("Update pattern matcher");

            // any references made by itd initialisers will appear in a delegate method,
            // and thus have already been processed by j2j; all resolving ok.
            ita.initialisers(); // weave the field initialisers into the constructors
            AbcTimer.mark("Weave Initializers");

            if (!Debug.v().testITDsOnly) {
                // Make sure that all the standard AspectJ shadow types are loaded
                AbcTimer.mark("Load shadow types");

                // for each shadow in each weavable class, compute list of applicable advice
                GlobalAspectInfo.v().computeAdviceLists();
                AbcTimer.mark("Compute advice lists");

                if(Debug.v().matcherTest) {
                    System.err.println("--- BEGIN ADVICE LISTS ---");
                    // print out matching information for testing purposes
                    for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
                        final AbcClass cl = (AbcClass) clIt.next();
                        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                            final SootMethod method = (SootMethod) methodIt.next();
                            final StringBuffer sb=new StringBuffer(1000);
                            sb.append("method: "+method.getSignature()+"\n");
                            GlobalAspectInfo.v().getAdviceList(method).debugInfo(" ",sb);
                            System.err.println(sb.toString());
                        }
                    }
                    System.err.println("--- END ADVICE LISTS ---");
                }

                Weaver weaver = new Weaver();
                weaver.weave(); // timer marks inside weave() */
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

        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {

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
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            abc.soot.util.Validate.validate(cl.getSootClass());
        }
    }

    public void optimize(){
        PackManager.v().runPacks();
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
}
