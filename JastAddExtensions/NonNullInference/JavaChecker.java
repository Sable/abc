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
    Options options = program.options();
    if(options.hasOption("-test"))
      program.emitTest();
    else
      program.emit();
    if(options.hasValueForOption("-debug")) {
      String value = options.getValueForOption("-debug");
      try {
        String fileName = new File(value).getCanonicalPath();
        for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
          CompilationUnit unit = (CompilationUnit)iter.next();
          if(unit.relativeName() != null && fileName.equals(new File(unit.relativeName()).getCanonicalPath())) {
            unit.printDebugInfo();
            return true;
          }
        }
      } catch (IOException e) {
      }
      System.err.println("Warning: could not find compilation unit to debug: " + value);
    }
    return true;
  }

  protected void initOptions() {
    super.initOptions();
    Options options = program.options();
    options.addKeyOption("-test");
    options.addKeyValueOption("-import");
    options.addKeyOption("-legacysyntax");
    options.addKeyOption("-disableraw");
    options.addKeyOption("-defaultnonnull");
    options.addKeyValueOption("-debug");
  }
  
  protected void processArgs(String[] args) {
    Options options = program.options();
    options.addOptions(args);
    if(!options.hasOption("-d"))
      options.addOptions(new String[] { "-d", "inferred" });
    program.rawEnabled = !options.hasOption("-disableraw");
  }

  protected String name() { return "NonNullInferencer"; }
  protected String version() { return "R20080305"; }

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
          "  -import <package>         Include the specified import in the generated source file unless\n" +
          "                            it already exists\n" +
          "  -legacysyntax             Use legacy syntax for inferred annotations, i.e., /*@NonNull*/\n" +
          "  -disableraw               Disable raw types for partially initialised objects. This has\n" +
          "                            the effect that all fields are considered possibly-null\n" +
          "  -help                     Print a synopsis of standard options\n" +
          "  -version                  Print version information\n"
          );
    }


}
