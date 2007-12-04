
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;


public class InstanceOfExpr extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        InstanceOfExpr node = (InstanceOfExpr)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          InstanceOfExpr node = (InstanceOfExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        InstanceOfExpr res = (InstanceOfExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 558


  public void printOp(StringBuffer s) {
    s.append(" instanceof ");
  }

    // Declared in PrettyPrint.jadd at line 562

  
  public void toString(StringBuffer s) {
    getExpr().toString(s);
    printOp(s);
    getTypeAccess().toString(s);
  }

    // Declared in TypeCheck.jrag at line 224


  // 15.20.2
  public void typeCheck() {
    TypeDecl relationalExpr = getExpr().type();
    TypeDecl referenceType = getTypeAccess().type();
    if(!relationalExpr.isUnknown()) {
      if(!relationalExpr.isReferenceType() && !relationalExpr.isNull())
        error("The relational expression in instance of must be reference or null type");
      if(!referenceType.isReferenceType())
        error("The reference expression in instance of must be reference type");
      if(!relationalExpr.castingConversionTo(referenceType))
        error("The type " + relationalExpr.typeName() + " of the relational expression " + 
          getExpr() +  " can not be cast into the type " + referenceType.typeName());
      if(getExpr().isTypeAccess())
        error("The relational expression " + getExpr() + " must not be a type name");
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 189

    public InstanceOfExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 189
    public InstanceOfExpr(Expr p0, Access p1) {
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
    // Declared in java.ast line 189
    public void setExpr(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getExpr() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getExprNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 189
    public void setTypeAccess(Access node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Access getTypeAccess() {
        return (Access)getChild(1);
    }

    // Declared in java.ast at line 9


    public Access getTypeAccessNoTransform() {
        return (Access)getChildNoTransform(1);
    }

    // Declared in ConstantExpression.jrag at line 467
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  false;  }

    // Declared in DefiniteAssignment.jrag at line 336
    public boolean isDAafterFalse(Variable v) {
        boolean isDAafterFalse_Variable_value = isDAafterFalse_compute(v);
        return isDAafterFalse_Variable_value;
    }

    private boolean isDAafterFalse_compute(Variable v) {  return  isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 337
    public boolean isDAafterTrue(Variable v) {
        boolean isDAafterTrue_Variable_value = isDAafterTrue_compute(v);
        return isDAafterTrue_Variable_value;
    }

    private boolean isDAafterTrue_compute(Variable v) {  return  isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 396
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  getExpr().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 845
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  getExpr().isDUafter(v);  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 360
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

    private TypeDecl type_compute() {  return  typeBoolean();  }

    // Declared in SyntacticClassification.jrag at line 79
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getTypeAccessNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
