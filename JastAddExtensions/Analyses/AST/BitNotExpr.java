
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

public class BitNotExpr extends Unary implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        BitNotExpr node = (BitNotExpr)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          BitNotExpr node = (BitNotExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        BitNotExpr res = (BitNotExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 473


  public void printPreOp(StringBuffer s) {
    s.append("~");
  }

    // Declared in TypeCheck.jrag at line 270


  // 15.15.5
  public void typeCheck() {
    if(!getOperand().type().isIntegralType())
      error("unary ~ only operates on integral types");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 145

    public BitNotExpr() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 145
    public BitNotExpr(Expr p0) {
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

    // Declared in ConstantExpression.jrag at line 106
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  type().bitNot(getOperand().constant());  }

    // Declared in ConstantExpression.jrag at line 464
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  getOperand().isConstant();  }

    // Declared in TypeAnalysis.jrag at line 316
    public TypeDecl type() {
        if(type_computed)
            return type_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        type_value = type_compute();
        if(isFinal && num == boundariesCrossed)
            type_computed = true;
        return type_value;
    }

    private TypeDecl type_compute() {  return  getOperand().type().unaryNumericPromotion();  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
