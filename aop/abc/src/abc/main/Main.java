
package abc.main;

import soot.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;

import abc.weaving.weaver.*;
import abc.weaving.aspectinfo.*;

import java.util.*;

public class Main {
    public static void main(String[] args) {
	// Parse args to find aware and self-contained classes.

	String classpath = System.getProperty("java.class.path");
	//System.out.println(classpath);

        Collection/*<String>*/ aspect_sources = Arrays.asList(args); //FIXME
        Collection/*<String>*/ java_sources = new ArrayList(); //FIXME
        String classes_destdir = ""; //FIXME
        Collection/*<String>*/ weavable_classes = new ArrayList(); //FIXME
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            weavable_classes.add(args[i].substring(0, args[i].lastIndexOf('.')));
        }

	Scene.v().setSootClassPath(classpath);
	//Scene.v().loadClassAndSupport("java.lang.Object");

	// TODO: Resolve java classes

	// Invoke polyglot
	ExtensionInfo ext = new abc.aspectj.ExtensionInfo();
	Options options = ext.getOptions();
        options.assertions = true;
	options.serialize_type_info = false;
	options.classpath = classpath;
	Options.global = options;
	Compiler compiler = new Compiler(ext);
	if (!compiler.compile(aspect_sources)) {
	    System.out.println("Compiler failed.");
	    System.exit(5);
	}

	// We should now have all classes as jimple
	
        generateDummyGAI();

        Weaver weaver = new Weaver();
        weaver.weave();

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
    public static void generateDummyGAI() {
        for( Iterator clIt = Scene.v().getApplicationClasses().iterator(); clIt.hasNext(); ) {
            final SootClass cl = (SootClass) clIt.next();
            G.v().out.println( "generating dummy gai: "+cl.toString() );
            if( isAspect(cl) ) {
                System.out.println( "it's an aspect");
		final Aspect aspect=new Aspect(new AbcClass(cl.getName()),null,null);
		GlobalAspectInfo.v().addAspect(aspect);
		GlobalAspectInfo.v().addAdviceDecl(new AdviceDecl(null,new SetField(null),null,aspect,null));
            } else {
                System.out.println( "it's not an aspect");
		GlobalAspectInfo.v().addClass(new AbcClass(cl.getName()));
            }
        }   
    }
     private static boolean isAspect( SootClass cl ) {
         if( cl.getName().equals( "Aspect" ) ) return true;
         return false;
     }


    
}
