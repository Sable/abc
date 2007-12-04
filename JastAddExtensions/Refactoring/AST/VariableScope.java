
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

  public interface VariableScope {
    // Declared in LookupVariable.jrag at line 222

    public SimpleSet lookupVariable(String name);

}
