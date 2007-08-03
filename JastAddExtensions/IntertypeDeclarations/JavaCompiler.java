import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JavaCompiler extends Frontend {

  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }

  public static boolean compile(String args[]) {
    return new JavaCompiler().process(
       args,
       new bytecode.Parser(),
       new JavaParser() {
          parser.JavaParser parser = new parser.JavaParser();
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return parser.parse(is, fileName);
          }
       }
    );
  }

  public boolean process(String[] args, BytecodeReader reader, JavaParser parser) {
    if(!super.process(args, reader, parser))
      return false;
    program.generateIntertypeDecls();
    program.transformation();
  
    System.out.println(program.toString());
    return true;
  }

  protected String name() {
    return "Java1.4 + ITDs";
  }

}
