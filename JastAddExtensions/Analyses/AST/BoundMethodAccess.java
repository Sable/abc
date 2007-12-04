
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;
// A BoundMethodAccess is a method access that bypasses the normal name binding.
// It receives its corresponding declaration explicitly through the constructor.
public class BoundMethodAccess extends MethodAccess implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decl_computed = false;
        decl_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        BoundMethodAccess node = (BoundMethodAccess)super.clone();
        node.decl_computed = false;
        node.decl_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          BoundMethodAccess node = (BoundMethodAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BoundMethodAccess res = (BoundMethodAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BoundNames.jrag at line 52


  // A BoundMethodAccess is a MethodAccess where the name analysis is bypassed by explicitly setting the desired binding
  // this is useful when name binding is cached and recomputation is undesired
  public BoundMethodAccess(String name, List args, MethodDecl methodDecl) {
    this(name, args);
    this.methodDecl = methodDecl;
  }

    // Declared in BoundNames.jrag at line 56

  private MethodDecl methodDecl;

    // Declared in BoundNames.ast at line 3
    // Declared in BoundNames.ast line 3

    public BoundMethodAccess() {
        super();

        setChild(new List(), 0);

    }

    // Declared in BoundNames.ast at line 11


    // Declared in BoundNames.ast line 3
    public BoundMethodAccess(String p0, List p1) {
        setID(p0);
        setChild(p1, 0);
    }

    // Declared in BoundNames.ast at line 16


  protected int numChildren() {
    return 1;
  }

    // Declared in BoundNames.ast at line 19

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 17
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 17
    public void setArgList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumArg = 0;

    // Declared in java.ast at line 7

    public int getNumArg() {
        return getArgList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Expr getArg(int i) {
        return (Expr)getArgList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addArg(Expr node) {
        List list = getArgList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setArg(Expr node, int i) {
        List list = getArgList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getArgList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getArgListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    // Declared in BoundNames.jrag at line 57
    public MethodDecl decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private MethodDecl decl_compute() {  return  methodDecl;  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
