package abc.main;

import soot.*;
import soot.util.*;
import soot.xml.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;
import polyglot.frontend.Stats;

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
    public String classes_destdir = ""; //FIXME

    /** reset all static information so main can be called again */
    public static void reset() {
      soot.G.reset(); // reset all of Soots global info
      // TODO: add a call here to the reset method for any class that
      //  needs staic information reset for repeated calls to main
      abc.main.AbcTimer.reset();
      abc.soot.util.Restructure.reset();
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

        soot_args.add("-keep-line-number"); // always want line number info
        soot_args.add("-xml-attributes"); // FIXME: want to remove this

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
    }

    public void run() throws CompilerFailedException {
        // Timer start stuff
        Date abcstart = new Date(); // wall clock time start
        long abcstart_time = System.currentTimeMillis(); // java timer

        Timers.v().totalTimer.start(); // Soot timer start
        G.v().out.println("Abc started on " + abcstart);

        // Main phases
        AbcTimer.start(); // start the AbcTimer

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

        // Timer end stuff
        Date abcfinish = new Date(); // wall clock time finish
        long abcfinish_time = System.currentTimeMillis(); // java timer
        G.v().out.println("Abc finished on " + abcfinish);
        long runtime = abcfinish.getTime() - abcstart.getTime();
        G.v().out.println( "Abc has run for " + (runtime / 60000)
                + " min. " + ((runtime % 60000) / 1000) + " sec. (wall clock)");
        G.v().out.println("Elapsed time is " + 
                          (abcfinish_time - abcstart_time) + 
                          " milliseconds.");

        // Print out Soot time stats, if Soot -time flag on.   
        Timers.v().totalTimer.end();
        if (soot.options.Options.v().time())
          Timers.v().printProfilingInformation();

        // Print out abc timer information
        AbcTimer.report();
    }

    public void initSoot() throws IllegalArgumentException {
        Scene.v().setSootClassPath(classpath);
        String[] soot_argv = (String[]) soot_args.toArray(new String[0]);
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
                new abc.aspectj.ExtensionInfo(weavable_classes);
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

            AbcTimer.mark("Polyglot phases");
            AbcTimer.storePolyglotStats(ext.getStats());
            GlobalAspectInfo.v().transformClassNames(ext.hierarchy);
            AbcTimer.mark("Transform class names");
        } catch (polyglot.main.UsageError e) {
            throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
        }

        // Output the aspect info
        // GlobalAspectInfo.v().print(System.err);
    }

        protected Compiler createCompiler(ExtensionInfo ext) {
                return new Compiler(ext);
        }
        
    public void weave() throws CompilerFailedException {
        // Perform the declare parents
        new DeclareParentsWeaver().weave();

        // Adjust Soot types for intertype decls
        IntertypeAdjuster ita = new IntertypeAdjuster();
        ita.adjust();
        AbcTimer.mark("Intertype Adjuster");

        // retrieve all bodies
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                if( !method.isConcrete() ) continue;
                method.retrieveActiveBody();
            }
        }
        
        AbcTimer.mark("Retrieving bodies");
        ita.initialisers(); // weave the field initialisers into the constructors
        AbcTimer.mark("Weave Initializers");
        
        // We should now have all classes as jimple

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
