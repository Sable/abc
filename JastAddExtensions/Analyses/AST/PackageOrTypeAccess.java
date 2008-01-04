
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

public class PackageOrTypeAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        PackageOrTypeAccess node = (PackageOrTypeAccess)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          PackageOrTypeAccess node = (PackageOrTypeAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        PackageOrTypeAccess res = (PackageOrTypeAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NameCheck.jrag at line 43

  
  public void nameCheck() {
    error("packageortype name " + name());
  }

    // Declared in NodeConstructors.jrag at line 23

  public PackageOrTypeAccess(String name, int start, int end) {
    this(name);
    this.start = start;
    this.end = end;
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 30

    public PackageOrTypeAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 30
    public PackageOrTypeAccess(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 30
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in LookupType.jrag at line 280
    public SimpleSet qualifiedLookupType(String name) {
        SimpleSet qualifiedLookupType_String_value = qualifiedLookupType_compute(name);
        return qualifiedLookupType_String_value;
    }

    private SimpleSet qualifiedLookupType_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in LookupVariable.jrag at line 142
    public SimpleSet qualifiedLookupVariable(String name) {
        SimpleSet qualifiedLookupVariable_String_value = qualifiedLookupVariable_compute(name);
        return qualifiedLookupVariable_String_value;
    }

    private SimpleSet qualifiedLookupVariable_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in PrettyPrint.jadd at line 943
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 6
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in QualifiedNames.jrag at line 28
    public String packageName() {
        String packageName_value = packageName_compute();
        return packageName_value;
    }

    private String packageName_compute()  {
    StringBuffer s = new StringBuffer();
    if(hasPrevExpr()) {
      s.append(prevExpr().packageName());
      s.append(".");
    }
    s.append(name());
    return s.toString();
  }

    // Declared in SyntacticClassification.jrag at line 98
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.PACKAGE_OR_TYPE_NAME;  }

public ASTNode rewriteTo() {
    // Declared in ResolveAmbiguousNames.jrag at line 147
    if(!duringSyntacticClassification()) {
        duringResolveAmbiguousNames++;
        ASTNode result = rewriteRule0();
        duringResolveAmbiguousNames--;
        return result;
    }

    return super.rewriteTo();
}

    // Declared in ResolveAmbiguousNames.jrag at line 147
    private Access rewriteRule0(){
      if(!lookupType(name()).isEmpty())
        return new TypeAccess(name(), start(), end());
      else
        return new PackageAccess(name(), start(), end());
    }
}
