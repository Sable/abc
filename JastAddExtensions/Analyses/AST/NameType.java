
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;

  
  // NameType is basically an Enum for the different kinds of names
  // The factory method reclassify builds name nodes of a particular kind
  public class NameType extends java.lang.Object {
    // Declared in SyntacticClassification.jrag at line 16
    private NameType() {
      super();
    }

    // Declared in SyntacticClassification.jrag at line 19
    public static final NameType NO_NAME = new NameType();

    // Declared in SyntacticClassification.jrag at line 20
    public static final NameType PACKAGE_NAME = new NameType() {
      public Access reclassify(String name, int start, int end) { return new PackageAccess(name, start, end); }
    };

    // Declared in SyntacticClassification.jrag at line 23
    public static final NameType TYPE_NAME = new NameType() {
      public Access reclassify(String name, int start, int end) { return new TypeAccess(name, start, end); }
    };

    // Declared in SyntacticClassification.jrag at line 26
    public static final NameType PACKAGE_OR_TYPE_NAME = new NameType() {
      public Access reclassify(String name, int start, int end) { return new PackageOrTypeAccess(name, start, end); }
    };

    // Declared in SyntacticClassification.jrag at line 29
    public static final NameType AMBIGUOUS_NAME = new NameType() {
      public Access reclassify(String name, int start, int end) { return new AmbiguousAccess(name, start, end); }
    };

    // Declared in SyntacticClassification.jrag at line 32
    public static final NameType METHOD_NAME = new NameType();

    // Declared in SyntacticClassification.jrag at line 33
    public static final NameType ARRAY_TYPE_NAME = new NameType();

    // Declared in SyntacticClassification.jrag at line 34
    public static final NameType ARRAY_READ_NAME = new NameType();

    // Declared in SyntacticClassification.jrag at line 35
    public static final NameType EXPRESSION_NAME = new NameType() {
      public Access reclassify(String name, int start, int end) { return new VarAccess(name, start, end); }
    };

    // Declared in SyntacticClassification.jrag at line 39

    public Access reclassify(String name, int start, int end) {
      throw new Error("Can not reclassify ParseName node " + name);
    }


}
