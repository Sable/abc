
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class PreDecExpr extends Unary implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        PreDecExpr node = (PreDecExpr)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          PreDecExpr node = (PreDecExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        PreDecExpr res = (PreDecExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DefiniteAssignment.jrag at line 70

  
  public void definiteAssignment() {
    if(getOperand().isVariable()) {
      Variable v = getOperand().varDecl();
      if(v != null && v.isFinal()) {
        error("++ and -- can not be applied to final variable " + v);
      }
    }
  }

    // Declared in DefiniteAssignment.jrag at line 480

  protected boolean checkDUeverywhere(Variable v) {
    if(getOperand().isVariable() && getOperand().varDecl() == v)
      if(!isDAbefore(v))
        return false;
    return super.checkDUeverywhere(v);
  }

    // Declared in PrettyPrint.jadd at line 461


  public void printPreOp(StringBuffer s) {
    s.append("--");
  }

    // Declared in TypeCheck.jrag at line 298

  
  // 15.15.2
  public void typeCheck() {
    if(!getOperand().isVariable())
      error("prefix decrement expression only work on variables");
    else if(!getOperand().type().isNumericType())
      error("unary decrement only operates on numeric types");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 142

    public PreDecExpr() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 142
    public PreDecExpr(Expr p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 140
    public void setOperand(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getOperand() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getOperandNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in DefiniteAssignment.jrag at line 44
    public boolean Define_boolean_isIncOrDec(ASTNode caller, ASTNode child) {
        if(caller == getOperandNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isIncOrDec(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 36
    public boolean Define_boolean_isDest(ASTNode caller, ASTNode child) {
        if(caller == getOperandNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isDest(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
