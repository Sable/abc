
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

public class MinusExpr extends Unary implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        MinusExpr node = (MinusExpr)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          MinusExpr node = (MinusExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        MinusExpr res = (MinusExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 465


  public void printPreOp(StringBuffer s) {
    s.append("-");
  }

    // Declared in TypeCheck.jrag at line 264

  
  // 15.15.4
  public void typeCheck() {
    if(!getOperand().type().isNumericType())
      error("unary minus only operates on numeric types");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 143

    public MinusExpr() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 143
    public MinusExpr(Expr p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return true; }

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

    // Declared in ConstantExpression.jrag at line 105
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  type().minus(getOperand().constant());  }

    // Declared in ConstantExpression.jrag at line 463
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  getOperand().isConstant();  }

    // Declared in TypeAnalysis.jrag at line 315
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
    // Declared in ConstantExpression.jrag at line 224
    if(getOperand() instanceof IntegerLiteral && ((IntegerLiteral)getOperand()).isDecimal() && getOperand().isPositive()) {
        duringConstantExpression++;
        ASTNode result = rewriteRule0();
        duringConstantExpression--;
        return result;
    }

    // Declared in ConstantExpression.jrag at line 229
    if(getOperand() instanceof LongLiteral && ((LongLiteral)getOperand()).isDecimal() && getOperand().isPositive()) {
        duringConstantExpression++;
        ASTNode result = rewriteRule1();
        duringConstantExpression--;
        return result;
    }

    return super.rewriteTo();
}

    // Declared in ConstantExpression.jrag at line 224
    private IntegerLiteral rewriteRule0() {
        return  new IntegerLiteral("-" + ((IntegerLiteral)getOperand()).getLITERAL());
    }
    // Declared in ConstantExpression.jrag at line 229
    private LongLiteral rewriteRule1() {
        return  new LongLiteral("-" + ((LongLiteral)getOperand()).getLITERAL());
    }
}
