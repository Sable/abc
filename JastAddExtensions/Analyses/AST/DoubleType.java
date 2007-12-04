
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

public class DoubleType extends FloatingPointType implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        DoubleType node = (DoubleType)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          DoubleType node = (DoubleType)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        DoubleType res = (DoubleType)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 59

    public DoubleType() {
        super();

        setChild(null, 0);
        setChild(new Opt(), 1);
        setChild(new List(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 59
    public DoubleType(Modifiers p0, String p1, Opt p2, List p3) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
        setChild(p3, 2);
    }

    // Declared in java.ast at line 20


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 23

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 41
    public void setModifiers(Modifiers node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Modifiers getModifiers() {
        return (Modifiers)getChild(0);
    }

    // Declared in java.ast at line 9


    public Modifiers getModifiersNoTransform() {
        return (Modifiers)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 41
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 41
    public void setSuperClassAccessOpt(Opt opt) {
        setChild(opt, 1);
    }

    // Declared in java.ast at line 6


    public boolean hasSuperClassAccess() {
        return getSuperClassAccessOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Access getSuperClassAccess() {
        return (Access)getSuperClassAccessOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setSuperClassAccess(Access node) {
        getSuperClassAccessOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getSuperClassAccessOpt() {
        return (Opt)getChild(1);
    }

    // Declared in java.ast at line 21


    public Opt getSuperClassAccessOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 41
    public void setBodyDeclList(List list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumBodyDecl = 0;

    // Declared in java.ast at line 7

    public int getNumBodyDecl() {
        return getBodyDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


    public BodyDecl getBodyDecl(int i) {
        return (BodyDecl)getBodyDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addBodyDecl(BodyDecl node) {
        List list = getBodyDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setBodyDecl(BodyDecl node, int i) {
        List list = getBodyDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getBodyDeclList() {
        return (List)getChild(2);
    }

    // Declared in java.ast at line 28


    public List getBodyDeclListNoTransform() {
        return (List)getChildNoTransform(2);
    }

    // Declared in ConstantExpression.jrag at line 291
    public Constant cast(Constant c) {
        Constant cast_Constant_value = cast_compute(c);
        return cast_Constant_value;
    }

    private Constant cast_compute(Constant c) {  return  Constant.create(c.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 302
    public Constant plus(Constant c) {
        Constant plus_Constant_value = plus_compute(c);
        return plus_Constant_value;
    }

    private Constant plus_compute(Constant c) {  return  c;  }

    // Declared in ConstantExpression.jrag at line 311
    public Constant minus(Constant c) {
        Constant minus_Constant_value = minus_compute(c);
        return minus_Constant_value;
    }

    private Constant minus_compute(Constant c) {  return  Constant.create(-c.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 327
    public Constant mul(Constant c1, Constant c2) {
        Constant mul_Constant_Constant_value = mul_compute(c1, c2);
        return mul_Constant_Constant_value;
    }

    private Constant mul_compute(Constant c1, Constant c2) {  return  Constant.create(c1.doubleValue() * c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 336
    public Constant div(Constant c1, Constant c2) {
        Constant div_Constant_Constant_value = div_compute(c1, c2);
        return div_Constant_Constant_value;
    }

    private Constant div_compute(Constant c1, Constant c2) {  return  Constant.create(c1.doubleValue() / c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 345
    public Constant mod(Constant c1, Constant c2) {
        Constant mod_Constant_Constant_value = mod_compute(c1, c2);
        return mod_Constant_Constant_value;
    }

    private Constant mod_compute(Constant c1, Constant c2) {  return  Constant.create(c1.doubleValue() % c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 354
    public Constant add(Constant c1, Constant c2) {
        Constant add_Constant_Constant_value = add_compute(c1, c2);
        return add_Constant_Constant_value;
    }

    private Constant add_compute(Constant c1, Constant c2) {  return  Constant.create(c1.doubleValue() + c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 364
    public Constant sub(Constant c1, Constant c2) {
        Constant sub_Constant_Constant_value = sub_compute(c1, c2);
        return sub_Constant_Constant_value;
    }

    private Constant sub_compute(Constant c1, Constant c2) {  return  Constant.create(c1.doubleValue() - c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 418
    public Constant questionColon(Constant cond, Constant c1, Constant c2) {
        Constant questionColon_Constant_Constant_Constant_value = questionColon_compute(cond, c1, c2);
        return questionColon_Constant_Constant_Constant_value;
    }

    private Constant questionColon_compute(Constant cond, Constant c1, Constant c2) {  return  Constant.create(cond.booleanValue() ? c1.doubleValue() : c2.doubleValue());  }

    // Declared in ConstantExpression.jrag at line 522
    public boolean eqIsTrue(Expr left, Expr right) {
        boolean eqIsTrue_Expr_Expr_value = eqIsTrue_compute(left, right);
        return eqIsTrue_Expr_Expr_value;
    }

    private boolean eqIsTrue_compute(Expr left, Expr right) {  return  left.constant().doubleValue() == right.constant().doubleValue();  }

    // Declared in ConstantExpression.jrag at line 530
    public boolean ltIsTrue(Expr left, Expr right) {
        boolean ltIsTrue_Expr_Expr_value = ltIsTrue_compute(left, right);
        return ltIsTrue_Expr_Expr_value;
    }

    private boolean ltIsTrue_compute(Expr left, Expr right) {  return  left.constant().doubleValue() < right.constant().doubleValue();  }

    // Declared in ConstantExpression.jrag at line 536
    public boolean leIsTrue(Expr left, Expr right) {
        boolean leIsTrue_Expr_Expr_value = leIsTrue_compute(left, right);
        return leIsTrue_Expr_Expr_value;
    }

    private boolean leIsTrue_compute(Expr left, Expr right) {  return  left.constant().doubleValue() <= right.constant().doubleValue();  }

    // Declared in TypeAnalysis.jrag at line 192
    public boolean isDouble() {
        boolean isDouble_value = isDouble_compute();
        return isDouble_value;
    }

    private boolean isDouble_compute() {  return  true;  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
