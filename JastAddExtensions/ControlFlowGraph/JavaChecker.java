import AST.*;
import java.util.*;
import java.io.*;

class JavaChecker extends Frontend {

  public static void main(String args[]) {
    compile(args);
  }

  public static boolean compile(String args[]) {
    JavaChecker checker = new JavaChecker();
    boolean result = checker.process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
        }
    );
    return result;
  }

  protected void processNoErrors(CompilationUnit unit) {
    System.out.println(unit);
    System.out.println(unit.dumpTree());
  }
}
