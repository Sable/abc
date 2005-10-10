import AST.*;

import java.util.*;
import java.io.*;
import parser.*;

public class JastAdd {

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
    Collection files = program.files();
    
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
      if(Program.verbose())
        program.prettyPrint(files.size());
    if(program.errorCheck(files.size())) {
      if(Program.verbose())
        program.prettyPrint(files.size());
    }
    else {
      program.generateIntertypeDecls(files.size());
      if(Program.verbose())
        program.prettyPrint(files.size());
      program.updateRemoteAttributeCollections(files.size());
      program.generateClassfile(files.size());
    }
  }
}
