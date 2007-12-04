
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


public class CastExpr extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        CastExpr node = (CastExpr)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          CastExpr node = (CastExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        CastExpr res = (CastExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 481


  public void toString(StringBuffer s) {
    s.append("(");
    getTypeAccess().toString(s);
    s.append(")");
    getExpr().toString(s);
  }

    // Declared in TypeCheck.jrag at line 241

  
  // 15.16
  public void typeCheck() {
    TypeDecl expr = getExpr().type();
    TypeDecl type = getTypeAccess().type();
    if(!expr.isUnknown()) {
      if(!expr.castingConversionTo(type))
        error(expr.typeName() + " can not be cast into " + type.typeName());
      if(!getTypeAccess().isTypeAccess())
        error("" + getTypeAccess() + " is not a type access in cast expression");
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 148

    public CastExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 148
    public CastExpr(Access p0, Expr p1) {
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
    // Declared in java.ast line 148
    public void setTypeAccess(Access node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Access getTypeAccess() {
        return (Access)getChild(0);
    }

    // Declared in java.ast at line 9


    public Access getTypeAccessNoTransform() {
        return (Access)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 148
    public void setExpr(Expr node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Expr getExpr() {
        return (Expr)getChild(1);
    }

    // Declared in java.ast at line 9


    public Expr getExprNoTransform() {
        return (Expr)getChildNoTransform(1);
    }

    // Declared in ConstantExpression.jrag at line 101
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  type().cast(getExpr().constant());  }

    // Declared in ConstantExpression.jrag at line 460
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  getExpr().isConstant() &&
    (getTypeAccess().type().isPrimitive() || getTypeAccess().type().isString());  }

    // Declared in DefiniteAssignment.jrag at line 388
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  getExpr().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 840
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  getExpr().isDUafter(v);  }

    // Declared in ResolveAmbiguousNames.jrag at line 20
    public boolean isSuperAccess() {
        boolean isSuperAccess_value = isSuperAccess_compute();
        return isSuperAccess_value;
    }

    private boolean isSuperAccess_compute() {  return  getExpr().isSuperAccess();  }

    // Declared in ResolveAmbiguousNames.jrag at line 26
    public boolean isThisAccess() {
        boolean isThisAccess_value = isThisAccess_compute();
        return isThisAccess_value;
    }

    private boolean isThisAccess_compute() {  return  getExpr().isThisAccess();  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 319
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

    private TypeDecl type_compute() {  return  getTypeAccess().type();  }

    // Declared in TypeHierarchyCheck.jrag at line 143
    public boolean staticContextQualifier() {
        boolean staticContextQualifier_value = staticContextQualifier_compute();
        return staticContextQualifier_value;
    }

    private boolean staticContextQualifier_compute() {  return  getExpr().staticContextQualifier();  }

    // Declared in SyntacticClassification.jrag at line 78
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
