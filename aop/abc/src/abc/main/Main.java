
package arc.main;

import soot.*;

import java.util.*;

public class Main extends polyglot.main.Main {
    protected void start(String[] args) {
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
        List/*<String>*/ polyglot_args = new ArrayList();
        polyglot_args.addAll(aspect_sources);
        polyglot_args.add("-extclass");
        polyglot_args.add("arc.aspectj.ExtensionInfo");
        polyglot_args.add("-sx");
        polyglot_args.add("java");
        try {
            super.start((String[]) polyglot_args.toArray(new String[0]));
        } catch (polyglot.main.Main.TerminationException e) {
	    System.out.println("Termination: " + e);
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
