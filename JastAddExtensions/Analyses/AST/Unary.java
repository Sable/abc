
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


public abstract class Unary extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        Unary node = (Unary)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in PrettyPrint.jadd at line 437


  // Pre and post operations for unary expression
  
  public void toString(StringBuffer s) {
    printPreOp(s);
    getOperand().toString(s);
    printPostOp(s);
  }

    // Declared in PrettyPrint.jadd at line 443


  public void printPreOp(StringBuffer s) {
  }

    // Declared in PrettyPrint.jadd at line 446


  public void printPostOp(StringBuffer s) {
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 140

    public Unary() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 140
    public Unary(Expr p0) {
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

    // Declared in DefiniteAssignment.jrag at line 387
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  getOperand().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 839
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  getOperand().isDUafter(v);  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 313
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

    private TypeDecl type_compute() {  return  getOperand().type();  }

    // Declared in DefiniteAssignment.jrag at line 33
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getOperandNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isSource(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
