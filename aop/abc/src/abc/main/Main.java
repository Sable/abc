
package arc.main;

import soot.*;

import polyglot.frontend.Compiler;
import polyglot.frontend.ExtensionInfo;
import polyglot.main.Options;

import java.util.*;

public class Main {
    public static void main(String[] args) {
	// Parse args to find aware and self-contained classes.

        Collection/*<String>*/ aspect_sources = Arrays.asList(args); //FIXME
        Collection/*<String>*/ java_sources = new ArrayList(); //FIXME
        String classes_destdir = ""; //FIXME
        Collection/*<String>*/ weavable_classes = new ArrayList(); //FIXME
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            weavable_classes.add(args[i].substring(0, args[i].indexOf('.')));
        }

	// TODO: Resolve java classes

	// Invoke polyglot
	ExtensionInfo ext = new arc.aspectj.ExtensionInfo();
	Options.global = ext.getOptions();
	Compiler compiler = new Compiler(ext);
	if (!compiler.compile(aspect_sources)) {
	    System.out.println("Compiler failed.");
	    System.exit(5);
	}

	// We should now have all classes as jimple
	
	// TODO: WEAVE!

	// Write classes

        Iterator/*<String>*/ wci = weavable_classes.iterator();
        while (wci.hasNext()) {
            String wc = (String) wci.next();
            System.out.println(wc);
            SootClass sc = Scene.v().getSootClass(wc);
            Printer.v().write(sc, classes_destdir);
        }
    }
    
}
