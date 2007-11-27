
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class ConstructorAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decls_computed = false;
        decls_value = null;
        decl_computed = false;
        decl_value = null;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ConstructorAccess node = (ConstructorAccess)super.clone();
        node.decls_computed = false;
        node.decls_value = null;
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
          ConstructorAccess node = (ConstructorAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ConstructorAccess res = (ConstructorAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in ExceptionHandling.jrag at line 88


  public void exceptionHandling() {
    for(int i = 0; i < decl().getNumException(); i++) {
      TypeDecl exceptionType = decl().getException(i).type();
      if(!handlesException(exceptionType))
        error("" + this + " may throw uncaught exception " + exceptionType.fullName());
    }
  }

    // Declared in ExceptionHandling.jrag at line 234

  
  // 8.8.4 (8.4.4)
  protected boolean reachedException(TypeDecl catchType) {
    for(int i = 0; i < decl().getNumException(); i++) {
      TypeDecl exceptionType = decl().getException(i).type();
      if(catchType.mayCatch(exceptionType))
        return true;
    }
    return super.reachedException(catchType);
  }

    // Declared in NameCheck.jrag at line 100


  public void nameCheck() {
    super.nameCheck();
    if(decls().isEmpty())
      error("no constructor named " + this);
    if(decls().size() > 1 && validArgs()) {
      error("several most specific constructors for " + this);
      for(Iterator iter = decls().iterator(); iter.hasNext(); ) {
        error("         " + ((ConstructorDecl)iter.next()).signature());
      }
    }
  }

    // Declared in PrettyPrint.jadd at line 656


  public void toString(StringBuffer s) {
    s.append(name());
    s.append("(");
    if(getNumArg() > 0) {
      getArg(0).toString(s);
      for(int i = 1; i < getNumArg(); i++) {
        s.append(", ");
        getArg(i).toString(s);
      }
    }
    s.append(")");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 18

    public ConstructorAccess() {
        super();

        setChild(new List(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 18
    public ConstructorAccess(String p0, List p1) {
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

    // Declared in DefiniteAssignment.jrag at line 287
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  decl().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 750
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  decl().isDUafter(v);  }

    // Declared in LookupConstructor.jrag at line 42
    public boolean applicableAndAccessible(ConstructorDecl decl) {
        boolean applicableAndAccessible_ConstructorDecl_value = applicableAndAccessible_compute(decl);
        return applicableAndAccessible_ConstructorDecl_value;
    }

    private boolean applicableAndAccessible_compute(ConstructorDecl decl) {  return 
    decl.applicable(getArgList()) && decl.accessibleFrom(hostType());  }

    protected boolean decls_computed = false;
    protected SimpleSet decls_value;
    // Declared in LookupConstructor.jrag at line 48
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

    private SimpleSet decls_compute() {  return  mostSpecificConstructor(lookupConstructor());  }

    protected boolean decl_computed = false;
    protected ConstructorDecl decl_value;
    // Declared in LookupConstructor.jrag at line 56
    public ConstructorDecl decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private ConstructorDecl decl_compute()  {
    SimpleSet decls = decls();
    if(decls.size() == 1)
      return (ConstructorDecl)decls.iterator().next();
    return unknownConstructor();
  }

    // Declared in NameCheck.jrag at line 112
    public boolean validArgs() {
        boolean validArgs_value = validArgs_compute();
        return validArgs_value;
    }

    private boolean validArgs_compute()  {
    for(int i = 0; i < getNumArg(); i++)
      if(getArg(i).type().isUnknown())
        return false;
    return true;
  }

    // Declared in QualifiedNames.jrag at line 10
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  "this";  }

    // Declared in SyntacticClassification.jrag at line 118
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.AMBIGUOUS_NAME;  }

    // Declared in TypeAnalysis.jrag at line 287
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

    private TypeDecl type_compute() {  return  decl().type();  }

    // Declared in ExceptionHandling.jrag at line 21
    public boolean handlesException(TypeDecl exceptionType) {
        boolean handlesException_TypeDecl_value = getParent().Define_boolean_handlesException(this, null, exceptionType);
        return handlesException_TypeDecl_value;
    }

    // Declared in LookupConstructor.jrag at line 5
    public Collection lookupConstructor() {
        Collection lookupConstructor_value = getParent().Define_Collection_lookupConstructor(this, null);
        return lookupConstructor_value;
    }

    // Declared in LookupConstructor.jrag at line 62
    public ConstructorDecl unknownConstructor() {
        ConstructorDecl unknownConstructor_value = getParent().Define_ConstructorDecl_unknownConstructor(this, null);
        return unknownConstructor_value;
    }

    // Declared in LookupVariable.jrag at line 134
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupVariable(name);
        }
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in LookupMethod.jrag at line 18
    public Collection Define_Collection_lookupMethod(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupMethod(name);
        }
        return getParent().Define_Collection_lookupMethod(this, caller, name);
    }

    // Declared in LookupType.jrag at line 79
    public boolean Define_boolean_hasPackage(ASTNode caller, ASTNode child, String packageName) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().hasPackage(packageName);
        }
        return getParent().Define_boolean_hasPackage(this, caller, packageName);
    }

    // Declared in TypeHierarchyCheck.jrag at line 9
    public String Define_String_methodHost(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  unqualifiedScope().methodHost();
        }
        return getParent().Define_String_methodHost(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 111
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.EXPRESSION_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in LookupType.jrag at line 169
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupType(name);
        }
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

    // Declared in TypeHierarchyCheck.jrag at line 121
    public boolean Define_boolean_inExplicitConstructorInvocation(ASTNode caller, ASTNode child) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        return getParent().Define_boolean_inExplicitConstructorInvocation(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
