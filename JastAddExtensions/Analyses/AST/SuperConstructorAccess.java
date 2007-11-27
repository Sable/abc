
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class SuperConstructorAccess extends ConstructorAccess implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decls_computed = false;
        decls_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        SuperConstructorAccess node = (SuperConstructorAccess)super.clone();
        node.decls_computed = false;
        node.decls_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          SuperConstructorAccess node = (SuperConstructorAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        SuperConstructorAccess res = (SuperConstructorAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in TypeHierarchyCheck.jrag at line 54


  public void nameCheck() {
    super.nameCheck();
    // 8.8.5.1
    TypeDecl c = hostType();
    TypeDecl s = c.isClassDecl() && ((ClassDecl)c).hasSuperclass() ? ((ClassDecl)c).superclass() : unknownType();
    if(isQualified()) {
      if(!s.isInnerType() || s.inStaticContext())
        error("the super type " + s.typeName() + " of " + c.typeName() +
           " is not an inner class");
    
      else if(!qualifier().type().instanceOf(s.enclosingType()))
        error("The type of this primary expression, " +
                qualifier().type().typeName() + " is not enclosing the super type, " + 
                s.typeName() + ", of " + c.typeName());
    }
    if(!isQualified() && s.isInnerType()) {
      if(!c.isInnerType()) {
        //error("" + s.typeName() + " isStatic: " + s.isStatic() + ", enclosingType: " + s.enclosingType().typeName());

        error("no enclosing instance for " + s.typeName() + " when accessed in " + this);
      }
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 19

    public SuperConstructorAccess() {
        super();

        setChild(new List(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 19
    public SuperConstructorAccess(String p0, List p1) {
        setID(p0);
        setChild(p1, 0);
    }

    // Declared in java.ast at line 16


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 19

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 18
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
    // Declared in java.ast line 18
    public void setArgList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumArg = 0;

    // Declared in java.ast at line 7

    public int getNumArg() {
        return getArgList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Expr getArg(int i) {
        return (Expr)getArgList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addArg(Expr node) {
        List list = getArgList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setArg(Expr node, int i) {
        List list = getArgList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getArgList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getArgListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    // Declared in DefiniteAssignment.jrag at line 288
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  isDAbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 751
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  isDUbefore(v);  }

    // Declared in LookupConstructor.jrag at line 50
    public SimpleSet decls() {
        if(decls_computed)
            return decls_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decls_value = decls_compute();
        if(isFinal && num == boundariesCrossed)
            decls_computed = true;
        return decls_value;
    }

    private SimpleSet decls_compute()  {
    Collection c = hasPrevExpr() && !prevExpr().isTypeAccess() ?
      hostType().lookupSuperConstructor() : lookupSuperConstructor();
    return mostSpecificConstructor(c);
  }

    // Declared in QualifiedNames.jrag at line 11
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  "super";  }

    // Declared in ResolveAmbiguousNames.jrag at line 42
    public boolean isSuperConstructorAccess() {
        boolean isSuperConstructorAccess_value = isSuperConstructorAccess_compute();
        return isSuperConstructorAccess_value;
    }

    private boolean isSuperConstructorAccess_compute() {  return  true;  }

    // Declared in SyntacticClassification.jrag at line 86
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.EXPRESSION_NAME;  }

    // Declared in LookupConstructor.jrag at line 10
    public Collection lookupSuperConstructor() {
        Collection lookupSuperConstructor_value = getParent().Define_Collection_lookupSuperConstructor(this, null);
        return lookupSuperConstructor_value;
    }

    // Declared in TypeCheck.jrag at line 423
    public TypeDecl enclosingInstance() {
        TypeDecl enclosingInstance_value = getParent().Define_TypeDecl_enclosingInstance(this, null);
        return enclosingInstance_value;
    }

    // Declared in LookupVariable.jrag at line 135
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupVariable(name);
        }
        return super.Define_SimpleSet_lookupVariable(caller, child, name);
    }

    // Declared in LookupType.jrag at line 80
    public boolean Define_boolean_hasPackage(ASTNode caller, ASTNode child, String packageName) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().hasPackage(packageName);
        }
        return super.Define_boolean_hasPackage(caller, child, packageName);
    }

    // Declared in TypeHierarchyCheck.jrag at line 122
    public boolean Define_boolean_inExplicitConstructorInvocation(ASTNode caller, ASTNode child) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        return super.Define_boolean_inExplicitConstructorInvocation(caller, child);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
