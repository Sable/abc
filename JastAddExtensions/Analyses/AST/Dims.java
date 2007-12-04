
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

public class Dims extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        Dims node = (Dims)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          Dims node = (Dims)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        Dims res = (Dims)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 428


  public void toString(StringBuffer s) {
    s.append("[");
    if(hasExpr())
      getExpr().toString(s);
    s.append("]");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 138

    public Dims() {
        super();

        setChild(new Opt(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 138
    public Dims(Opt p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 138
    public void setExprOpt(Opt opt) {
        setChild(opt, 0);
    }

    // Declared in java.ast at line 6


    public boolean hasExpr() {
        return getExprOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Expr getExpr() {
        return (Expr)getExprOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setExpr(Expr node) {
        getExprOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getExprOpt() {
        return (Opt)getChild(0);
    }

    // Declared in java.ast at line 21


    public Opt getExprOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }

    // Declared in DefiniteAssignment.jrag at line 430
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  hasExpr() ? getExpr().isDAafter(v) : isDAbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 868
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  hasExpr() ? getExpr().isDUafter(v) : isDUbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 431
    public boolean isDAbefore(Variable v) {
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        return isDAbefore_Variable_value;
    }

    // Declared in DefiniteAssignment.jrag at line 869
    public boolean isDUbefore(Variable v) {
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        return isDUbefore_Variable_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
