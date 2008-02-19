import AST.*;

import java.util.*;
import java.io.*;

class JavaChecker extends Frontend {

  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }

  public static boolean compile(String args[]) {
    return new JavaChecker().process(
        args,
        new BytecodeParser(),
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

    program.updateRemoteAttributeCollectionsFrontend();
    if(program.hasOption("-test"))
      System.out.println(program);
    else
      program.emit();
    return true;
  }

  protected void initOptions() {
    super.initOptions();
    program.addKeyOption("-test");
  }
  
  protected void processArgs(String[] args) {
    program.addOptions(args);
    if(!program.hasOption("-d"))
      program.addOptions(new String[] { "-d", "inferred" });
  }

  protected String name() { return "NonNullInferencer"; }
  protected String version() { return "R20070806"; }

}
