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

import abc.weaving.matching.StmtAdviceApplication;
import abc.weaving.matching.StmtShadowMatch;
import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.*;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

public class Main {
    public static final String abcVersionString = "0.1.0";

    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ jar_classes = new ArrayList();
    public Collection/*<String>*/ in_jars = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classpath = System.getProperty("java.class.path");
    public String classes_destdir = ""; // TODO: LJH - fixed with -d option?

    public ErrorQueue error_queue; // For reporting errors and warnings

    /** reset all static information so main can be called again */
    public static void reset() {
      soot.G.reset(); // reset all of Soot's global info
      // TODO: add a call here to the reset method for any class that
      //  needs static information reset for repeated calls to main
      abc.main.AbcTimer.reset();
      abc.main.Options.reset();
      abc.soot.util.Restructure.reset();
      abc.aspectj.visit.PCStructure.reset();
      abc.aspectj.visit.AspectInfoHarvester.reset();
      abc.weaving.aspectinfo.GlobalAspectInfo.reset();
      abc.weaving.matching.ShadowType.reset();
      abc.weaving.weaver.AroundWeaver.reset();
      abc.weaving.matching.StmtShadowMatch.reset();
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
        G.v().out.println("Abc copyright and license info goes here."); // TODO
      }
    
    public static void abcPrintHelp()
      { G.v().out.println("abc options here");
      }

    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
            System.out.println("Illegal arguments: "+e.getMessage());
            System.exit(1);
        } catch (CompilerFailedException e) {
            System.out.println(e.getMessage());
            System.exit(5);
        }
    }

  public Main(String[] args) throws IllegalArgumentException {
     parseArgs(args);
  }

  public void parseArgs(String[] args) throws IllegalArgumentException {
    String outputdir=".";
    boolean optflag=false;
    if (args.length == 0)
      abcPrintVersion();

    for (int i = 0 ; i < args.length ; i++) 
      { /* --------FULLY IMPLEMENTED AJC-COMPLIANT OPTIONS ----------*/
        // abc options that we handle completely, 
        //     and correspond to ajc options
            
        // TODO: -help needs to be filled in 
        if (args[i].equals("-help") || args[i].equals("--help") ||
            args[i].equals("-h"))
          abcPrintHelp();
        else if (args[i].equals("-version") || args[i].equals("--version") ||
            args[i].equals("-v")) 
          { abcPrintVersion();
          }
        else if (args[i].equals("-injars")) 
          { while (++i < args.length && !args[i].startsWith("-")) 
              { in_jars.add(args[i]);
              }
             i--;
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
           G.v().out.println(soot.options.Options.v().getUsage());

        // TODO; should actually list only polyglot options useful for abc
        else if (args[i].equals("-help:polyglot")) 
          { abc.aspectj.ExtensionInfo ext = 
                new abc.aspectj.ExtensionInfo(null, null);
            Options options = ext.getOptions();
            options.usage(G.v().out);
          }

         else if (args[i].equals("-O"))  // -O flag in abc options
           optflag=true;

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
        if(optflag) soot_args.add("-O");
    }


    public void run() throws CompilerFailedException {
        //System.out.println(classpath);
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

        if (!GlobalAspectInfo.v().getWeavableClasses().isEmpty())
          { weave();   // Timers marked inside weave()

            abortIfErrors();

            if(!abc.main.Debug.v().dontCheckExceptions) 
              { checkExceptions();
                AbcTimer.mark("Exceptions check");
              }
        
            abortIfErrors();

            if (Debug.v().doValidate) validate();
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
        G.v().out.println( " ( " + (runtime / 60000)
                + " min. " + ((runtime % 60000) / 1000) + " sec. )");

        // Print out Soot time stats, if Soot -time flag on.   
        if (soot.options.Options.v().time())
          { Timers.v().totalTimer.end();
            Timers.v().printProfilingInformation();
          }

        // Print out abc timer information
        AbcTimer.report();
    }

    private void abortIfErrors() throws CompilerFailedException {
        if(error_queue.hasErrors()) {
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
        sb.append(":");
      }
      sb.append(classpath);
      classpath = sb.toString();
    }

    public void initSoot() throws IllegalArgumentException {
        Scene.v().setSootClassPath(classpath);
        String[] soot_argv = (String[]) soot_args.toArray(new String[0]);
        //System.out.println(classpath);
        if (!soot.options.Options.v().parse(soot_argv)) {
            throw new IllegalArgumentException("Soot usage error");
        }
    }

    public void loadJars() throws CompilerFailedException {
    // Load the classes in all given jars
    Iterator jari = in_jars.iterator();
    while (jari.hasNext()) {
        String jar = (String)jari.next();
        List jar_classes = soot.SourceLocator.v().resolveClassesUnder(jar);
        jar_classes.addAll(jar_classes);
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
                new abc.aspectj.ExtensionInfo(jar_classes, aspect_sources);
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

            if (!compiler.compile(aspect_sources)) {
                throw new CompilerFailedException("Compiler failed.");
            }
            error_queue = compiler.errorQueue(); // should be empty

            AbcTimer.mark("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

            GlobalAspectInfo.v().resolveClassNames();
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
                if( !method.isConcrete() ) continue;
                // System.out.println("retrieve "+method+ " from "+cl);
                method.retrieveActiveBody();
            }
        }
        AbcTimer.mark("Retrieving bodies");

        ita.initialisers(); // weave the field initialisers into the constructors
        AbcTimer.mark("Weave Initializers");

        if (!Debug.v().testITDsOnly) {
            // Make sure that all the standard AspectJ shadow types are loaded
            AspectJShadows.load();
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
    } catch(SemanticException e) {
        error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,e.getMessage(),e.position());
    }
    }

    private class GotCheckedExceptionError implements ExceptionCheckerErrorReporter {
        public void reportError(ExceptionCheckerError err) {
            SootClass exctype=err.excType();
        
            String message="The exception "+exctype+" must be either caught "+
                "or declared to be thrown";
            Stmt stmt=err.throwing();
            Position pos=null;
            if(err.method().getDeclaringClass().hasTag("SourceFileTag")) {
                SourceFileTag sfTag=(SourceFileTag) err.method()
            .getDeclaringClass().getTag("SourceFileTag");
                if(stmt.hasTag("SourceLnPosTag")) {
                    SourceLnPosTag slpTag=(SourceLnPosTag) stmt.getTag("SourceLnPosTag");
                    pos=new Position(sfTag.getSourceFile(),
                                     slpTag.startLn(),slpTag.startPos(),
                                     slpTag.endLn(),slpTag.endPos());
                } else {
                    pos=new Position(sfTag.getSourceFile());
                    message+=" in method "+err.method();
                }
            } else {
                message+=" in method "+err.method()
                    +" in class "+err.method().getDeclaringClass();
            }

            if(pos==null) error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,message);
            else error_queue.enqueue(ErrorInfo.SEMANTIC_ERROR,message,pos);
        }
    }

    public void checkExceptions() {
        ExceptionChecker exccheck=new ExceptionChecker(new GotCheckedExceptionError());
        HashMap options=new HashMap();
        options.put("enabled","true");

        for(Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); 
            clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            
            for(Iterator methodIt=cl.getSootClass().getMethods().iterator(); 
                methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();

                if(!method.isConcrete()) continue;
                if(method.getName().equals(SootMethod.staticInitializerName)) 
                    continue;

                //FIXME: is "jtp.jec" sensible?
                exccheck.transform(method.getActiveBody(),"jtp.jec",options);

            }
        }
    }

    public void validate() {
    for(Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
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

    
}
