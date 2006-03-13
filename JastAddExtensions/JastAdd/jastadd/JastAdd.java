package jastadd;

import AST.*;

import java.util.*;
import java.io.*;
import parser.*;

import AST.List;

public class JastAdd {
  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
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

    int numFiles = 0;
    JavaParser parser = new JavaParser();
    for(Iterator iter = files.iterator(); iter.hasNext(); ) {
      String name = (String)iter.next();
      try {
        Reader reader = new FileReader(name);
        JavaScanner scanner = new JavaScanner(new UnicodeEscapes(new BufferedReader(reader)));
        if(name.endsWith(".java") || name.endsWith(".jrag") || name.endsWith(".jadd")) {
          CompilationUnit unit = ((Program)parser.parse(scanner)).getCompilationUnit(0);
          unit.setFromSource(true);
          unit.setRelativeName(name);
          unit.setPathName(".");
          program.addCompilationUnit(unit);
          numFiles++;
        }
        else if(name.endsWith(".ast")) {
          scanner.enterJastAdd();
          CompilationUnit cu = (CompilationUnit)parser.parse(scanner, JavaParser.AltGoals.ast_file);
          scanner.previousState();
          List packageList = cu.getPackageDeclList();
          List importList = cu.getImportDeclList();
          for(int i = 0; i < cu.getTypeDeclList().getNumChild(); i++) {
            TypeDecl typeDecl = (TypeDecl)cu.getTypeDeclList().getChildNoTransform(i);
            CompilationUnit unit = new CompilationUnit(
              (List)packageList.fullCopy(),
              (List)importList.fullCopy(),
              new List().add(typeDecl)
            );
            unit.setFromSource(true);
            unit.setRelativeName(name);
            unit.setPathName(".");
            program.addCompilationUnit(unit);
            numFiles++;
          }
        }
        reader.close();
      } catch (Error e) {
        System.err.println(name + ": " + e.getMessage());
        e.printStackTrace();
        return false;
      } catch (RuntimeException e) {
        System.err.println(name + ": " + e.getMessage());
        e.printStackTrace();
        return false;
      } catch (IOException e) {
        System.err.println("error: " + e.getMessage());
        return false;
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
      }
    }
    program.updateRemoteAttributeCollections(numFiles);
    if(Program.verbose())
        program.prettyPrint(numFiles);
    if(program.errorCheck(numFiles)) {
      if(Program.verbose())
        program.prettyPrint(numFiles);
    }
    else {
      program.java2Transformation(numFiles);
      program.generateIntertypeDecls(numFiles);
      if(Program.verbose())
        program.prettyPrint(numFiles);
      program.updateRemoteAttributeCollections(numFiles);
      program.generateClassfile(numFiles);
      return true;
    }
    return false;
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
