
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;

public interface JavaParser {
    // Declared in ClassPath.jrag at line 19

    CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception;

}
