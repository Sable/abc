
package abc.main;

import soot.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;

import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.*;

import java.util.*;

public class Main {
    public Collection/*<String>*/ aspect_sources = new ArrayList();
    public Collection/*<String>*/ weavable_classes = new ArrayList();
    public Collection/*<String>*/ in_jars = new ArrayList();

    public List/*<String>*/ soot_args = new ArrayList();
    public List/*<String>*/ polyglot_args = new ArrayList();

    public String classpath = System.getProperty("java.class.path");
    public String classes_destdir = ""; //FIXME

    public static void main(String[] args) {
	try {
	    Main main = new Main(args);
	    main.run();
	} catch (IllegalArgumentException e) {
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
	for (int i = 0 ; i < args.length ; i++) {
	    if (args[i].equals("+soot")) {
		while (++i < args.length && !args[i].equals("-soot")) {
		    soot_args.add(args[i]);
		}
	    } else if (args[i].equals("+polyglot")) {
		while (++i < args.length && !args[i].equals("-polyglot")) {
		    polyglot_args.add(args[i]);
		}
	    } else if (args[i].equals("-injars")) {
		while (++i < args.length && !args[i].startsWith("-")) {
		    in_jars.add(args[i]);
		}
		i--;
	    } else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
		if (i+1 < args.length) {
		    classpath = args[i+1];
		    i++;
		} else {
		    throw new IllegalArgumentException("Missing argument to "+args[i]);
		}
	    } else if (args[i].startsWith("-")) {
		throw new IllegalArgumentException("Unknown option "+args[i]);
	    } else {
		aspect_sources.add(args[i]);
	    }
	}
    }

    public void run() throws CompilerFailedException {
	initSoot();
	loadJars();
	compile();
	weave();
	output();
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
	    ExtensionInfo ext = new abc.aspectj.ExtensionInfo(weavable_classes);
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
	    Compiler compiler = new Compiler(ext);
	    if (!compiler.compile(aspect_sources)) {
		throw new CompilerFailedException("Compiler failed.");
	    }
	} catch (polyglot.main.UsageError e) {
	    throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
	}

	// Output the aspect info
	GlobalAspectInfo.v().print(System.err);
    }

    public void weave() throws CompilerFailedException {
        // Adjust Soot types for intertype decls
        new IntertypeAdjuster().adjust();

        // retrieve all bodies
        for( Iterator clIt = GlobalAspectInfo.v().getWeavableClasses().iterator(); clIt.hasNext(); ) {
            final AbcClass cl = (AbcClass) clIt.next();
            for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
                final SootMethod method = (SootMethod) methodIt.next();
                if( !method.isConcrete() ) continue;
                method.retrieveActiveBody();
            }
        }
        // We should now have all classes as jimple

        GlobalAspectInfo.v().computeAdviceLists();
	
        //generateDummyGAI();

        Weaver weaver = new Weaver();
        weaver.weave();
    }

    public void output() {
	// Write classes

        Iterator/*<String>*/ wci = weavable_classes.iterator();
        while (wci.hasNext()) {
            String wc = (String) wci.next();
            System.out.println(wc);
            SootClass sc = Scene.v().getSootClass(wc);
	    Iterator mi = sc.getMethods().iterator();
	    while (mi.hasNext()) {
		SootMethod m = (SootMethod)mi.next();
		m.retrieveActiveBody();
	    }
            Printer.v().write(sc, classes_destdir);
        }
    }

    // Dummy code to be removed when matcher builds real GAI info
    // Redundant now, I hope!
    /* 
    public static void generateDummyGAI() {
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            G.v().out.println( "generating dummy gai: "+cl.toString() );
            if( isAspect(cl) ) {
                System.out.println( "it's an aspect");
		final Aspect aspect=new Aspect(new AbcClass(cl.getName()),null,null);
		GlobalAspectInfo.v().addAspect(aspect);
		GlobalAspectInfo.v().addAdviceDecl(
                        new AdviceDecl(
                            new BeforeAdvice(null),
                            new ShadowPointcut(new SetField(null),null),
                            new MethodSig(
				0,
                                aspect.getInstanceClass(),
                                new AbcType( aspect.getInstanceClass().getSootClass().getType() ),
                                "before$1",
                                new ArrayList(),
                                new ArrayList(),
                                null),
                            aspect,
			    -1,-1,-1,
                            null));
            } else {
                System.out.println( "it's not an aspect");
		GlobalAspectInfo.v().addClass(new AbcClass(cl.getName()));
            }
        }   
    }
    */
    private static boolean isAspect( SootClass cl ) {
	if( cl.getName().equals( "Aspect" ) ) return true;
	return false;
    }


    
}
