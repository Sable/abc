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
 * This library is distributed in the hope that it will be useful,
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

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
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
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import abc.aspectj.visit.PatternMatcher;
import abc.polyglot.util.ErrorInfoFactory;
import abc.soot.util.AspectJExceptionChecker;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.weaver.CflowIntraAggregate;
import abc.weaving.weaver.CflowIntraproceduralAnalysis;
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
    public static final String abcVersionString
        = new abc.aspectj.Version().toString();

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

    private String extinfo_classname = "abc.aspectj.ExtensionInfo";

    /** reset all static information so main can be called again */
    public static void reset() {
      soot.G.reset(); // reset all of Soot's global info
      if(!abc.main.Debug.v().verbose)
          soot.G.v().out=new java.io.PrintStream(new java.io.OutputStream() {
                  public void write(int b) { }
              });
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
      abc.weaving.matching.ShadowType.reset();
      abc.weaving.weaver.AroundWeaver.reset();
      abc.weaving.matching.StmtShadowMatch.reset();
      abc.weaving.matching.ExecutionShadowMatch.reset();
      abc.weaving.aspectinfo.GlobalCflowSetupFactory.reset();

      v=null;
    }

    public static void compilerOptionIgnored(String option, String message)
      { G.v().out.println( "*** Option " + option + " ignored: " + message);
      }

    public static void abcPrintVersion()
      { G.v().out.println("abc version " + abcVersionString);
          // FIXME: should print out any extension version
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

  public void parseArgs(String[] args) throws IllegalArgumentException, CompilerAbortedException {
    String outputdir=".";
    boolean optflag=true;
    if (args.length == 0)
      { abcPrintVersion();
        throw new CompilerAbortedException("No arguments provied.");
      }

    for (int i = 0 ; i < args.length ; i++)
      { /* --------FULLY IMPLEMENTED AJC-COMPLIANT OPTIONS ----------*/
        // abc options that we handle completely,
        //     and correspond to ajc options

        // TODO: -help needs to be filled in
        if (args[i].equals("-help") || args[i].equals("--help") ||
            args[i].equals("-h"))
           { abc.main.Usage.abcPrintHelp();
                throw new CompilerAbortedException("Acted on -help option.");
           }
        else if (args[i].equals("-version") || args[i].equals("--version") ||
            args[i].equals("-v"))
          { abcPrintVersion();
                throw new CompilerAbortedException("Acted on -version option.");
          }
        else if (args[i].equals("-injars")||args[i].equals("-inpath"))
          {
            // a class-path-delimiter separated list should follow -injars
            i++;
            if(i < args.length){
                parsePath(args[i], in_jars);
            } else throw new IllegalArgumentException("Missing argument to " + args[i]);
          } // injars
        else if (args[i].equals("-sourceroots"))
          {
            // a class-path-delimiter separated list should follow -sourceroots
            i++;
            if(i < args.length){
                parsePath(args[i], source_roots);
            } else throw new IllegalArgumentException("Missing argument to " + args[i]);
          } // sourceroots
        // TODO: -argfile File
        // TODO: -outjar output.jar
        else if (args[i].equals("-classpath") || args[i].equals("-cp"))
          { if (i+1 < args.length)
              { classpath = args[i+1];
                    i++;
              }
            else
              { throw new IllegalArgumentException
                  ("Missing argument to "+args[i]);
              }
           } // classpath
         else if (args[i].equals("-d"))  // -d flag in abc options
           { if (i+1 < args.length)
               { outputdir = args[i+1];
                 i++;
               }
             else
               throw new IllegalArgumentException(
                        "Missing argument to " + args[i]);
           } // output directory
         else if (args[i].equals("-noImportError"))
           // don't report unresolved imports
           { // nothing to do, because we don't do it anyway.
           }
         else if (args[i].equals("-nested-comments") || args[i].equals("-nested-comments:true")
                 || args[i].equals("-nested-comments:on"))
         {
             //allow nested comments for this compiler run
             abc.main.Debug.v().allowNestedComments = true;
         }
         else if(args[i].equals("-nested-comments:false") || args[i].equals("-nested-comments:off"))
         {
             abc.main.Debug.v().allowNestedComments = false;
         }
         else if (args[i].equals("-time"))
           { abc.main.Debug.v().abcTimer=true;
             abc.main.Debug.v().polyglotTimer=true;
             abc.main.Debug.v().sootResolverTimer=true;
           }

         /* -------- PARTIALLY-IMPLEMENTED AJC-COMPLIANT OPTIONS -------*/
         // abc options that we handle incompletely, but don't
         //    want to stop compilation

         // -Xlint, -Xlint:ignore, -Xlint:warning, -Xlint:errror
         else if (args[i].equals("-Xlint") ||
                  args[i].equals("-Xlint:warning") ||
                  args[i].equals("-Xlint:error") ||
                  args[i].equals("-Xlint:ignore"))
           { compilerOptionIgnored(args[i],
                 "abc does not support Xlint");
             if (args[i].equals("Xlint") || args[i].equals("Xlint:warning"))
               abc.main.Options.v().Xlint = abc.main.Options.WARNING;
             else if (args[i].equals("Xlint:error"))
               abc.main.Options.v().Xlint = abc.main.Options.ERROR;
             else
               abc.main.Options.v().Xlint = abc.main.Options.IGNORE;
           }

         // -1.3, -1.4
         else if (args[i].equals("-1.3"))
             abc.main.Debug.v().java13=true;
         else if (args[i].equals("-1.4"))
             abc.main.Debug.v().java13=false;

         // -target 1.1,  -target 1.2,  -target 1.3, -target 1.4
         else if (args[i].equals("-target"))
           { i++;
             if (args[i].equals("1.1") || args[i].equals("1.2") ||
                 args[i].equals("1.3") || args[i].equals("1.4"))
               compilerOptionIgnored("-target " + args[i],
                  "abc-generated code should run on any 1.1 - 1.4 VM.");
             else
               compilerOptionIgnored("-target " + args[i],
                   args[i] + " is not a known target number.");
           }

         // -source 1.3,  -source 1.4
         else if (args[i].equals("-source"))
           { i++;
             if (args[i].equals("1.3"))
               compilerOptionIgnored("-source 1.3",
                   "abc treats asserts as keywords as in 1.4");
             else if (args[i].equals("1.4"))
               { // that's what we do ... so ok
               }
             else
               compilerOptionIgnored("-source " + args[i],
                   args[i] + " is not a known source number.");
           }

         // -nowarn, -warn:items  where items is a comma-delmited list
         else if (args[i].equals("-nowarn"))
           { // TODO: remove following line when compiler looks at flag
             compilerOptionIgnored(args[i], "warnings not disabled.");
             abc.main.Options.v().warn = abc.main.Options.NOWARNINGS;
           }
         else if (args[i].startsWith("-warn:"))
          { // TODO: remove following line when compiler looks at flag
            compilerOptionIgnored(args[i],
                        " warning flags not implemented yet.");
            // special case of -warn:none
            if (args[i].equals("-warn:none"))
                abc.main.Options.v().warn = abc.main.Options.NOWARNINGS;
            else
              { String kindList = args[i].substring(6); // strip off "-warn:"
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
         else if (args[i].equals("-g"))
           compilerOptionIgnored(args[i],
              "abc does not yet support creating debug info.");
         else if (args[i].startsWith("-g:"))
           compilerOptionIgnored(args[i],
              "abc does not yet support creating debug info");

         /* -------- ABC-SPECIFIC OPTIONS, NO AJC EQUIVALENTS ----------*/
         // abc-specific options which have no ajc equivalents

         // TODO: should actually list only soot options useful for abc
         else if (args[i].equals("-help:soot"))
           { G.v().out.println(soot.options.Options.v().getUsage());
                throw new CompilerAbortedException("Acted on -help:soot option.");
           }

        // TODO; should actually list only polyglot options useful for abc
        else if (args[i].equals("-help:polyglot"))
          { abc.aspectj.ExtensionInfo ext =
                new abc.aspectj.ExtensionInfo(null, null);
            Options options = ext.getOptions();
            options.usage(G.v().out);
            throw new CompilerAbortedException("Acted on -help:polyglot option.");
          }

         // optimization settings
         else if (args[i].equals("-O") || (args[i].equals("-O1")))
           optflag=true;                 //   this is the default
         else if (args[i].equals("-O0")) // -O0 turns opt off
           optflag=false;

         // -debug and -nodebug flags in abc options
         else if (args[i].equals("-debug") || args[i].equals("-nodebug"))
           { if (i+1 < args.length)
               { String debug_name = args[i+1];
                 try {
                   Field f = abc.main.Debug.class.getField(debug_name);
                   f.setBoolean(abc.main.Debug.v(), args[i].equals("-debug"));
                 }
                 catch (NoSuchFieldException e) {
                   throw new IllegalArgumentException(
                                    "No such debug option: "+debug_name);
                 } catch (Exception e) {
                   throw new InternalCompilerError(
                       "Error setting debug field "+debug_name, e);
                 }
                 i++;
                }
              else
                throw new IllegalArgumentException(
                    "Missing argument to " + args[i]);
           }
         else if (args[i].equals("-ext"))
           {
                i++;
                if (i < args.length) {
                    extinfo_classname = args[i];
                } else {
                    throw new IllegalArgumentException(
                         "Missing argument to  " + args[i-1]);
                }
           }
         else if (args[i].equals("-v")) {
             abc.main.Debug.v().verbose=true;
         }

         /* -------------------  SOOT OPTIONS -------------------------*/
         // handle soot-specific options, must be between +soot -soot
         else if (args[i].equals("+soot"))
           { i++; // skip +soot
             while (i < args.length && !args[i].equals("-soot"))
               { if (args[i].equals("-d")) // get the Soot -d option
                   { if (i+1 < args.length)
                       { outputdir = args[i+1];
                         i++ ;
                       }
                     else
                       throw new IllegalArgumentException(
                            "Missing argument to " + args[i]);
                    }
                 else if (args[i].equals("-O"))
                   optflag = true;
                 else
                   if(!args[i].equals("-keep-line-number") &&
                        !args[i].equals("-xml-attributes"))
                      soot_args.add(args[i]);
                     i++;
               }
             if (i >= args.length )
               throw new IllegalArgumentException("no matching -soot found");
           } // +soot .... -soot optoins

        /* -------------------  POLYGLOT OPTIONS -------------------------*/
        // handle polyglot-specific options,
        //   must be between +polyglot -polyglot
        else if (args[i].equals("+polyglot"))
          { i++; // skip +polyglot
            while (i < args.length && !args[i].equals("-polyglot"))
              { polyglot_args.add(args[i]);
                i++;
              }
            if (i >= args.length)
              throw new IllegalArgumentException(
                               "no matching -polyglot found");
          } // +polyglot ... -polyglot options

         /* ---------  UNKNOWN OPTION ---------------------------------- */
         else if (args[i].startsWith("-"))
           { throw new IllegalArgumentException("Unknown option "+args[i]);
               }

         /* ---------  FILE NAME  ---------------------------------- */
         else
           { aspect_sources.add(args[i]);
           } // must be file name
        } // for each arg

        // always want line number info
        soot_args.add("-keep-line-number");
        // handle output directory, -d . is default
        soot_args.add("-d");
        soot_args.add(outputdir);
        if(optflag) {
            soot_args.add("-O");
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
                } catch (InternalCompilerError e) {
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
        String[] soot_argv = (String[]) soot_args.toArray(new String[0]);
        //System.out.println(classpath);
        if (!soot.options.Options.v().parse(soot_argv)) {
            throw new IllegalArgumentException("Soot usage error");
        }

        PackManager.v().getPack("jtp").add(new Transform("jtp.uce", UnreachableCodeEliminator.v()));


        InitialResolver.v().setJBBFactory(new AbstractJBBFactory(){
            protected AbstractJimpleBodyBuilder createJimpleBodyBuilder(){
                JimpleBodyBuilder jbb = new JimpleBodyBuilder();
                AccessFieldJBB afjbb = new AccessFieldJBB();
                afjbb.ext(jbb);
                return afjbb;
            }
        });
        Scene.v().setSootClassPath(classpath);

        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowStack",SootClass.SIGNATURES);
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.reflect.Factory",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint");
        Scene.v().addBasicClass("org.aspectj.lang.JoinPoint$StaticPart");
        Scene.v().addBasicClass("org.aspectj.lang.SoftException",SootClass.SIGNATURES);
        Scene.v().addBasicClass("org.aspectj.lang.NoAspectBoundException");
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.runtime.internal.CFlowCounter",SootClass.SIGNATURES);

        // FIXME: move this to EAJ
        Scene.v().addBasicClass("uk.ac.ox.comlab.abc.eaj.runtime.reflect.EajFactory",SootClass.SIGNATURES);

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
                loadExtensionInfo(jar_classes, aspect_sources);
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
                if (!compiler.compile(aspect_sources)) {
                    throw new CompilerFailedException("Compiler failed.");
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
            AJShadows.load();
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

        // FIXME - find a better place for adding this; want to be sure it'll be in the list precisely
        // once, even when running the test harness

        // Add a null check eliminator that knows about abc specific stuff
        soot.jimple.toolkits.annotation.nullcheck.NullCheckEliminator.AnalysisFactory f
            =new soot.jimple.toolkits.annotation.nullcheck.NullCheckEliminator.AnalysisFactory() {
                    public soot.jimple.toolkits.annotation.nullcheck.BranchedRefVarsAnalysis newAnalysis
                        (soot.toolkits.graph.UnitGraph g) {
                        return new soot.jimple.toolkits.annotation.nullcheck.BranchedRefVarsAnalysis(g) {
                                public boolean isAlwaysNonNull(Value v) {
                                    if(super.isAlwaysNonNull(v)) return true;
                                    if(v instanceof soot.jimple.InvokeExpr) {
                                        soot.jimple.InvokeExpr ie=(soot.jimple.InvokeExpr) v;
                                        soot.SootMethodRef m=ie.getMethodRef();
                                        if(m.name().equals("makeJP") &&
                                           m.declaringClass().getName().equals
                                           ("uk.ac.ox.comlab.abc.runtime.reflect.Factory"))
                                            return true;
                                    }
                                    return false;
                                }
                            };
                    }
                };
        // want this to run before Dead assignment eliminiation
        PackManager.v()
            .getPack("jop")
            .insertBefore(new Transform("jop.nullcheckelim", new soot.jimple.toolkits.annotation.nullcheck.NullCheckEliminator(f)),
                          "jop.dae");

        if (Debug.v().cflowIntraAnalysis) {
                // Cflow Intraprocedural Analysis
                // Two phases:
                //              - Collapse all the local vars assigned to the same CflowStack/CflowCounter field
                //                to the same var, only needs to be assigned once
                //              - Get the stack/counter for the current thread for each of these at the beginning
                //                of the method to avoid repeated currentThread()s
                //                NOTE This could have a negative performance impact, something better might be
                //                in order
        PackManager.v().getPack("jop")
        .insertBefore(new Transform("jop.cflowintra", CflowIntraproceduralAnalysis.v()),
                                        "jop.dae");
        // Before running the cflow intraprocedural, need to aggregate cflow vars
        PackManager.v().getPack("jop")
        .insertBefore(new Transform("jop.cflowaggregate", CflowIntraAggregate.v()),
                                        "jop.cflowintra");
        }

        PackManager.v().runPacks();
    }

    public void output() {
      // Write classes
      PackManager.v().writeOutput();
    }

    private abc.aspectj.ExtensionInfo loadExtensionInfo(Collection jar_classes, Collection aspect_sources)
    {
        Class[] types = new Class[] { Collection.class, Collection.class };
        Object[] args = new Object[] { jar_classes, aspect_sources };

        Class extinfo;
        Constructor cons;
        abc.aspectj.ExtensionInfo extensionInfoInstance;

        try {
            extinfo = Class.forName(extinfo_classname);
            cons = extinfo.getConstructor(types);
            extensionInfoInstance = (abc.aspectj.ExtensionInfo) cons.newInstance(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot load extension class " + extinfo_classname);
        }

        return extensionInfoInstance;
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
