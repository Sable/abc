
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;


public class AssignSimpleExpr extends AssignExpr implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        AssignSimpleExpr node = (AssignSimpleExpr)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          AssignSimpleExpr node = (AssignSimpleExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AssignSimpleExpr res = (AssignSimpleExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 278


  public void printOp(StringBuffer s) {
    s.append(" = ");
  }

    // Declared in TypeCheck.jrag at line 35


  // 5.2 Assignment Conversion
  public void typeCheck() {
    if(!getDest().isVariable())
      error("left hand side is not a variable");
    else if(!sourceType().assignConversionTo(getDest().type(), getSource()) && !sourceType().isUnknown())
      error("can not assign " + getDest() + " of type " + getDest().type().typeName() +
            " a value of type " + sourceType().typeName());
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 102

    public AssignSimpleExpr() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 102
    public AssignSimpleExpr(Expr p0, Expr p1) {
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
    // Declared in java.ast line 100
    public void setDest(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getDest() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getDestNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 100
    public void setSource(Expr node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Expr getSource() {
        return (Expr)getChild(1);
    }

    // Declared in java.ast at line 9


    public Expr getSourceNoTransform() {
        return (Expr)getChildNoTransform(1);
    }

    // Declared in TypeCheck.jrag at line 110
    public TypeDecl sourceType() {
        TypeDecl sourceType_value = sourceType_compute();
        return sourceType_value;
    }

    private TypeDecl sourceType_compute() {  return  getSource().type();  }

    // Declared in DefiniteAssignment.jrag at line 18
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getDestNoTransform()) {
            return  false;
        }
        return super.Define_boolean_isSource(caller, child);
    }

    // Declared in DefiniteAssignment.jrag at line 8
    public boolean Define_boolean_isDest(ASTNode caller, ASTNode child) {
        if(caller == getDestNoTransform()) {
            return  true;
        }
        return super.Define_boolean_isDest(caller, child);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
