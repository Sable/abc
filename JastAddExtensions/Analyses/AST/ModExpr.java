
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

public class ModExpr extends MultiplicativeExpr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isConstant_computed = false;
    }
    public Object clone() throws CloneNotSupportedException {
        ModExpr node = (ModExpr)super.clone();
        node.isConstant_computed = false;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ModExpr node = (ModExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ModExpr res = (ModExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 506


  public void printOp(StringBuffer s) {
    s.append(" % ");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 160

    public ModExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 160
    public ModExpr(Expr p0, Expr p1) {
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
    // Declared in java.ast line 154
    public void setLeftOperand(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getLeftOperand() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getLeftOperandNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 154
    public void setRightOperand(Expr node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Expr getRightOperand() {
        return (Expr)getChild(1);
    }

    // Declared in java.ast at line 9


    public Expr getRightOperandNoTransform() {
        return (Expr)getChildNoTransform(1);
    }

    // Declared in ConstantExpression.jrag at line 110
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  type().mod(getLeftOperand().constant(), getRightOperand().constant());  }

    protected boolean isConstant_visited = false;
    protected boolean isConstant_computed = false;
    protected boolean isConstant_initialized = false;
    protected boolean isConstant_value;
    public boolean isConstant() {
        if(isConstant_computed)
            return isConstant_value;
        if (!isConstant_initialized) {
            isConstant_initialized = true;
            isConstant_value = false;
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isConstant_visited = true;
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            do {
                CHANGE = false;
                boolean new_isConstant_value = isConstant_compute();
                if (new_isConstant_value!=isConstant_value)
                    CHANGE = true;
                isConstant_value = new_isConstant_value; 
            } while (CHANGE);
            isConstant_visited = false;
            if(isFinal && num == boundariesCrossed)
{
            isConstant_computed = true;
            }
            else {
            RESET_CYCLE = true;
            isConstant_compute();
            RESET_CYCLE = false;
              isConstant_computed = false;
              isConstant_initialized = false;
            }
            IN_CIRCLE = false; 
            return isConstant_value;
        }
        if(!isConstant_visited) {
            if (RESET_CYCLE) {
                isConstant_computed = false;
                isConstant_initialized = false;
                return isConstant_value;
            }
            isConstant_visited = true;
            boolean new_isConstant_value = isConstant_compute();
            if (new_isConstant_value!=isConstant_value)
                CHANGE = true;
            isConstant_value = new_isConstant_value; 
            isConstant_visited = false;
            return isConstant_value;
        }
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  getLeftOperand().isConstant() && getRightOperand().isConstant() && !(getRightOperand().type().isInt() && getRightOperand().constant().intValue() == 0);  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
