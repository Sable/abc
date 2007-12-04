
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;



public class ConditionalExpr extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        constant_computed = false;
        constant_value = null;
        isConstant_computed = false;
        booleanOperator_computed = false;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ConditionalExpr node = (ConditionalExpr)super.clone();
        node.constant_computed = false;
        node.constant_value = null;
        node.isConstant_computed = false;
        node.booleanOperator_computed = false;
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ConditionalExpr node = (ConditionalExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ConditionalExpr res = (ConditionalExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 584


  public void toString(StringBuffer s) {
    getCondition().toString(s);
    s.append(" ? ");
    getTrueExpr().toString(s);
    s.append(" : ");
    getFalseExpr().toString(s);
  }

    // Declared in TypeCheck.jrag at line 547


  // 15.25
  public void typeCheck() {
    if(!getCondition().type().isBoolean())
      error("*** First expression must be a boolean in conditional operator");
    if(type().isUnknown() && !getTrueExpr().type().isUnknown() && !getFalseExpr().type().isUnknown()) {
      error("*** Operands in conditional operator does not match"); 
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 192

    public ConditionalExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(null, 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 192
    public ConditionalExpr(Expr p0, Expr p1, Expr p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
    }

    // Declared in java.ast at line 19


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 22

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 192
    public void setCondition(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getCondition() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getConditionNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 192
    public void setTrueExpr(Expr node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Expr getTrueExpr() {
        return (Expr)getChild(1);
    }

    // Declared in java.ast at line 9


    public Expr getTrueExprNoTransform() {
        return (Expr)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 192
    public void setFalseExpr(Expr node) {
        setChild(node, 2);
    }

    // Declared in java.ast at line 5

    public Expr getFalseExpr() {
        return (Expr)getChild(2);
    }

    // Declared in java.ast at line 9


    public Expr getFalseExprNoTransform() {
        return (Expr)getChildNoTransform(2);
    }

    protected boolean constant_computed = false;
    protected Constant constant_value;
    // Declared in ConstantExpression.jrag at line 123
    public Constant constant() {
        if(constant_computed)
            return constant_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        constant_value = constant_compute();
        if(isFinal && num == boundariesCrossed)
            constant_computed = true;
        return constant_value;
    }

    private Constant constant_compute() {  return  type().questionColon(getCondition().constant(), getTrueExpr().constant(),getFalseExpr().constant());  }

    protected boolean isConstant_computed = false;
    protected boolean isConstant_value;
    // Declared in ConstantExpression.jrag at line 468
    public boolean isConstant() {
        if(isConstant_computed)
            return isConstant_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isConstant_value = isConstant_compute();
        if(isFinal && num == boundariesCrossed)
            isConstant_computed = true;
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  getCondition().isConstant() && getTrueExpr().isConstant() && getFalseExpr().isConstant();  }

    protected boolean booleanOperator_computed = false;
    protected boolean booleanOperator_value;
    // Declared in DefiniteAssignment.jrag at line 221
    public boolean booleanOperator() {
        if(booleanOperator_computed)
            return booleanOperator_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        booleanOperator_value = booleanOperator_compute();
        if(isFinal && num == boundariesCrossed)
            booleanOperator_computed = true;
        return booleanOperator_value;
    }

    private boolean booleanOperator_compute() {  return  getTrueExpr().type().isBoolean() && getFalseExpr().type().isBoolean();  }

    // Declared in DefiniteAssignment.jrag at line 372
    public boolean isDAafterTrue(Variable v) {
        boolean isDAafterTrue_Variable_value = isDAafterTrue_compute(v);
        return isDAafterTrue_Variable_value;
    }

    private boolean isDAafterTrue_compute(Variable v) {  return  (getTrueExpr().isDAafterTrue(v) && getFalseExpr().isDAafterTrue(v)) || isFalse();  }

    // Declared in DefiniteAssignment.jrag at line 373
    public boolean isDAafterFalse(Variable v) {
        boolean isDAafterFalse_Variable_value = isDAafterFalse_compute(v);
        return isDAafterFalse_Variable_value;
    }

    private boolean isDAafterFalse_compute(Variable v) {  return  (getTrueExpr().isDAafterFalse(v) && getFalseExpr().isDAafterFalse(v)) || isTrue();  }

    // Declared in DefiniteAssignment.jrag at line 377
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  booleanOperator() ? isDAafterTrue(v) && isDAafterFalse(v) : getTrueExpr().isDAafter(v) && getFalseExpr().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 816
    public boolean isDUafterTrue(Variable v) {
        boolean isDUafterTrue_Variable_value = isDUafterTrue_compute(v);
        return isDUafterTrue_Variable_value;
    }

    private boolean isDUafterTrue_compute(Variable v) {  return  getTrueExpr().isDUafterTrue(v) && getFalseExpr().isDUafterTrue(v);  }

    // Declared in DefiniteAssignment.jrag at line 817
    public boolean isDUafterFalse(Variable v) {
        boolean isDUafterFalse_Variable_value = isDUafterFalse_compute(v);
        return isDUafterFalse_Variable_value;
    }

    private boolean isDUafterFalse_compute(Variable v) {  return  getTrueExpr().isDUafterFalse(v) && getFalseExpr().isDUafterFalse(v);  }

    // Declared in DefiniteAssignment.jrag at line 821
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  booleanOperator() ? isDUafterTrue(v) && isDUafterFalse(v) : getTrueExpr().isDUafter(v) && getFalseExpr().isDUafter(v);  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 363
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

    private TypeDecl type_compute()  {
    TypeDecl trueType = getTrueExpr().type();
    TypeDecl falseType = getFalseExpr().type();
    
    if(trueType == falseType) return trueType;
    
    if(trueType.isNumericType() && falseType.isNumericType()) {
      if(trueType.isByte() && falseType.isShort()) return falseType;
      if(trueType.isShort() && falseType.isByte()) return trueType;
      if((trueType.isByte() || trueType.isShort() || trueType.isChar()) && 
         falseType.isInt() && getFalseExpr().isConstant() && getFalseExpr().representableIn(trueType))
        return trueType;
      if((falseType.isByte() || falseType.isShort() || falseType.isChar()) && 
         trueType.isInt() && getTrueExpr().isConstant() && getTrueExpr().representableIn(falseType))
        return falseType;
      return trueType.binaryNumericPromotion(falseType);
    }
    else if(trueType.isBoolean() && falseType.isBoolean()) {
      return trueType;
    }
    else if(trueType.isReferenceType() && falseType.isNull()) {
      return trueType;
    }
    else if(trueType.isNull() && falseType.isReferenceType()) {
      return falseType;
    }
    else if(trueType.isReferenceType() && falseType.isReferenceType()) {
      if(trueType.assignConversionTo(falseType, null))
        return falseType;
      if(falseType.assignConversionTo(trueType, null))
        return trueType;
      return unknownType();
    }
    else
      return unknownType();
  }

    // Declared in DefiniteAssignment.jrag at line 376
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getFalseExprNoTransform()) {
            return  getCondition().isDAafterFalse(v);
        }
        if(caller == getTrueExprNoTransform()) {
            return  getCondition().isDAafterTrue(v);
        }
        if(caller == getConditionNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 820
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getFalseExprNoTransform()) {
            return  getCondition().isDUafterFalse(v);
        }
        if(caller == getTrueExprNoTransform()) {
            return  getCondition().isDUafterTrue(v);
        }
        if(caller == getConditionNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
