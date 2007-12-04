
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


public class AssertStmt extends Stmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        AssertStmt node = (AssertStmt)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          AssertStmt node = (AssertStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AssertStmt res = (AssertStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in TypeCheck.jrag at line 367


  public void typeCheck() {
    // 14.10
    if(!getfirst().type().isBoolean())
      error("Assert requires boolean condition");
    if(hasExpr() && getExpr().type().isVoid())
      error("The second part of an assert statement may not be void");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 226

    public AssertStmt() {
        super();

        setChild(null, 0);
        setChild(new Opt(), 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 226
    public AssertStmt(Expr p0, Opt p1) {
        setChild(p0, 0);
        setChild(p1, 1);
    }

    // Declared in java.ast at line 17


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 20

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 226
    public void setfirst(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getfirst() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getfirstNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 226
    public void setExprOpt(Opt opt) {
        setChild(opt, 1);
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
        return (Opt)getChild(1);
    }

    // Declared in java.ast at line 21


    public Opt getExprOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }

    // Declared in DefiniteAssignment.jrag at line 405
    public boolean isDAafter(Variable v) {
        Object _parameters = v;
if(isDAafter_Variable_values == null) isDAafter_Variable_values = new java.util.HashMap(4);
        if(isDAafter_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAafter_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDAafter_Variable_values.put(_parameters, Boolean.valueOf(isDAafter_Variable_value));
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  getfirst().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 872
    public boolean isDUafter(Variable v) {
        Object _parameters = v;
if(isDUafter_Variable_values == null) isDUafter_Variable_values = new java.util.HashMap(4);
        if(isDUafter_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUafter_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDUafter_Variable_values.put(_parameters, Boolean.valueOf(isDUafter_Variable_value));
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  getfirst().isDUafter(v);  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
