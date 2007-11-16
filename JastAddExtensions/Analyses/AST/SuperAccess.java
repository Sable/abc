
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;

public class SuperAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decl_computed = false;
        decl_value = null;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        SuperAccess node = (SuperAccess)super.clone();
        node.decl_computed = false;
        node.decl_value = null;
        node.type_computed = false;
        node.type_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          SuperAccess node = (SuperAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        SuperAccess res = (SuperAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 629

  
  public void toString(StringBuffer s) {
    s.append("super");
  }

    // Declared in TypeHierarchyCheck.jrag at line 78


  public void nameCheck() {
    if(isQualified()) {
      if(!hostType().isInnerTypeOf(qualifier().type()) && hostType() != qualifier().type())
        error("qualified super must name an enclosing type");
      if(inStaticContext()) {
        error("*** Qualified super may not occur in static context");
      }
    }
    // 8.8.5.1
    if(inExplicitConstructorInvocation() && hostType().instanceOf(decl().hostType()) )
      error("super may not be accessed in an explicit constructor invocation");
    // 8.4.3.2
    if(inStaticContext())
      error("super may not be accessed in a static context");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 24

    public SuperAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 24
    public SuperAccess(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 24
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in LookupType.jrag at line 156
    public SimpleSet decls() {
        SimpleSet decls_value = decls_compute();
        return decls_value;
    }

    private SimpleSet decls_compute() {  return  SimpleSet.emptySet;  }

    protected boolean decl_computed = false;
    protected TypeDecl decl_value;
    // Declared in LookupType.jrag at line 158
    public TypeDecl decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private TypeDecl decl_compute()  {
    TypeDecl typeDecl = isQualified() ? qualifier().type() : hostType();
    if(!typeDecl.isClassDecl())
      return unknownType();
    ClassDecl classDecl = (ClassDecl)typeDecl;
    if(!classDecl.hasSuperclass())
      return unknownType();
    return classDecl.superclass();
  }

    // Declared in ResolveAmbiguousNames.jrag at line 18
    public boolean isSuperAccess() {
        boolean isSuperAccess_value = isSuperAccess_compute();
        return isSuperAccess_value;
    }

    private boolean isSuperAccess_compute() {  return  true;  }

    // Declared in SyntacticClassification.jrag at line 83
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.TYPE_NAME;  }

    // Declared in TypeAnalysis.jrag at line 290
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

    private TypeDecl type_compute() {  return  decl();  }

    // Declared in TypeHierarchyCheck.jrag at line 115
    public boolean inExplicitConstructorInvocation() {
        boolean inExplicitConstructorInvocation_value = getParent().Define_boolean_inExplicitConstructorInvocation(this, null);
        return inExplicitConstructorInvocation_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
