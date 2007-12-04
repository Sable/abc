
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


public class ArrayCreationExpr extends PrimaryExpr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ArrayCreationExpr node = (ArrayCreationExpr)super.clone();
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ArrayCreationExpr node = (ArrayCreationExpr)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ArrayCreationExpr res = (ArrayCreationExpr)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 415


  public void toString(StringBuffer s) {
    s.append("new ");
    getTypeAccess().toString(s);

    for(int i = 0; i < getNumDims(); i++) {
      getDims(i).toString(s);
    }
    
    if(hasArrayInit()) {
      getArrayInit().toString(s);
    }
  }

    // Declared in TypeCheck.jrag at line 537


  public void typeCheck() {
    super.typeCheck();
    for(int i = 0; i < getNumDims(); i++) {
      if(getDims(i).hasExpr() && !getDims(i).getExpr().type().unaryNumericPromotion().isInt())
        error("The type of dimension " + i + " which is " + getDims(i).getExpr().type().typeName() +
              " is not int after unary numeric promotion");
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 137

    public ArrayCreationExpr() {
        super();

        setChild(null, 0);
        setChild(new List(), 1);
        setChild(new Opt(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 137
    public ArrayCreationExpr(Access p0, List p1, Opt p2) {
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
    // Declared in java.ast line 137
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
    // Declared in java.ast line 137
    public void setDimsList(List list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumDims = 0;

    // Declared in java.ast at line 7

    public int getNumDims() {
        return getDimsList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Dims getDims(int i) {
        return (Dims)getDimsList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addDims(Dims node) {
        List list = getDimsList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setDims(Dims node, int i) {
        List list = getDimsList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getDimsList() {
        return (List)getChild(1);
    }

    // Declared in java.ast at line 28


    public List getDimsListNoTransform() {
        return (List)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 137
    public void setArrayInitOpt(Opt opt) {
        setChild(opt, 2);
    }

    // Declared in java.ast at line 6


    public boolean hasArrayInit() {
        return getArrayInitOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public ArrayInit getArrayInit() {
        return (ArrayInit)getArrayInitOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setArrayInit(ArrayInit node) {
        getArrayInitOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getArrayInitOpt() {
        return (Opt)getChild(2);
    }

    // Declared in java.ast at line 21


    public Opt getArrayInitOptNoTransform() {
        return (Opt)getChildNoTransform(2);
    }

    // Declared in DefiniteAssignment.jrag at line 418
    public boolean isDAafterCreation(Variable v) {
        boolean isDAafterCreation_Variable_value = isDAafterCreation_compute(v);
        return isDAafterCreation_Variable_value;
    }

    private boolean isDAafterCreation_compute(Variable v)  {
    if(getNumDims() == 0)
      return isDAbefore(v);
    return getDims(getNumDims()-1).isDAafter(v);
  }

    // Declared in DefiniteAssignment.jrag at line 423
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  hasArrayInit() ? getArrayInit().isDAafter(v) : isDAafterCreation(v);  }

    // Declared in DefiniteAssignment.jrag at line 857
    public boolean isDUafterCreation(Variable v) {
        boolean isDUafterCreation_Variable_value = isDUafterCreation_compute(v);
        return isDUafterCreation_Variable_value;
    }

    private boolean isDUafterCreation_compute(Variable v)  {
    if(getNumDims() == 0)
      return isDUbefore(v);
    return getDims(getNumDims()-1).isDUafter(v);
  }

    // Declared in DefiniteAssignment.jrag at line 862
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  hasArrayInit() ? getArrayInit().isDUafter(v) : isDUafterCreation(v);  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 306
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
    TypeDecl typeDecl = getTypeAccess().type();
    for(int i = 0; i < getNumDims(); i++)
      typeDecl = typeDecl.arrayType();
    return typeDecl;
  }

    // Declared in TypeAnalysis.jrag at line 259
    public TypeDecl Define_TypeDecl_declType(ASTNode caller, ASTNode child) {
        if(caller == getArrayInitOptNoTransform()) {
            return  type();
        }
        return getParent().Define_TypeDecl_declType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 77
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getTypeAccessNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 429
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getArrayInitOptNoTransform()) {
            return  isDAafterCreation(v);
        }
        if(caller == getDimsListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
    if(i == 0)
      return isDAbefore(v);
    return getDims(i-1).isDAafter(v);
  }
}
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 866
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getArrayInitOptNoTransform()) {
            return  isDUafterCreation(v);
        }
        if(caller == getDimsListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  i == 0 ?
    isDUbefore(v) : getDims(i-1).isDUafter(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
