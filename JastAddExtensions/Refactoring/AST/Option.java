
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

  public class Option extends java.lang.Object {
    // Declared in Options.jadd at line 5
    public String name;

    // Declared in Options.jadd at line 6
    public boolean hasValue;

    // Declared in Options.jadd at line 7
    public boolean isCollection;

    // Declared in Options.jadd at line 8
    public Option(String name, boolean hasValue, boolean isCollection) {
      this.name = name;
      this.hasValue = hasValue;
      this.isCollection = isCollection;
    }


}
