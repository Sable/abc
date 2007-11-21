import AST.*;

import java.util.*;
import java.io.*;
import parser.*;

public class PrettyPrintTest58 {

  public static void main(String args[]) {
    Program program = new Program();
    program.initOptions();
    program.addKeyValueOption("-classpath");
    program.addKeyValueOption("-sourcepath");
    program.addKeyValueOption("-bootclasspath");
    program.addKeyValueOption("-extdirs");
    program.addKeyValueOption("-d");
    program.addKeyOption("-verbose");
    program.addKeyOption("-no_cache_cycle");
    program.addKeyOption("-no_visit_check");
    program.addKeyOption("-no_component_check");
    
    program.addOptions(args);
    Collection files = new ArrayList();
    files.add("test/ASTNode.java");
    files.add("test/Opt.java");
    files.add("test/List.java");
    files.add("test/Test58.java");
    
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

    if(program.errorCheck()) {
      if(Program.verbose())
        System.out.println(program);
    }
    else {
      program.generateIntertypeDecls();
      program.transformation();
      System.out.println(program.getCompilationUnit(3).getTypeDecl(0));
    }

  }
}
