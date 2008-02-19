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
      program.emitTest();
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
  protected String version() { return "R20080219"; }

  protected void printUsage() {
      printVersion();
      System.out.println(
          "\n" + name() + "\n\n" +
          "Usage: java -jar JavaNonNullInferencer.jar <options> <source files>\n" +
          "  -verbose                  Output messages about what the compiler is doing\n" +
          "  -classpath <path>         Specify where to find user class files\n" +
          "  -sourcepath <path>        Specify where to find input source files\n" + 
          "  -bootclasspath <path>     Override location of bootstrap class files\n" + 
          "  -extdirs <dirs>           Override location of installed extensions\n" +
          "  -d <directory>            Specify where to place source files with inferred annotations\n" +
          "                            The default location is a folder named 'inferred'\n" +
          "  -test                     Emit inferred source files to standard output rather than a file\n" +
          "  -help                     Print a synopsis of standard options\n" +
          "  -version                  Print version information\n"
          );
    }


}
