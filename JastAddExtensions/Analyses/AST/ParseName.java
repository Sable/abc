
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


public class ParseName extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        ParseName node = (ParseName)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ParseName node = (ParseName)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ParseName res = (ParseName)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 29

    public ParseName() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 29
    public ParseName(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 29
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in LookupType.jrag at line 279
    public SimpleSet qualifiedLookupType(String name) {
        SimpleSet qualifiedLookupType_String_value = qualifiedLookupType_compute(name);
        return qualifiedLookupType_String_value;
    }

    private SimpleSet qualifiedLookupType_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in LookupVariable.jrag at line 141
    public SimpleSet qualifiedLookupVariable(String name) {
        SimpleSet qualifiedLookupVariable_String_value = qualifiedLookupVariable_compute(name);
        return qualifiedLookupVariable_String_value;
    }

    private SimpleSet qualifiedLookupVariable_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in PrettyPrint.jadd at line 942
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 5
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in SyntacticClassification.jrag at line 11
    public NameType nameType() {
        NameType nameType_value = getParent().Define_NameType_nameType(this, null);
        return nameType_value;
    }

public ASTNode rewriteTo() {
    // Declared in SyntacticClassification.jrag at line 6
        duringSyntacticClassification++;
        ASTNode result = rewriteRule0();
        duringSyntacticClassification--;
        return result;
}

    // Declared in SyntacticClassification.jrag at line 6
    private Access rewriteRule0() {
        return  nameType().reclassify(name(), start, end);
    }
}
