
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

public class AndLogicalExpr extends LogicalExpr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isDAafterTrue_Variable_values = null;
        isDAafterFalse_Variable_values = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        AndLogicalExpr node = (AndLogicalExpr)super.clone();
        node.isDAafterTrue_Variable_values = null;
        node.isDAafterFalse_Variable_values = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          AndLogicalExpr node = (AndLogicalExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AndLogicalExpr res = (AndLogicalExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 576


  public void printOp(StringBuffer s) {
    s.append(" && ");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 176

    public AndLogicalExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 176
    public AndLogicalExpr(Expr p0, Expr p1) {
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

    // Declared in ConstantExpression.jrag at line 512
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  Constant.create(left().constant().booleanValue() && right().constant().booleanValue());  }

    // Declared in DefiniteAssignment.jrag at line 351
    public boolean isDAafterTrue(Variable v) {
        Object _parameters = v;
if(isDAafterTrue_Variable_values == null) isDAafterTrue_Variable_values = new java.util.HashMap(4);
        if(isDAafterTrue_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAafterTrue_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAafterTrue_Variable_value = isDAafterTrue_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDAafterTrue_Variable_values.put(_parameters, Boolean.valueOf(isDAafterTrue_Variable_value));
        return isDAafterTrue_Variable_value;
    }

    private boolean isDAafterTrue_compute(Variable v) {  return  getRightOperand().isDAafterTrue(v) || isFalse();  }

    // Declared in DefiniteAssignment.jrag at line 353
    public boolean isDAafterFalse(Variable v) {
        Object _parameters = v;
if(isDAafterFalse_Variable_values == null) isDAafterFalse_Variable_values = new java.util.HashMap(4);
        if(isDAafterFalse_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAafterFalse_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAafterFalse_Variable_value = isDAafterFalse_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDAafterFalse_Variable_values.put(_parameters, Boolean.valueOf(isDAafterFalse_Variable_value));
        return isDAafterFalse_Variable_value;
    }

    private boolean isDAafterFalse_compute(Variable v) {  return  (getLeftOperand().isDAafterFalse(v) && getRightOperand().isDAafterFalse(v)) || isTrue();  }

    // Declared in DefiniteAssignment.jrag at line 359
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

    private boolean isDAafter_compute(Variable v) {  return  isDAafterTrue(v) && isDAafterFalse(v);  }

    // Declared in DefiniteAssignment.jrag at line 799
    public boolean isDUafterTrue(Variable v) {
        boolean isDUafterTrue_Variable_value = isDUafterTrue_compute(v);
        return isDUafterTrue_Variable_value;
    }

    private boolean isDUafterTrue_compute(Variable v) {  return  getRightOperand().isDUafterTrue(v);  }

    // Declared in DefiniteAssignment.jrag at line 800
    public boolean isDUafterFalse(Variable v) {
        boolean isDUafterFalse_Variable_value = isDUafterFalse_compute(v);
        return isDUafterFalse_Variable_value;
    }

    private boolean isDUafterFalse_compute(Variable v) {  return  getLeftOperand().isDUafterFalse(v) && getRightOperand().isDUafterFalse(v);  }

    // Declared in DefiniteAssignment.jrag at line 803
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

    private boolean isDUafter_compute(Variable v) {  return  isDUafterTrue(v) && isDUafterFalse(v);  }

    // Declared in DefiniteAssignment.jrag at line 357
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getRightOperandNoTransform()) {
            return  getLeftOperand().isDAafterTrue(v);
        }
        if(caller == getLeftOperandNoTransform()) {
            return  isDAbefore(v);
        }
        return super.Define_boolean_isDAbefore(caller, child, v);
    }

    // Declared in DefiniteAssignment.jrag at line 802
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getRightOperandNoTransform()) {
            return  getLeftOperand().isDUafterTrue(v);
        }
        if(caller == getLeftOperandNoTransform()) {
            return  isDUbefore(v);
        }
        return super.Define_boolean_isDUbefore(caller, child, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
