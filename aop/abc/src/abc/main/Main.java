package abc.main;

import soot.*;
import soot.util.*;
import soot.xml.*;

import soot.tagkit.SourceFileTag;
import soot.tagkit.SourceLnPosTag;
import soot.jimple.Stmt;
import soot.jimple.toolkits.base.ExceptionChecker;
import soot.jimple.toolkits.base.ExceptionCheckerError;
import soot.jimple.toolkits.base.ExceptionCheckerErrorReporter;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;
import polyglot.frontend.Stats;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;
import polyglot.util.Position;

import abc.aspectj.visit.PatternMatcher;
import abc.polyglot.util.ErrorInfoFactory;
import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class Main {
    public static final String abcVersionString = "0.1.0";

    private static Main v=null;
    public static Main v() {
	return v;
    }

    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ jar_classes = new ArrayList();
    public Collection/*<String>*/ in_jars = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classpath = System.getProperty("java.class.path");
    public String classes_destdir = ""; // TODO: LJH - fixed with -d option?

    public ErrorQueue error_queue; // For reporting errors and warnings

    private String extinfo_classname = "abc.aspectj.ExtensionInfo";

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
      abc.weaving.aspectinfo.AbcFactory.reset();
      abc.weaving.aspectinfo.GlobalAspectInfo.reset();
      abc.weaving.matching.ShadowType.reset();
      abc.weaving.weaver.AroundWeaver.reset();
      abc.weaving.matching.StmtShadowMatch.reset();
      abc.weaving.matching.ExecutionShadowMatch.reset();

      v=null;
    }

    public static void compilerOptionIgnored(String option, String message)
      { G.v().out.println( "*** Option " + option + " ignored: " + message);
      }

    public static void abcPrintVersion() 
      { G.v().out.println("Abc version " + abcVersionString);
        G.v().out.println("... using Soot toolkit version " + 
                                  soot.Main.v().versionString);
        G.v().out.println("... using Polyglot compiler toolkit version " + 
                                    new polyglot.ext.jl.Version());
        G.v().out.println("For usage,  java abc.main.Main --help");
        G.v().out.println("------------------------------------------");
        G.v().out.println("Abc copyright and license info goes here."); // TODO
        G.v().out.println("------------------------------------------");
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
        else if (args[i].equals("-injars")) 
          {
            // a class-path-delimiter separated list should follow -injars
            if(i + 1 < args.length){
                String[] jars = args[i + 1].split(System.getProperty("path.separator"));
                i++;
                for(int j = 0; j < jars.length; j++) {
                    // Do we need a sanity check here? !jars[j].equals("") or something like that?
                    in_jars.add(jars[j]);
                }
            } else throw new IllegalArgumentException("Missing argument to " + args[i]);
          } // injars 
        // TODO: -inpath PATH
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
                 "abc does not yet support Xlint");
             if (args[i].equals("Xlint") || args[i].equals("Xlint:warning"))
               abc.main.Options.v().Xlint = abc.main.Options.WARNING;
             else if (args[i].equals("Xlint:error"))
               abc.main.Options.v().Xlint = abc.main.Options.ERROR;
             else
               abc.main.Options.v().Xlint = abc.main.Options.IGNORE;
           }

         // -1.3, -1.4
         else if (args[i].equals("-1.3") || args[i].equals("-1.4"))
            compilerOptionIgnored(args[i],
                "abc should be able to handle both 1.3 and 1.4.");

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

	    // The following set of options causes an odd bug with soot -O
	    // to go away - it was eliminating a trap, but not the code
	    // it pointed to, which caused verify errors. 
	    // See tests/people/ganesh/NewSwitch2.java for an example
            soot_args.add("-p");
            soot_args.add("jb.uce");
            soot_args.add("remove-unreachable-traps");
            soot_args.add("-p");
            soot_args.add("jop.uce1");
            soot_args.add("remove-unreachable-traps");
            soot_args.add("-p");
            soot_args.add("jop.uce2");
            soot_args.add("remove-unreachable-traps");
        }
    }


    public void run() throws CompilerFailedException {
		try {
			// Timer start stuff
			Date abcstart = new Date(); // wall clock time start
			G.v().out.println("Abc started on " + abcstart);

			if (soot.options.Options.v().time())
				Timers.v().totalTimer.start(); // Soot timer start

			// Main phases

			AbcTimer.start(); // start the AbcTimer

			addJarsToClasspath();
			initSoot();
			AbcTimer.mark("Init. of Soot");

			loadJars();
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
			G.v().out.print("Abc finished on " + abcfinish + ".");
			long runtime = abcfinish.getTime() - abcstart.getTime();
			G.v().out.println(" ( " + (runtime / 60000) + " min. " + ((runtime % 60000) / 1000) + " sec. )");

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

	Scene.v().setSootClassPath(classpath);

	Scene.v().addBasicClass("org.aspectj.runtime.internal.CFlowStack",SootClass.SIGNATURES);
	Scene.v().addBasicClass("org.aspectj.runtime.reflect.Factory",SootClass.SIGNATURES);
	Scene.v().addBasicClass("org.aspectj.lang.JoinPoint");
	Scene.v().addBasicClass("org.aspectj.lang.JoinPoint$StaticPart");
	Scene.v().addBasicClass("org.aspectj.lang.SoftException",SootClass.SIGNATURES);
	Scene.v().addBasicClass("org.aspectj.lang.NoAspectBoundException");
	Scene.v().addBasicClass("abc.runtime.internal.CFlowCounter",SootClass.SIGNATURES);
	Scene.v().addBasicClass("abc.runtime.reflect.AbcFactory",SootClass.SIGNATURES);

	// FIXME: make ClassLoadException in soot, and catch it here 
	// and check what was wrong

	Scene.v().loadBasicClasses();

    }

    public void loadJars() throws CompilerFailedException {
    // Load the classes in all given jars
    Iterator jari = in_jars.iterator();
    while (jari.hasNext()) {
        String jar = (String)jari.next();
        List/*String*/ this_jar_classes = soot.SourceLocator.v().getClassesUnder(jar);
        for( Iterator classNameIt = this_jar_classes.iterator(); classNameIt.hasNext(); ) {
            final String className = (String) classNameIt.next();
            jar_classes.add(soot.Scene.v().loadClass(className, SootClass.BODIES).getName());
        }
      }

    // Make them all application classes
    Iterator cni = jar_classes.iterator();
    while (cni.hasNext()) {
        String cn = (String)cni.next();
        SootClass sc = Scene.v().getSootClass(cn);
        sc.setApplicationClass();
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

            AbcTimer.mark("Create polyglot compiler");
	    try {
		if (!compiler.compile(aspect_sources)) {
		    throw new CompilerFailedException("Compiler failed.");
		}
	    } finally {
		error_queue = compiler.errorQueue(); 
	    }
		abortIfErrors();

            AbcTimer.mark("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

            GlobalAspectInfo.v().buildAspectHierarchy();
            AbcTimer.mark("Resolve class names");

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
        AbcTimer.mark("Declare Parents");
        
        // Adjust Soot types for intertype decls
        IntertypeAdjuster ita = new IntertypeAdjuster();
        ita.adjust();
        AbcTimer.mark("Intertype Adjuster");

        // Retrieve all bodies
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
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
        AbcTimer.mark("Retrieving bodies");

        PatternMatcher.v().updateWithAllSootClasses();
        PatternMatcher.v().recomputeAllMatches();
        AbcTimer.mark("Update pattern matcher");

        ita.initialisers(); // weave the field initialisers into the constructors
        AbcTimer.mark("Weave Initializers");

        if (!Debug.v().testITDsOnly) {
            // Make sure that all the standard AspectJ shadow types are loaded
            AJShadows.load();
            AbcTimer.mark("Load shadow types");
    
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
    
            //generateDummyGAI();
    
            Weaver weaver = new Weaver();
            weaver.weave(); // timer marks inside weave() */
        }
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
        ExceptionChecker exccheck=new ExceptionChecker(new GotCheckedExceptionError());
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
					   m.declaringClass().getName().equals("org.aspectj.runtime.reflect.Factory"))
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
}
