
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;


public abstract class Binary extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isConstant_computed = false;
        isDAafterTrue_Variable_values = null;
        isDAafterFalse_Variable_values = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        isDUbefore_Variable_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        Binary node = (Binary)super.clone();
        node.isConstant_computed = false;
        node.isDAafterTrue_Variable_values = null;
        node.isDAafterFalse_Variable_values = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.isDUbefore_Variable_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in PrettyPrint.jadd at line 490


  // Binary Expr

  public void toString(StringBuffer s) {
    getLeftOperand().toString(s);
    printOp(s);
    getRightOperand().toString(s);
  }

    // Declared in PrettyPrint.jadd at line 496


  public abstract void printOp(StringBuffer s);

    // Declared in java.ast at line 3
    // Declared in java.ast line 154

    public Binary() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 154
    public Binary(Expr p0, Expr p1) {
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

    private boolean isConstant_compute() {  return  getLeftOperand().isConstant() && getRightOperand().isConstant();  }

    // Declared in ConstantExpression.jrag at line 489
    public Expr left() {
        Expr left_value = left_compute();
        return left_value;
    }

    private Expr left_compute() {  return  getLeftOperand();  }

    // Declared in ConstantExpression.jrag at line 490
    public Expr right() {
        Expr right_value = right_compute();
        return right_value;
    }

    private Expr right_compute() {  return  getRightOperand();  }

    // Declared in ConstantExpression.jrag at line 491
    public TypeDecl binaryNumericPromotedType() {
        TypeDecl binaryNumericPromotedType_value = binaryNumericPromotedType_compute();
        return binaryNumericPromotedType_value;
    }

    private TypeDecl binaryNumericPromotedType_compute()  {
    TypeDecl leftType = left().type();
    TypeDecl rightType = right().type();
    if(leftType.isString())
      return leftType;
    if(rightType.isString())
      return rightType;
    if(leftType.isNumericType() && rightType.isNumericType())
      return leftType.binaryNumericPromotion(rightType);
    if(leftType.isBoolean() && rightType.isBoolean())
      return leftType;
    return unknownType();
  }

    protected java.util.Map isDAafterTrue_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 390
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

    private boolean isDAafterTrue_compute(Variable v) {  return  getRightOperand().isDAafter(v) || isFalse();  }

    protected java.util.Map isDAafterFalse_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 391
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

    private boolean isDAafterFalse_compute(Variable v) {  return  getRightOperand().isDAafter(v) || isTrue();  }

    protected java.util.Map isDAafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 393
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

    private boolean isDAafter_compute(Variable v) {  return  getRightOperand().isDAafter(v);  }

    protected java.util.Map isDUafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 842
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

    private boolean isDUafter_compute(Variable v) {  return  getRightOperand().isDUafter(v);  }

    protected java.util.Map isDUbefore_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 693
    public boolean isDUbefore(Variable v) {
        Object _parameters = v;
if(isDUbefore_Variable_values == null) isDUbefore_Variable_values = new java.util.HashMap(4);
        if(isDUbefore_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUbefore_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        if(isFinal && num == boundariesCrossed)
            isDUbefore_Variable_values.put(_parameters, Boolean.valueOf(isDUbefore_Variable_value));
        return isDUbefore_Variable_value;
    }

    // Declared in DefiniteAssignment.jrag at line 394
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getRightOperandNoTransform()) {
            return  getLeftOperand().isDAafter(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 843
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getRightOperandNoTransform()) {
            return  getLeftOperand().isDUafter(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
