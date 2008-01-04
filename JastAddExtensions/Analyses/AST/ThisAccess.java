
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;

public class ThisAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decl_computed = false;
        decl_value = null;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ThisAccess node = (ThisAccess)super.clone();
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
          ThisAccess node = (ThisAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ThisAccess res = (ThisAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 625


  public void toString(StringBuffer s) {
    s.append("this");
  }

    // Declared in TypeHierarchyCheck.jrag at line 94


  public void nameCheck() {
    // 8.8.5.1
    if(inExplicitConstructorInvocation() && hostType() == type())
      error("this may not be accessed in an explicit constructor invocation");
    else if(isQualified()) {
      // 15.8.4
      if(inStaticContext())
        error("qualified this may not occur in static context");
      else if(!hostType().isInnerTypeOf(qualifier().type()) && hostType() != qualifier().type())
        error("qualified this must name an enclosing type: " + getParent());
    }
    // 8.4.3.2
    else if(!isQualified() && inStaticContext())
      error("this may not be accessed in static context: " + enclosingStmt());
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 23

    public ThisAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 23
    public ThisAccess(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 23
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in LookupType.jrag at line 155
    public SimpleSet decls() {
        SimpleSet decls_value = decls_compute();
        return decls_value;
    }

    private SimpleSet decls_compute() {  return  SimpleSet.emptySet;  }

    protected boolean decl_computed = false;
    protected TypeDecl decl_value;
    // Declared in LookupType.jrag at line 157
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

    private TypeDecl decl_compute() {  return  isQualified() ? qualifier().type() : hostType();  }

    // Declared in ResolveAmbiguousNames.jrag at line 24
    public boolean isThisAccess() {
        boolean isThisAccess_value = isThisAccess_compute();
        return isThisAccess_value;
    }

    private boolean isThisAccess_compute() {  return  true;  }

    // Declared in SyntacticClassification.jrag at line 82
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.TYPE_NAME;  }

    // Declared in TypeAnalysis.jrag at line 289
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

    // Declared in TypeHierarchyCheck.jrag at line 116
    public boolean inExplicitConstructorInvocation() {
        boolean inExplicitConstructorInvocation_value = getParent().Define_boolean_inExplicitConstructorInvocation(this, null);
        return inExplicitConstructorInvocation_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
