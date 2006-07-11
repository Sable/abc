package jastadd;

import AST.*;

import java.util.*;
import java.io.*;
import parser.*;

import AST.List;

public class JastAdd {
  public static void main(String args[]) {
    try {
      if(!compile(args))
        System.exit(1);
    } catch (Error e) {
      e.printStackTrace();
    }
  }

  public static boolean compile(String args[]) {
    Program program = new Program();
    program.initOptions();
    program.addKeyValueOption("-classpath");
    program.addKeyValueOption("-sourcepath");
    program.addKeyValueOption("-bootclasspath");
    program.addKeyValueOption("-extdirs");
    program.addKeyValueOption("-d");
    program.addKeyOption("-verbose");
    program.addKeyOption("-version");
    program.addKeyOption("-help");

    program.addKeyOption("-no_cache_cycle");
    program.addKeyOption("-no_visit_check");
    program.addKeyOption("-no_component_check");
    program.addKeyValueOption("-package");
    
    program.addOptions(args);
    Collection files = program.files();
    
    if(program.hasOption("-version")) {
      printVersion();
      return false;
    }
    if(program.hasOption("-help") || files.isEmpty()) {
      printUsage();
      return false;
    }

    for(Iterator iter = files.iterator(); iter.hasNext(); ) {
      String name = (String)iter.next();
      if(name.endsWith(".java") || name.endsWith(".jrag") || name.endsWith(".jadd") || name.endsWith(".ast"))
        program.addSourceFile(name);
    }

    // Force loading of all classes and aspects prior to analysis
    // Inter type declarations may be declared in any aspect
    // and need to be loaded prior to name analysis
    for(Iterator iter = program.compilationUnitIterator(); iter.hasNext();  ) {
      iter.next();
    }

    if(Program.verbose()) {
      System.out.println("**** Before ErrorCheck and Transformation ****");
      System.out.println(program.toString());
    }

    if(program.errorCheck())
      return false;
    
    program.generateIntertypeDecls();
    program.java2Transformation();

    if(Program.verbose()) {
      System.out.println("**** After Transformation ****");
      System.out.println(program.toString());
    }

    program.generateClassfile();
    return true;
  }
  
  protected static void printUsage() {
    printVersion();
    System.out.println(
      "\nJastAdd\n\n" +
      "Usage: java JastAdd <options> <source files>\n" +
      "  -verbose                  Output messages about what the compiler is doing\n" +
      "  -classpath <path>         Specify where to find user class files\n" +
      "  -sourcepath <path>        Specify where to find input source files\n" + 
      "  -bootclasspath <path>     Override location of bootstrap class files\n" + 
      "  -extdirs <dirs>           Override location of installed extensions\n" +
      "  -d <directory>            Specify where to place generated class files\n" +
      "  -help                     Print a synopsis of standard options\n" +
      "  -version                  Print version information\n" +
      "  -no_cache_cycle           Disable cache cycle optimization for circular attributes\n" +
      "  -no_visit_check           Disable visit checks used to detect circularities for attributes\n"
    );
  }

  protected static void printVersion() {
    System.out.println("JastAdd (Experimental Bootstrapped Version) (http://jastadd.cs.lth.se) Version R20060217");
  }
}
