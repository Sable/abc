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
    
    program.addOptions(args);
    Collection files = new ArrayList();
    files.add("test/ASTNode.java");
    files.add("test/Opt.java");
    files.add("test/List.java");
    files.add("test/Test58.java");
    
    JavaParser parser = new JavaParser();
    for(Iterator iter = files.iterator(); iter.hasNext(); ) {
      String name = (String)iter.next();
      try {
        Reader reader = new FileReader(name);
        JavaScanner scanner = new JavaScanner(new UnicodeEscapes(new BufferedReader(reader)));
        CompilationUnit unit = ((Program)parser.parse(scanner)).getCompilationUnit(0);
        unit.setFromSource(true);
        unit.setRelativeName(name);
        unit.setPathName(".");
      	reader.close();
        program.addCompilationUnit(unit);
      } catch (Error e) {
        System.err.println(name + ": " + e.getMessage());
        System.exit(1);
      } catch (RuntimeException e) {
        System.err.println(name + ": " + e.getMessage());
      } catch (IOException e) {
      } catch (Exception e) {
        System.err.println(e);
        e.printStackTrace();
      }
    }
    program.updateRemoteAttributeCollections(files.size());
    if(program.errorCheck(files.size())) {
      if(Program.verbose())
        program.prettyPrint(files.size());
    }
    else {
      program.generateIntertypeDecls(files.size());
      StringBuffer s = new StringBuffer();
      program.getCompilationUnit(3).getTypeDecl(0).toString(s);
      System.out.println(s.toString());
      //if(Program.verbose())
      //  program.prettyPrint(files.size());
      //program.updateRemoteAttributeCollections(files.size());
      //program.generateClassfile(files.size());
    }
  }
}
