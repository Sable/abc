package abc.main;

import soot.*;
import soot.util.*;
import soot.xml.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;
import polyglot.frontend.Stats;
import polyglot.types.SemanticException;
import polyglot.util.ErrorQueue;
import polyglot.util.ErrorInfo;

import abc.aspectj.visit.PatternMatcher;

import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.*;

import java.util.*;
import java.io.*;

public class Main {
    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ weavable_classes = new ArrayList();
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
      abc.soot.util.Restructure.reset();
      abc.weaving.aspectinfo.GlobalAspectInfo.reset();
      abc.weaving.weaver.AroundWeaver.reset();
    }
    
    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
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

        soot_args.add("-keep-line-number"); // always want line number info
        // soot_args.add("-xml-attributes"); // FIXME: want to remove this

        for (int i = 0 ; i < args.length ; i++) 
	  { if (args[i].equals("+soot")) 
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
	    else if (args[i].equals("-injars")) 
	      { while (++i < args.length && !args[i].startsWith("-")) 
	          { in_jars.add(args[i]);
                  }
                 i--;
               } // injars 
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
   	     else if (args[i].equals("-O"))  // -O flag in abc options
		 optflag=true;
	     else if (args[i].startsWith("-")) 
	       { throw new IllegalArgumentException("Unknown option "+args[i]);
               } 
	     else 
	       { aspect_sources.add(args[i]);
               } // must be file name
          } // for each arg

        // handle output directory, -d . is default
        soot_args.add("-d");
        soot_args.add(outputdir);
	if(optflag) soot_args.add("-O");
    }

    public void run() throws CompilerFailedException {
        // Timer start stuff
        Date abcstart = new Date(); // wall clock time start
        G.v().out.println("Abc started on " + abcstart);

        if (soot.options.Options.v().time())
          Timers.v().totalTimer.start(); // Soot timer start

        // Main phases
	try {
	    AbcTimer.start(); // start the AbcTimer

	    addJarsToClasspath();
	    initSoot();
	    AbcTimer.mark("Init. of Soot");

	    loadJars();
	    AbcTimer.mark("Loading Jars");

	    compile(); // Timers marked inside compile()

	    weave();   // Timers marked inside weave()

	    optimize();
	    AbcTimer.mark("Soot Packs");

	    output();
	    AbcTimer.mark("Soot Writing Output");
	} catch (SemanticException e) {
	    System.out.println(e.position()+": "+e.getMessage());
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

    public void addJarsToClasspath() {
	StringBuffer sb = new StringBuffer(classpath);
	Iterator jari = in_jars.iterator();
	while (jari.hasNext()) {
	    String jar = (String)jari.next();
	    sb.append(":");
	    sb.append(jar);
	}
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
        // TODO
    }

    public void compile() throws CompilerFailedException, IllegalArgumentException {
        // Invoke polyglot
        try {
            abc.aspectj.ExtensionInfo ext = 
                new abc.aspectj.ExtensionInfo(weavable_classes, aspect_sources);
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
	    error_queue = compiler.errorQueue();

            AbcTimer.mark("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());

            GlobalAspectInfo.v().transformClassNames(ext.hierarchy);
            AbcTimer.mark("Transform class names");

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
        
    public void weave() throws CompilerFailedException, SemanticException {
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

	// Update pattern matcher class hierarchy and recompute pattern matches
	PatternMatcher.v().updateWithAllSootClasses();
	PatternMatcher.v().recomputeAllMatches();
        AbcTimer.mark("Recompute name pattern matches");
        
	// Compute the precedence relation between aspects
	GlobalAspectInfo.v().computePrecedenceRelation();
	AbcTimer.mark("Compute precedence relation");

        ita.initialisers(); // weave the field initialisers into the constructors
        AbcTimer.mark("Weave Initializers");
        
        // Make sure that all the standard AspectJ shadow types are loaded
        AspectJShadows.load();
        AbcTimer.mark("Load shadow types");

        GlobalAspectInfo.v().computeAdviceLists();
        AbcTimer.mark("Compute advice lists");

        if(Debug.v.matcherTest) {
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
        weaver.weave(); // timer marks inside weave()
    }

    public void optimize(){
        PackManager.v().runPacks();
    }
    
    public void output() {
      // Write classes
      PackManager.v().writeOutput();
    }

}
