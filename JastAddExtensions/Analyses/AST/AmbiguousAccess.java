
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

public class AmbiguousAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        AmbiguousAccess node = (AmbiguousAccess)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          AmbiguousAccess node = (AmbiguousAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AmbiguousAccess res = (AmbiguousAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NameCheck.jrag at line 39


  public void nameCheck() {
    error("ambiguous name " + name());
  }

    // Declared in NodeConstructors.jrag at line 28

  public AmbiguousAccess(String name, int start, int end) {
    this(name);
    this.start = start;
    this.end = end;
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 31

    public AmbiguousAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 31
    public AmbiguousAccess(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 31
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in LookupType.jrag at line 281
    public SimpleSet qualifiedLookupType(String name) {
        SimpleSet qualifiedLookupType_String_value = qualifiedLookupType_compute(name);
        return qualifiedLookupType_String_value;
    }

    private SimpleSet qualifiedLookupType_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in LookupVariable.jrag at line 143
    public SimpleSet qualifiedLookupVariable(String name) {
        SimpleSet qualifiedLookupVariable_String_value = qualifiedLookupVariable_compute(name);
        return qualifiedLookupVariable_String_value;
    }

    private SimpleSet qualifiedLookupVariable_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in PrettyPrint.jadd at line 944
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 7
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in SyntacticClassification.jrag at line 102
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.AMBIGUOUS_NAME;  }

public ASTNode rewriteTo() {
    // Declared in ResolveAmbiguousNames.jrag at line 166
    if(!duringSyntacticClassification()) {
        duringResolveAmbiguousNames++;
        ASTNode result = rewriteRule0();
        duringResolveAmbiguousNames--;
        return result;
    }

    return super.rewriteTo();
}

    // Declared in ResolveAmbiguousNames.jrag at line 166
    private Access rewriteRule0() {
      if(!lookupVariable(name()).isEmpty()) {
        return new VarAccess(name(), start(), end());
      }
      else if(!lookupType(name()).isEmpty()) {
        return new TypeAccess(name(), start(), end());
      }
      else {
        return new PackageAccess(name(), start(), end());
      }
    }
}
