
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


  public interface FinallyHost {
    // Declared in DefiniteAssignment.jrag at line 913

    //public Block getFinally();
    public boolean isDUafterFinally(Variable v);

    // Declared in DefiniteAssignment.jrag at line 914

    public boolean isDAafterFinally(Variable v);

}
