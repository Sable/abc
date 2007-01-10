import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JavaCompiler {
  public static void main(String args[]) {
    try {
      if(!compile(args))
        System.exit(1);
    } catch (Exception e) {
      e.printStackTrace();
      throw new Error("Error");
    }
  }

  public static boolean compile(String args[]) {
    Program program = new Program();

    program.initBytecodeReader(new bytecode.Parser());
    program.initJavaParser(
      new JavaParser() {
        public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
          return new parser.JavaParser().parse(is, fileName);
        }
      }
    );
    // extract package name from a source file without parsing the entire file
    program.initPackageExtractor(new parser.JavaScanner());

    program.initOptions();
    program.addKeyValueOption("-classpath");
    program.addKeyValueOption("-sourcepath");
    program.addKeyValueOption("-bootclasspath");
    program.addKeyValueOption("-extdirs");
    program.addKeyValueOption("-d");
    program.addKeyOption("-verbose");
    program.addKeyOption("-version");
    program.addKeyOption("-help");

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
      if(name.endsWith(".java"))
        program.addSourceFile(name);
    }

    try {
      // Force loading of all classes and aspects prior to analysis
      // Inter type declarations may be declared in any aspect
      // and need to be loaded prior to name analysis
      for(Iterator iter = program.compilationUnitIterator(); iter.hasNext();  ) {
        iter.next();
      }

      Collection errors = new LinkedList();
      program.errorCheck(errors);
      if(!errors.isEmpty()) {
        System.out.println("Errors:");
        for(Iterator iter2 = errors.iterator(); iter2.hasNext(); ) {
          String s = (String)iter2.next();
          System.out.println(s);
        }
        return false;
      }
    } catch (ParseError e) {
      System.err.println(e.getMessage());
      return false;
    } catch (LexicalError e) {
      System.err.println(e.getMessage());
      return false;
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }

    program.generateIntertypeDecls();
    program.transformation();
  
    System.out.println(program.toString());
    return true;
  }
  
  protected static void printUsage() {
    printVersion();
    System.out.println(
      "\nJavaCompiler\n\n" +
      "Usage: java JavaCompiler <options> <source files>\n" +
      "  -verbose                  Output messages about what the compiler is doing\n" +
      "  -classpath <path>         Specify where to find user class files\n" +
      "  -sourcepath <path>        Specify where to find input source files\n" + 
      "  -bootclasspath <path>     Override location of bootstrap class files\n" + 
      "  -extdirs <dirs>           Override location of installed extensions\n" +
      "  -d <directory>            Specify where to place generated class files\n" +
      "  -help                     Print a synopsis of standard options\n" +
      "  -version                  Print version information\n"
    );
  }

  protected static void printVersion() {
    System.out.println("Java + ITD");
  }
}
