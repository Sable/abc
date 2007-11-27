
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class FloatingPointLiteral extends Literal implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isZero_computed = false;
        constant_computed = false;
        constant_value = null;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        FloatingPointLiteral node = (FloatingPointLiteral)super.clone();
        node.isZero_computed = false;
        node.constant_computed = false;
        node.constant_value = null;
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          FloatingPointLiteral node = (FloatingPointLiteral)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        FloatingPointLiteral res = (FloatingPointLiteral)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in TypeCheck.jrag at line 566


 public void typeCheck() {
   if(!isZero() && constant().floatValue() == 0.0f)
     error("It is an error for nonzero floating-point " + getLITERAL() + " to round to zero");
   if(constant().floatValue() == Float.NEGATIVE_INFINITY || constant().floatValue() == Float.POSITIVE_INFINITY)
     error("It is an error for floating-point " + getLITERAL() + " to round to an infinity");
     
 }

    // Declared in java.ast at line 3
    // Declared in java.ast line 128

    public FloatingPointLiteral() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 128
    public FloatingPointLiteral(String p0) {
        setLITERAL(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 125
    private String tokenString_LITERAL;

    // Declared in java.ast at line 3

    public void setLITERAL(String value) {
        tokenString_LITERAL = value;
    }

    // Declared in java.ast at line 6

    public String getLITERAL() {
        return tokenString_LITERAL != null ? tokenString_LITERAL : "";
    }

    protected boolean isZero_computed = false;
    protected boolean isZero_value;
    // Declared in ConstantExpression.jrag at line 126
    public boolean isZero() {
        if(isZero_computed)
            return isZero_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isZero_value = isZero_compute();
        if(isFinal && num == boundariesCrossed)
            isZero_computed = true;
        return isZero_value;
    }

    private boolean isZero_compute()  {
    String s = getLITERAL();
    for(int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if(c == 'E'  || c == 'e')
        break;
      if(Character.isDigit(c) && c != '0') {
        return false;
      }
    }
    return true;
  }

    // Declared in ConstantExpression.jrag at line 257
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

    private Constant constant_compute()  {
    try {
      return Constant.create(Float.parseFloat(getLITERAL()));
    }
    catch (NumberFormatException e) {
      Constant c = Constant.create(0.0f);
      c.error = true;
      return c;
    }
  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 296
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

    private TypeDecl type_compute() {  return  typeFloat();  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
