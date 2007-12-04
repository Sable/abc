
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class MethodAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
        computeDAbefore_int_Variable_values = null;
        exceptionCollection_computed = false;
        exceptionCollection_value = null;
        decls_computed = false;
        decls_value = null;
        decl_computed = false;
        decl_value = null;
        type_computed = false;
        type_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        MethodAccess node = (MethodAccess)super.clone();
        node.computeDAbefore_int_Variable_values = null;
        node.exceptionCollection_computed = false;
        node.exceptionCollection_value = null;
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
          MethodAccess node = (MethodAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        MethodAccess res = (MethodAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in AnonymousClasses.jrag at line 140


  protected void collectExceptions(Collection c, ASTNode target) {
    super.collectExceptions(c, target);
    for(int i = 0; i < decl().getNumException(); i++)
      c.add(decl().getException(i).type());
  }

    // Declared in ExceptionHandling.jrag at line 34

  
  public void exceptionHandling() {
    for(Iterator iter = exceptionCollection().iterator(); iter.hasNext(); ) {
      TypeDecl exceptionType = (TypeDecl)iter.next();
      if(!handlesException(exceptionType))
        error("" + decl().hostType().fullName() + "." + this + " invoked in " + hostType().fullName() + " may throw uncaught exception " + exceptionType.fullName());
    }
  }

    // Declared in ExceptionHandling.jrag at line 215


  protected boolean reachedException(TypeDecl catchType) {
    for(Iterator iter = exceptionCollection().iterator(); iter.hasNext(); ) {
      TypeDecl exceptionType = (TypeDecl)iter.next();
      if(catchType.mayCatch(exceptionType))
        return true;
    }
    return super.reachedException(catchType);
  }

    // Declared in LookupMethod.jrag at line 99

  private static SimpleSet removeInstanceMethods(SimpleSet c) {
    SimpleSet set = SimpleSet.emptySet;
    for(Iterator iter = c.iterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(m.isStatic())
        set = set.add(m);
    }
    return set;
  }

    // Declared in LookupMethod.jrag at line 138

  
  public boolean applicable(MethodDecl decl) {
    if(getNumArg() != decl.getNumParameter())
      return false;
    if(!name().equals(decl.name()))
      return false;
    for(int i = 0; i < getNumArg(); i++) {
      if(!getArg(i).type().instanceOf(decl.getParameter(i).type()))
        return false;
    }
    return true;
  }

    // Declared in NodeConstructors.jrag at line 51


  public MethodAccess(String name, List args, int start, int end) {
    this(name, args);
    setStart(start);
    setEnd(end);
  }

    // Declared in PrettyPrint.jadd at line 643


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

    // Declared in TypeCheck.jrag at line 113


  // 5.3 Method Invocation Conversion
  public void typeCheck() {
    for(int i = 0; i < decl().getNumParameter(); i++) {
      TypeDecl exprType = getArg(i).type();
      TypeDecl parmType = decl().getParameter(i).type();
      if(!exprType.methodInvocationConversionTo(parmType) && !exprType.isUnknown() && !parmType.isUnknown()) {
        error("The type " + exprType.typeName() + " of expr " +
            getArg(i) + " is not compatible with the method parameter " +
            decl().getParameter(i));
      }
    }
  }

    // Declared in TypeHierarchyCheck.jrag at line 14

  
  public void nameCheck() {
    if(isQualified() && qualifier().isPackageAccess() && !qualifier().isUnknown())
      error("The method " + decl().signature() + 
          " can not be qualified by a package name.");
    if(isQualified() && decl().isAbstract() && qualifier().isSuperAccess())
      error("may not access abstract methods in superclass");
    if(decls().isEmpty() && (!isQualified() || !qualifier().isUnknown())) {
      StringBuffer s = new StringBuffer();
      s.append("no method named " + name());
      s.append("(");
      for(int i = 0; i < getNumArg(); i++) {
        if(i != 0)
          s.append(", ");
        s.append(getArg(i).type().typeName());
      }
      s.append(")" + " in " + methodHost() + " matches.");
      if(singleCandidateDecl() != null)
        s.append(" However, there is a method " + singleCandidateDecl().signature());
      error(s.toString());
    }
    if(decls().size() > 1) {
      boolean allAbstract = true;
      for(Iterator iter = decls().iterator(); iter.hasNext() && allAbstract; ) {
         MethodDecl m = (MethodDecl)iter.next();
        if(!m.isAbstract() && !m.hostType().isObject())
          allAbstract = false;
      }
      if(!allAbstract && validArgs()) {
        StringBuffer s = new StringBuffer();
        s.append("several most specific methods for " + this + "\n");
        for(Iterator iter = decls().iterator(); iter.hasNext(); ) {
          MethodDecl m = (MethodDecl)iter.next();
          s.append("    " + m.signature() + " in " + m.hostType().typeName() + "\n");
        }
        error(s.toString());
      }
       
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 17

    public MethodAccess() {
        super();

        setChild(new List(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 17
    public MethodAccess(String p0, List p1) {
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
    // Declared in java.ast line 17
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
    // Declared in java.ast line 17
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

    protected java.util.Map computeDAbefore_int_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 399
    public boolean computeDAbefore(int i, Variable v) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(new Integer(i));
        _parameters.add(v);
if(computeDAbefore_int_Variable_values == null) computeDAbefore_int_Variable_values = new java.util.HashMap(4);
        if(computeDAbefore_int_Variable_values.containsKey(_parameters))
            return ((Boolean)computeDAbefore_int_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean computeDAbefore_int_Variable_value = computeDAbefore_compute(i, v);
        if(isFinal && num == boundariesCrossed)
            computeDAbefore_int_Variable_values.put(_parameters, Boolean.valueOf(computeDAbefore_int_Variable_value));
        return computeDAbefore_int_Variable_value;
    }

    private boolean computeDAbefore_compute(int i, Variable v) {  return 
    i == 0 ? isDAbefore(v) : getArg(i-1).isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 401
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  getNumArg() == 0 ? isDAbefore(v) : getArg(getNumArg()-1).isDAafter(v);  }

    protected boolean exceptionCollection_computed = false;
    protected Collection exceptionCollection_value;
    // Declared in ExceptionHandling.jrag at line 42
    public Collection exceptionCollection() {
        if(exceptionCollection_computed)
            return exceptionCollection_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        exceptionCollection_value = exceptionCollection_compute();
        if(isFinal && num == boundariesCrossed)
            exceptionCollection_computed = true;
        return exceptionCollection_value;
    }

    private Collection exceptionCollection_compute()  {
    //System.out.println("Computing exceptionCollection for " + name());
    HashSet set = new HashSet();
    Iterator iter = decls().iterator();
    if(!iter.hasNext())
      return set;

    MethodDecl m = (MethodDecl)iter.next();
    //System.out.println("Processing first found method " + m.signature() + " in " + m.hostType().fullName());

    for(int i = 0; i < m.getNumException(); i++) {
      TypeDecl exceptionType = m.getException(i).type();
      set.add(exceptionType);
    }
    while(iter.hasNext()) {
      HashSet first = new HashSet();
      first.addAll(set);
      HashSet second = new HashSet();
      m = (MethodDecl)iter.next();
      //System.out.println("Processing the next method " + m.signature() + " in " + m.hostType().fullName());
      for(int i = 0; i < m.getNumException(); i++) {
        TypeDecl exceptionType = m.getException(i).type();
        second.add(exceptionType);
      }
      set = new HashSet();
      for(Iterator i1 = first.iterator(); i1.hasNext(); ) {
        TypeDecl firstType = (TypeDecl)i1.next(); 
        for(Iterator i2 = second.iterator(); i2.hasNext(); ) {
          TypeDecl secondType = (TypeDecl)i2.next();
          if(firstType.instanceOf(secondType)) {
            set.add(firstType);
          }
          else if(secondType.instanceOf(firstType)) {
            set.add(secondType);
          }
        }
      }
    }
    return set;
  }

    // Declared in LookupMethod.jrag at line 52
    public MethodDecl singleCandidateDecl() {
        MethodDecl singleCandidateDecl_value = singleCandidateDecl_compute();
        return singleCandidateDecl_value;
    }

    private MethodDecl singleCandidateDecl_compute()  {
    MethodDecl result = null;
    for(Iterator iter = lookupMethod(name()).iterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(result == null)
        result = m;
      else if(m.getNumParameter() == getNumArg() && result.getNumParameter() != getNumArg())
        result = m;
    }
    return result;
  }

    protected boolean decls_computed = false;
    protected SimpleSet decls_value;
    // Declared in LookupMethod.jrag at line 64
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
    SimpleSet maxSpecific = SimpleSet.emptySet;
    for(Iterator iter = lookupMethod(name()).iterator(); iter.hasNext(); ) {
      MethodDecl decl = (MethodDecl)iter.next();
      if(applicable(decl) && accessible(decl)) {
        if(maxSpecific.isEmpty())
          maxSpecific = maxSpecific.add(decl);
        else {
          if(decl.moreSpecificThan((MethodDecl)maxSpecific.iterator().next()))
            maxSpecific = SimpleSet.emptySet.add(decl);
          else if(!((MethodDecl)maxSpecific.iterator().next()).moreSpecificThan(decl))
            maxSpecific = maxSpecific.add(decl);
        }
      }
    }
    if(isQualified() ? qualifier().staticContextQualifier() : inStaticContext())
      maxSpecific = removeInstanceMethods(maxSpecific);
    return maxSpecific;
  }

    protected boolean decl_computed = false;
    protected MethodDecl decl_value;
    // Declared in LookupMethod.jrag at line 83
    public MethodDecl decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private MethodDecl decl_compute()  {
    SimpleSet decls = decls();
    if(decls.size() == 1)
      return (MethodDecl)decls.iterator().next();

    // 8.4.6.4 - only return the first method in case of multply inherited abstract methods
    boolean allAbstract = true;
    for(Iterator iter = decls.iterator(); iter.hasNext() && allAbstract; ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(!m.isAbstract() && !m.hostType().isObject())
        allAbstract = false;
    }
    if(decls.size() > 1 && allAbstract)
      return (MethodDecl)decls.iterator().next();
    return unknownMethod();
  }

    // Declared in LookupMethod.jrag at line 150
    public boolean accessible(MethodDecl m) {
        boolean accessible_MethodDecl_value = accessible_compute(m);
        return accessible_MethodDecl_value;
    }

    private boolean accessible_compute(MethodDecl m)  {
    if(!isQualified())
      return true;
    if(!m.accessibleFrom(hostType()))
      return false;
    // the method is not accessible if the type is not accessible
    if(!qualifier().type().accessibleFrom(hostType()))
      return false;
    // 6.6.2.1 -  include qualifier type for protected access
    if(m.isProtected() && !m.hostPackage().equals(hostPackage()) && !m.isStatic() && !qualifier().isSuperAccess()) {
      TypeDecl C = m.hostType();
      TypeDecl S = hostType().subclassWithinBody(C);
      TypeDecl Q = qualifier().type();
      if(S == null || !Q.instanceOf(S))
        return false;
    }
    return true;
  }

    // Declared in NameCheck.jrag at line 48
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

    // Declared in PrettyPrint.jadd at line 938
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 9
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in ResolveAmbiguousNames.jrag at line 10
    public boolean isMethodAccess() {
        boolean isMethodAccess_value = isMethodAccess_compute();
        return isMethodAccess_value;
    }

    private boolean isMethodAccess_compute() {  return  true;  }

    // Declared in SyntacticClassification.jrag at line 103
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.AMBIGUOUS_NAME;  }

    // Declared in TypeAnalysis.jrag at line 286
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

    // Declared in ExceptionHandling.jrag at line 20
    public boolean handlesException(TypeDecl exceptionType) {
        boolean handlesException_TypeDecl_value = getParent().Define_boolean_handlesException(this, null, exceptionType);
        return handlesException_TypeDecl_value;
    }

    // Declared in LookupMethod.jrag at line 6
    public MethodDecl unknownMethod() {
        MethodDecl unknownMethod_value = getParent().Define_MethodDecl_unknownMethod(this, null);
        return unknownMethod_value;
    }

    // Declared in TypeHierarchyCheck.jrag at line 114
    public boolean inExplicitConstructorInvocation() {
        boolean inExplicitConstructorInvocation_value = getParent().Define_boolean_inExplicitConstructorInvocation(this, null);
        return inExplicitConstructorInvocation_value;
    }

    // Declared in LookupVariable.jrag at line 133
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupVariable(name);
        }
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in LookupMethod.jrag at line 17
    public Collection Define_Collection_lookupMethod(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupMethod(name);
        }
        return getParent().Define_Collection_lookupMethod(this, caller, name);
    }

    // Declared in LookupType.jrag at line 78
    public boolean Define_boolean_hasPackage(ASTNode caller, ASTNode child, String packageName) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().hasPackage(packageName);
        }
        return getParent().Define_boolean_hasPackage(this, caller, packageName);
    }

    // Declared in TypeHierarchyCheck.jrag at line 8
    public String Define_String_methodHost(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  unqualifiedScope().methodHost();
        }
        return getParent().Define_String_methodHost(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 110
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.EXPRESSION_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 398
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getArgListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  computeDAbefore(i, v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in LookupType.jrag at line 168
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getArgListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  unqualifiedScope().lookupType(name);
        }
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected void collect_contributors_MethodDecl_uses() {
        // Declared in Uses.jrag at line 34
        {
            MethodDecl ref = (MethodDecl)(decl());
            if(ref != null)
                ref.MethodDecl_uses_contributors.add(this);
        }
        super.collect_contributors_MethodDecl_uses();
    }
    protected void collect_contributors_MethodDecl_definiteUses() {
        // Declared in Uses.jrag at line 30
        {
            MethodDecl ref = (MethodDecl)(decl());
            if(ref != null)
                ref.MethodDecl_definiteUses_contributors.add(this);
        }
        super.collect_contributors_MethodDecl_definiteUses();
    }
    protected void contributeTo_MethodDecl_MethodDecl_definiteUses(HashSet collection) {
        collection.add(this);
    }

    protected void contributeTo_MethodDecl_MethodDecl_uses(HashSet collection) {
        collection.addAll(this.asSet());
    }

}
