
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;


// Explicitly bound access that bypasses name binding
public class BoundFieldAccess extends VarAccess implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decl_computed = false;
        decl_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        BoundFieldAccess node = (BoundFieldAccess)super.clone();
        node.decl_computed = false;
        node.decl_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          BoundFieldAccess node = (BoundFieldAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BoundFieldAccess res = (BoundFieldAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BoundNames.jrag at line 59


  public BoundFieldAccess(FieldDeclaration f) {
    this(f.name(), f);
  }

    // Declared in BoundNames.jrag at line 64

  public boolean isExactVarAccess() {
    return false;
  }

    // Declared in BoundNames.ast at line 3
    // Declared in BoundNames.ast line 6

    public BoundFieldAccess() {
        super();


    }

    // Declared in BoundNames.ast at line 10


    // Declared in BoundNames.ast line 6
    public BoundFieldAccess(String p0, FieldDeclaration p1) {
        setID(p0);
        setFieldDeclaration(p1);
    }

    // Declared in BoundNames.ast at line 15


  protected int numChildren() {
    return 0;
  }

    // Declared in BoundNames.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 16
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in BoundNames.ast at line 2
    // Declared in BoundNames.ast line 6
    private FieldDeclaration tokenFieldDeclaration_FieldDeclaration;

    // Declared in BoundNames.ast at line 3

    public void setFieldDeclaration(FieldDeclaration value) {
        tokenFieldDeclaration_FieldDeclaration = value;
    }

    // Declared in BoundNames.ast at line 6

    public FieldDeclaration getFieldDeclaration() {
        return tokenFieldDeclaration_FieldDeclaration;
    }

    // Declared in BoundNames.jrag at line 63
    public Variable decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private Variable decl_compute() {  return  getFieldDeclaration();  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
