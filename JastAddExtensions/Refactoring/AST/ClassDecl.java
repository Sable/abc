
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


// 4.3 Reference Types and Values
public class ClassDecl extends ReferenceType implements Cloneable {
    public void flushCache() {
        super.flushCache();
        noConstructor_computed = false;
        interfacesMethodsSignatureMap_computed = false;
        interfacesMethodsSignatureMap_value = null;
        methodsSignatureMap_computed = false;
        methodsSignatureMap_value = null;
        ancestorMethods_String_values = null;
        memberTypes_String_values = null;
        unimplementedMethods_computed = false;
        unimplementedMethods_value = null;
        hasAbstract_computed = false;
        castingConversionTo_TypeDecl_values = null;
        isString_computed = false;
        isObject_computed = false;
        instanceOf_TypeDecl_values = null;
        isCircular_computed = false;
    }
    public Object clone() throws CloneNotSupportedException {
        ClassDecl node = (ClassDecl)super.clone();
        node.noConstructor_computed = false;
        node.interfacesMethodsSignatureMap_computed = false;
        node.interfacesMethodsSignatureMap_value = null;
        node.methodsSignatureMap_computed = false;
        node.methodsSignatureMap_value = null;
        node.ancestorMethods_String_values = null;
        node.memberTypes_String_values = null;
        node.unimplementedMethods_computed = false;
        node.unimplementedMethods_value = null;
        node.hasAbstract_computed = false;
        node.castingConversionTo_TypeDecl_values = null;
        node.isString_computed = false;
        node.isObject_computed = false;
        node.instanceOf_TypeDecl_values = null;
        node.isCircular_computed = false;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ClassDecl node = (ClassDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ClassDecl res = (ClassDecl)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in AccessControl.jrag at line 138

  
  public void accessControl() {
    super.accessControl();
    
    // 8.1.1.2 final Classes
    TypeDecl typeDecl = hasSuperclass() ? superclass() : null;
    if(typeDecl != null && !typeDecl.accessibleFromExtend(this))
    //if(typeDecl != null && !isCircular() && !typeDecl.accessibleFrom(this))
      error("class " + fullName() + " may not extend non accessible type " + typeDecl.fullName());

    if(hasSuperclass() && !superclass().accessibleFrom(this))
      error("a superclass must be accessible which " + superclass().name() + " is not");

    // 8.1.4
    for(int i = 0; i < getNumImplements(); i++) {
      TypeDecl decl = getImplements(i).type();
      if(!decl.isCircular() && !decl.accessibleFrom(this))
        error("class " + fullName() + " can not implement non accessible type " + decl.fullName());
    }
  }

    // Declared in ExceptionHandling.jrag at line 83


  public void exceptionHandling() {
    constructors();
    super.exceptionHandling();
  }

    // Declared in LookupMethod.jrag at line 234


  // iterator over all methods in implemented interfaces
  public Iterator interfacesMethodsIterator() {
    return new Iterator() {
      private Iterator outer = interfacesMethodsSignatureMap().values().iterator();
      private Iterator inner = null;
      public boolean hasNext() {
        if((inner == null || !inner.hasNext()) && outer.hasNext())
          inner = ((SimpleSet)outer.next()).iterator();
        return inner == null ? false : inner.hasNext();
      }
      public Object next() {
        return inner.next();
      }
      public void remove() { throw new UnsupportedOperationException(); }
    };
  }

    // Declared in Modifiers.jrag at line 85

  
 public void checkModifiers() {
    super.checkModifiers();
    // 8.1.1.2 final Classes
    TypeDecl typeDecl = hasSuperclass() ? superclass() : null;
    if(typeDecl != null && typeDecl.isFinal()) {
      error("class " + fullName() + " may not extend final class " + typeDecl.fullName());
    }

  }

    // Declared in PrettyPrint.jadd at line 67



    
  public void toString(StringBuffer s) {
    getModifiers().toString(s);
    s.append("class " + name());
    if(hasSuperClassAccess()) {
      s.append(" extends ");
      s.append(getSuperClassAccess().type().name());
    }
    if(getNumImplements() > 0) {
      s.append(" implements ");
      getImplements(0).toString(s);
      for(int i = 1; i < getNumImplements(); i++) {
        s.append(", ");
        getImplements(i).toString(s);
      }
    }
    s.append(" {\n");
    indent++;
    for(int i=0; i < getNumBodyDecl(); i++) {
      getBodyDecl(i).toString(s);
    }

    indent--;
    s.append(indent() + "}\n");
  }

    // Declared in TypeAnalysis.jrag at line 595


  public boolean hasSuperclass() {
    return !isObject();
  }

    // Declared in TypeAnalysis.jrag at line 599


  public ClassDecl superclass() {
    if(isObject())
      return null;
    if(hasSuperClassAccess() && !isCircular() && getSuperClassAccess().type().isClassDecl())
      return (ClassDecl)getSuperClassAccess().type();
    return (ClassDecl)typeObject();
  }

    // Declared in TypeAnalysis.jrag at line 614

  

  public Iterator interfacesIterator() {
    return new Iterator() {
      public boolean hasNext() {
        computeNextCurrent();
        return current != null;
      }
      public Object next() {
        return current;
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
      private int index = 0;
      private TypeDecl current = null;
      private void computeNextCurrent() {
        current = null;
        if(isObject() || isCircular())
          return;
        while(index < getNumImplements()) {
          TypeDecl typeDecl = getImplements(index++).type();
          if(!typeDecl.isCircular() && typeDecl.isInterfaceDecl()) {
            current = typeDecl;
            return;
          }
        }
      }
    };
  }

    // Declared in TypeHierarchyCheck.jrag at line 230


  public void nameCheck() {
    super.nameCheck();
    if(hasSuperClassAccess() && !getSuperClassAccess().type().isClassDecl())
      error("class may only inherit a class and not " + getSuperClassAccess().type().typeName());
    if(isObject() && hasSuperClassAccess())
      error("class Object may not have superclass");
    if(isObject() && getNumImplements() != 0)
      error("class Object may not implement interfaces");
    
    // 8.1.3
    if(isCircular())
      error("circular inheritance dependency in " + typeName()); 
      
    // 8.1.4
    HashSet set = new HashSet();
    for(int i = 0; i < getNumImplements(); i++) {
      TypeDecl decl = getImplements(i).type();
      if(!decl.isInterfaceDecl() && !decl.isUnknown())
        error("type " + fullName() + " tries to implement non interface type " + decl.fullName());
      if(set.contains(decl))
        error("type " + decl.fullName() + " mentionened multiple times in implements clause");
      set.add(decl);
    }

    for(Iterator iter = interfacesMethodsIterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(localMethodsSignature(m.signature()).isEmpty()) {
        SimpleSet s = superclass().methodsSignature(m.signature());
        for(Iterator i2 = s.iterator(); i2.hasNext(); ) {
          MethodDecl n = (MethodDecl)i2.next();
          if(n.accessibleFrom(this)) {
            interfaceMethodCompatibleWithInherited(m, n);
          }
        }
        if(s.isEmpty()) {
          for(Iterator i2 = interfacesMethodsSignature(m.signature()).iterator(); i2.hasNext(); ) {
            MethodDecl n = (MethodDecl)i2.next();
            if(!n.mayOverrideReturn(m) && !m.mayOverrideReturn(n))
              error("Xthe return type of method " + m.signature() + " in " + m.hostType().typeName() + 
                  " does not match the return type of method " + n.signature() + " in " + 
                  n.hostType().typeName() + " and may thus not be overriden");
          }
        }
      }
    }
  }

    // Declared in TypeHierarchyCheck.jrag at line 277


  private void interfaceMethodCompatibleWithInherited(MethodDecl m, MethodDecl n) {
    if(n.isStatic())
      error("Xa static method may not hide an instance method");
    if(!n.isAbstract() && !n.isPublic())
      error("Xoverriding access modifier error for " + m.signature() + " in " + m.hostType().typeName() + " and " + n.hostType().typeName());
    if(!n.mayOverrideReturn(m) && !m.mayOverrideReturn(m))
      error("Xthe return type of method " + m.signature() + " in " + m.hostType().typeName() + 
            " does not match the return type of method " + n.signature() + " in " + 
            n.hostType().typeName() + " and may thus not be overriden");
    if(!n.isAbstract()) {
      // n implements and overrides method m in the interface
      // may not throw more checked exceptions
      for(int i = 0; i < n.getNumException(); i++) {
        Access e = n.getException(i);
        boolean found = false;
        for(int j = 0; !found && j < m.getNumException(); j++) {
          if(e.type().instanceOf(m.getException(j).type()))
            found = true;
        }
        if(!found && e.type().isUncheckedException())
          error("X" + n.signature() + " in " + n.hostType().typeName() + " may not throw more checked exceptions than overridden method " +
           m.signature() + " in " + m.hostType().typeName());
      }
    }
  }

    // Declared in AddMethod.jrag at line 11


	public void addMethod(MethodDecl md, 
				boolean mayOverride, boolean mayBeOverridden, boolean mayBeCaptured) 
			throws RefactoringException {
        AdjustmentTable table = new AdjustmentTable();
        for(Iterator i=unqualifiedLookupMethod(md.getID()).iterator();i.hasNext();) {
            MethodDecl m = (MethodDecl)i.next();
            for(Iterator j=m.uses().iterator();j.hasNext();) {
                MethodAccess acc = (MethodAccess)j.next();
                table.add(acc);
            }
        }
        addMemberMethod(md);
        programRoot().clear();
        if(localMethodsSignature(md.signature()).size() > 1)
            throw new RefactoringException("a method of signature "+md.signature()+" already exists");
		if(!mayBeCaptured && !md.uses().isEmpty())
			throw new RefactoringException("new method "+md.getID()+" could be captured");
		SimpleSet overrides = md.overrides();
		if(mayOverride) {
			for(Iterator i=overrides.iterator();i.hasNext();) {
				MethodDecl md2 = (MethodDecl)i.next();
				if(!md.canOverrideOrHide(md2))
					throw new RefactoringException("new method "+md.getID()+
							" cannot override method in "+md2.hostType().typeName());
			}
		} else if(!overrides.isEmpty()) {
			throw new RefactoringException("new method "+md.getID()+" overrides other method");
		}
		HashSet overriders = md.overriders();
		if(mayBeOverridden) {
			for(Iterator i=overriders.iterator();i.hasNext();) {
				MethodDecl md2 = (MethodDecl)i.next();
				if(!md2.canOverrideOrHide(md))
					throw new RefactoringException("new method "+md.getID()+
							" cannot be overriden by method in "+md2.hostType().typeName());
			}
		} else if(!overriders.isEmpty()) {
			throw new RefactoringException("new method "+md.getID()+" is overridden");
		}
        // make sure that uses of methods of the same name can be adjusted
        table.adjust();
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 62

    public ClassDecl() {
        super();

        setChild(null, 0);
        setChild(new Opt(), 1);
        setChild(new List(), 2);
        setChild(new List(), 3);

    }

    // Declared in java.ast at line 14


    // Declared in java.ast line 62
    public ClassDecl(Modifiers p0, String p1, Opt p2, List p3, List p4) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
        setChild(p3, 2);
        setChild(p4, 3);
    }

    // Declared in java.ast at line 22


  protected int numChildren() {
    return 4;
  }

    // Declared in java.ast at line 25

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 62
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
    // Declared in java.ast line 62
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
    // Declared in java.ast line 62
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
    // Declared in java.ast line 62
    public void setImplementsList(List list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumImplements = 0;

    // Declared in java.ast at line 7

    public int getNumImplements() {
        return getImplementsList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Access getImplements(int i) {
        return (Access)getImplementsList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addImplements(Access node) {
        List list = getImplementsList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setImplements(Access node, int i) {
        List list = getImplementsList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getImplementsList() {
        return (List)getChild(2);
    }

    // Declared in java.ast at line 28


    public List getImplementsListNoTransform() {
        return (List)getChildNoTransform(2);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 62
    public void setBodyDeclList(List list) {
        setChild(list, 3);
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
        return (List)getChild(3);
    }

    // Declared in java.ast at line 28


    public List getBodyDeclListNoTransform() {
        return (List)getChildNoTransform(3);
    }

    // Declared in ConstantExpression.jrag at line 293
    public Constant cast(Constant c) {
        Constant cast_Constant_value = cast_compute(c);
        return cast_Constant_value;
    }

    private Constant cast_compute(Constant c) {  return  Constant.create(c.stringValue());  }

    // Declared in ConstantExpression.jrag at line 355
    public Constant add(Constant c1, Constant c2) {
        Constant add_Constant_Constant_value = add_compute(c1, c2);
        return add_Constant_Constant_value;
    }

    private Constant add_compute(Constant c1, Constant c2) {  return  Constant.create(c1.stringValue() + c2.stringValue());  }

    // Declared in ConstantExpression.jrag at line 420
    public Constant questionColon(Constant cond, Constant c1, Constant c2) {
        Constant questionColon_Constant_Constant_Constant_value = questionColon_compute(cond, c1, c2);
        return questionColon_Constant_Constant_Constant_value;
    }

    private Constant questionColon_compute(Constant cond, Constant c1, Constant c2) {  return  Constant.create(cond.booleanValue() ? c1.stringValue() : c2.stringValue());  }

    // Declared in ConstantExpression.jrag at line 524
    public boolean eqIsTrue(Expr left, Expr right) {
        boolean eqIsTrue_Expr_Expr_value = eqIsTrue_compute(left, right);
        return eqIsTrue_Expr_Expr_value;
    }

    private boolean eqIsTrue_compute(Expr left, Expr right) {  return  isString() && left.constant().stringValue().equals(right.constant().stringValue());  }

    // Declared in ErrorCheck.jrag at line 21
    public int lineNumber() {
        int lineNumber_value = lineNumber_compute();
        return lineNumber_value;
    }

    private int lineNumber_compute() {  return  getSuperClassAccessOpt().lineNumber();  }

    // Declared in LookupConstructor.jrag at line 13
    public Collection lookupSuperConstructor() {
        Collection lookupSuperConstructor_value = lookupSuperConstructor_compute();
        return lookupSuperConstructor_value;
    }

    private Collection lookupSuperConstructor_compute() {  return  hasSuperclass() ? superclass().constructors() : Collections.EMPTY_LIST;  }

    protected boolean noConstructor_computed = false;
    protected boolean noConstructor_value;
    // Declared in LookupConstructor.jrag at line 168
    public boolean noConstructor() {
        if(noConstructor_computed)
            return noConstructor_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        noConstructor_value = noConstructor_compute();
        if(isFinal && num == boundariesCrossed)
            noConstructor_computed = true;
        return noConstructor_value;
    }

    private boolean noConstructor_compute()  {
    for(int i = 0; i < getNumBodyDecl(); i++)
      if(getBodyDecl(i) instanceof ConstructorDecl)
        return false;
    return true;
  }

    // Declared in LookupMethod.jrag at line 249
    public SimpleSet interfacesMethodsSignature(String signature) {
        SimpleSet interfacesMethodsSignature_String_value = interfacesMethodsSignature_compute(signature);
        return interfacesMethodsSignature_String_value;
    }

    private SimpleSet interfacesMethodsSignature_compute(String signature)  {
    SimpleSet set = (SimpleSet)interfacesMethodsSignatureMap().get(signature);
    if(set != null) return set;
    return SimpleSet.emptySet;
  }

    protected boolean interfacesMethodsSignatureMap_computed = false;
    protected HashMap interfacesMethodsSignatureMap_value;
    // Declared in LookupMethod.jrag at line 255
    public HashMap interfacesMethodsSignatureMap() {
        if(interfacesMethodsSignatureMap_computed)
            return interfacesMethodsSignatureMap_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        interfacesMethodsSignatureMap_value = interfacesMethodsSignatureMap_compute();
        if(isFinal && num == boundariesCrossed)
            interfacesMethodsSignatureMap_computed = true;
        return interfacesMethodsSignatureMap_value;
    }

    private HashMap interfacesMethodsSignatureMap_compute()  {
    HashMap map = new HashMap();
    for(Iterator iter = interfacesIterator(); iter.hasNext(); ) {
      TypeDecl typeDecl = (InterfaceDecl)iter.next();
      for(Iterator i2 = typeDecl.methodsIterator(); i2.hasNext(); ) {
        MethodDecl m = (MethodDecl)i2.next();
        putSimpleSetElement(map, m.signature(), m);
      }
    }
    return map;
  }

    // Declared in LookupMethod.jrag at line 291
    public HashMap methodsSignatureMap() {
        if(methodsSignatureMap_computed)
            return methodsSignatureMap_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        methodsSignatureMap_value = methodsSignatureMap_compute();
        if(isFinal && num == boundariesCrossed)
            methodsSignatureMap_computed = true;
        return methodsSignatureMap_value;
    }

    private HashMap methodsSignatureMap_compute()  {
    HashMap map = new HashMap(localMethodsSignatureMap());
    if(hasSuperclass()) {
      for(Iterator iter = superclass().methodsIterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        if(!m.isPrivate() && m.accessibleFrom(this) && !localMethodsSignatureMap().containsKey(m.signature()))
          putSimpleSetElement(map, m.signature(), m);
      }
    }
    for(Iterator outerIter = interfacesIterator(); outerIter.hasNext(); ) {
      TypeDecl typeDecl = (TypeDecl)outerIter.next();
      for(Iterator iter = typeDecl.methodsIterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        if(!m.isPrivate() && m.accessibleFrom(this) && !localMethodsSignatureMap().containsKey(m.signature()))
          if(allMethodsAbstract((SimpleSet)map.get(m.signature())))
            putSimpleSetElement(map, m.signature(), m);
      }
    }
    return map;
  }

    // Declared in LookupMethod.jrag at line 349
    public SimpleSet ancestorMethods(String signature) {
        Object _parameters = signature;
if(ancestorMethods_String_values == null) ancestorMethods_String_values = new java.util.HashMap(4);
        if(ancestorMethods_String_values.containsKey(_parameters))
            return (SimpleSet)ancestorMethods_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet ancestorMethods_String_value = ancestorMethods_compute(signature);
        if(isFinal && num == boundariesCrossed)
            ancestorMethods_String_values.put(_parameters, ancestorMethods_String_value);
        return ancestorMethods_String_value;
    }

    private SimpleSet ancestorMethods_compute(String signature)  {
    SimpleSet set = SimpleSet.emptySet;
    if(hasSuperclass()) {
      for(Iterator iter = superclass().localMethodsSignature(signature).iterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        if(!m.isPrivate())
          set = set.add(m);
      }
    }
    if(set.size() != 1 || ((MethodDecl)set.iterator().next()).isAbstract()) { 
      for(Iterator iter = interfacesMethodsSignature(signature).iterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        set = set.add(m);
      }
    }
    if(!hasSuperclass()) return set;
    if(set.size() == 1) {
      MethodDecl m = (MethodDecl)set.iterator().next();
      if(!m.isAbstract()) {
        boolean done = true;
        for(Iterator iter = superclass().ancestorMethods(signature).iterator(); iter.hasNext(); ) {
          MethodDecl n = (MethodDecl)iter.next();
          if(n.isPrivate() || !n.accessibleFrom(m.hostType()))
            done = false;
        }
        if(done) return set;
      }
    }
    for(Iterator iter = superclass().ancestorMethods(signature).iterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      set = set.add(m);
    }
    return set;
  }

    // Declared in LookupType.jrag at line 343
    public SimpleSet memberTypes(String name) {
        Object _parameters = name;
if(memberTypes_String_values == null) memberTypes_String_values = new java.util.HashMap(4);
        if(memberTypes_String_values.containsKey(_parameters))
            return (SimpleSet)memberTypes_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet memberTypes_String_value = memberTypes_compute(name);
        if(isFinal && num == boundariesCrossed)
            memberTypes_String_values.put(_parameters, memberTypes_String_value);
        return memberTypes_String_value;
    }

    private SimpleSet memberTypes_compute(String name)  {
    SimpleSet set = SimpleSet.emptySet;
    for(int i = 0; i < getNumBodyDecl(); i++) {
      if(getBodyDecl(i).declaresType(name)) {
        set = set.add(getBodyDecl(i).type(name));
      }
    }
    if(!set.isEmpty()) return set;
    for(Iterator outerIter = interfacesIterator(); outerIter.hasNext(); ) {
      TypeDecl type = (TypeDecl)outerIter.next();
      for(Iterator iter = type.memberTypes(name).iterator(); iter.hasNext(); ) {
        TypeDecl decl = (TypeDecl)iter.next();
        if(!decl.isPrivate() && decl.accessibleFrom(this))
          set = set.add(decl);
      }
    }
    if(hasSuperclass()) {
      for(Iterator iter = superclass().memberTypes(name).iterator(); iter.hasNext(); ) {
        TypeDecl decl = (TypeDecl)iter.next();
        if(!decl.isPrivate() && decl.accessibleFrom(this)) {
          set = set.add(decl);
        }
      }
    }
    return set;
  }

    // Declared in LookupVariable.jrag at line 290
    public SimpleSet fields(String name) {
        SimpleSet fields_String_value = fields_compute(name);
        return fields_String_value;
    }

    private SimpleSet fields_compute(String name)  {
    SimpleSet fields = localFields(name);
    if(!fields.isEmpty())
      return fields; // this causes hiding of fields in superclass and interfaces
    if(hasSuperclass()) {
      for(Iterator iter = superclass().fields(name).iterator(); iter.hasNext(); ) {
        FieldDeclaration decl = (FieldDeclaration)iter.next();
        if(!decl.isPrivate() && decl.accessibleFrom(this))
          fields = fields.add(decl);
      }
    }
    for(Iterator outerIter = interfacesIterator(); outerIter.hasNext(); ) {
      TypeDecl type = (TypeDecl)outerIter.next();
      for(Iterator iter = type.fields(name).iterator(); iter.hasNext(); ) {
        FieldDeclaration decl = (FieldDeclaration)iter.next();
        if(!decl.isPrivate() && decl.accessibleFrom(this))
          fields = fields.add(decl);
      }
    }
    return fields;
  }

    // Declared in Modifiers.jrag at line 8
    public Collection unimplementedMethods() {
        if(unimplementedMethods_computed)
            return unimplementedMethods_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        unimplementedMethods_value = unimplementedMethods_compute();
        if(isFinal && num == boundariesCrossed)
            unimplementedMethods_computed = true;
        return unimplementedMethods_value;
    }

    private Collection unimplementedMethods_compute()  {
    Collection c = new ArrayList();
    for(Iterator iter = interfacesMethodsIterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      boolean implemented = false;
      SimpleSet set = (SimpleSet)localMethodsSignature(m.signature());
      if(set.size() == 1) {
        MethodDecl n = (MethodDecl)set.iterator().next();
        if(!n.isAbstract())
          implemented = true;
      }
      if(!implemented) {
        set = (SimpleSet)ancestorMethods(m.signature());
        for(Iterator i2 = set.iterator(); i2.hasNext(); ) {
          MethodDecl n = (MethodDecl)i2.next();
          if(!n.isAbstract())
            implemented = true;
        }
      }
      if(!implemented) {
        c.add(m);
      }
    }

    if(hasSuperclass()) {
      for(Iterator iter = superclass().unimplementedMethods().iterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        SimpleSet set = (SimpleSet)localMethodsSignature(m.signature());
        if(set.size() == 1) {
          MethodDecl n = (MethodDecl)set.iterator().next();
          if(n.isAbstract() || !n.overrides(m))
            c.add(m);
        }
        else
          c.add(m);
      }
    }

    for(Iterator iter = localMethodsIterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(m.isAbstract()) {
        c.add(m);
      }
    }
    return c;
  }

    // Declared in Modifiers.jrag at line 55
    public boolean hasAbstract() {
        if(hasAbstract_computed)
            return hasAbstract_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        hasAbstract_value = hasAbstract_compute();
        if(isFinal && num == boundariesCrossed)
            hasAbstract_computed = true;
        return hasAbstract_value;
    }

    private boolean hasAbstract_compute() {  return  !unimplementedMethods().isEmpty();  }

    // Declared in TypeAnalysis.jrag at line 76
    public boolean castingConversionTo(TypeDecl type) {
        Object _parameters = type;
if(castingConversionTo_TypeDecl_values == null) castingConversionTo_TypeDecl_values = new java.util.HashMap(4);
        if(castingConversionTo_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)castingConversionTo_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean castingConversionTo_TypeDecl_value = castingConversionTo_compute(type);
        if(isFinal && num == boundariesCrossed)
            castingConversionTo_TypeDecl_values.put(_parameters, Boolean.valueOf(castingConversionTo_TypeDecl_value));
        return castingConversionTo_TypeDecl_value;
    }

    private boolean castingConversionTo_compute(TypeDecl type)  {
    if(type.isArrayDecl()) {
      return isObject();
    }
    else if(type.isClassDecl()) {
      return this == type || instanceOf(type) || type.instanceOf(this);
    }
    else if(type.isInterfaceDecl()) {
      return !isFinal() || instanceOf(type);
    }
    else return super.castingConversionTo(type);
  }

    // Declared in TypeAnalysis.jrag at line 202
    public boolean isClassDecl() {
        boolean isClassDecl_value = isClassDecl_compute();
        return isClassDecl_value;
    }

    private boolean isClassDecl_compute() {  return  true;  }

    // Declared in TypeAnalysis.jrag at line 217
    public boolean isString() {
        if(isString_computed)
            return isString_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isString_value = isString_compute();
        if(isFinal && num == boundariesCrossed)
            isString_computed = true;
        return isString_value;
    }

    private boolean isString_compute() {  return  fullName().equals("java.lang.String");  }

    // Declared in TypeAnalysis.jrag at line 220
    public boolean isObject() {
        if(isObject_computed)
            return isObject_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isObject_value = isObject_compute();
        if(isFinal && num == boundariesCrossed)
            isObject_computed = true;
        return isObject_value;
    }

    private boolean isObject_compute() {  return  name().equals("Object") && packageName().equals("java.lang");  }

    // Declared in TypeAnalysis.jrag at line 408
    public boolean instanceOf(TypeDecl type) {
        Object _parameters = type;
if(instanceOf_TypeDecl_values == null) instanceOf_TypeDecl_values = new java.util.HashMap(4);
        if(instanceOf_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)instanceOf_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean instanceOf_TypeDecl_value = instanceOf_compute(type);
        if(isFinal && num == boundariesCrossed)
            instanceOf_TypeDecl_values.put(_parameters, Boolean.valueOf(instanceOf_TypeDecl_value));
        return instanceOf_TypeDecl_value;
    }

    private boolean instanceOf_compute(TypeDecl type) {  return  type.isSupertypeOfClassDecl(this);  }

    // Declared in TypeAnalysis.jrag at line 423
    public boolean isSupertypeOfClassDecl(ClassDecl type) {
        boolean isSupertypeOfClassDecl_ClassDecl_value = isSupertypeOfClassDecl_compute(type);
        return isSupertypeOfClassDecl_ClassDecl_value;
    }

    private boolean isSupertypeOfClassDecl_compute(ClassDecl type)  {
    if(super.isSupertypeOfClassDecl(type))
      return true;
    return type.hasSuperclass() && type.superclass() != null && type.superclass().instanceOf(this);
  }

    // Declared in TypeAnalysis.jrag at line 440
    public boolean isSupertypeOfInterfaceDecl(InterfaceDecl type) {
        boolean isSupertypeOfInterfaceDecl_InterfaceDecl_value = isSupertypeOfInterfaceDecl_compute(type);
        return isSupertypeOfInterfaceDecl_InterfaceDecl_value;
    }

    private boolean isSupertypeOfInterfaceDecl_compute(InterfaceDecl type) {  return  isObject();  }

    // Declared in TypeAnalysis.jrag at line 453
    public boolean isSupertypeOfArrayDecl(ArrayDecl type) {
        boolean isSupertypeOfArrayDecl_ArrayDecl_value = isSupertypeOfArrayDecl_compute(type);
        return isSupertypeOfArrayDecl_ArrayDecl_value;
    }

    private boolean isSupertypeOfArrayDecl_compute(ArrayDecl type)  {
    if(super.isSupertypeOfArrayDecl(type))
      return true;
    return type.hasSuperclass() && type.superclass() != null && type.superclass().instanceOf(this);
  }

    // Declared in TypeAnalysis.jrag at line 529
    public boolean isInnerClass() {
        boolean isInnerClass_value = isInnerClass_compute();
        return isInnerClass_value;
    }

    private boolean isInnerClass_compute() {  return  isNestedType() && !isStatic() && enclosingType().isClassDecl();  }

    protected boolean isCircular_visited = false;
    protected boolean isCircular_computed = false;
    protected boolean isCircular_initialized = false;
    protected boolean isCircular_value;
    public boolean isCircular() {
        if(isCircular_computed)
            return isCircular_value;
        if (!isCircular_initialized) {
            isCircular_initialized = true;
            isCircular_value = true;
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isCircular_visited = true;
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            do {
                CHANGE = false;
                boolean new_isCircular_value = isCircular_compute();
                if (new_isCircular_value!=isCircular_value)
                    CHANGE = true;
                isCircular_value = new_isCircular_value; 
            } while (CHANGE);
            isCircular_visited = false;
            if(isFinal && num == boundariesCrossed)
{
            isCircular_computed = true;
            }
            else {
            RESET_CYCLE = true;
            isCircular_compute();
            RESET_CYCLE = false;
              isCircular_computed = false;
              isCircular_initialized = false;
            }
            IN_CIRCLE = false; 
            return isCircular_value;
        }
        if(!isCircular_visited) {
            if (RESET_CYCLE) {
                isCircular_computed = false;
                isCircular_initialized = false;
                return isCircular_value;
            }
            isCircular_visited = true;
            boolean new_isCircular_value = isCircular_compute();
            if (new_isCircular_value!=isCircular_value)
                CHANGE = true;
            isCircular_value = new_isCircular_value; 
            isCircular_visited = false;
            return isCircular_value;
        }
        return isCircular_value;
    }

    private boolean isCircular_compute()  {
    if(hasSuperClassAccess()) {
      Access a = getSuperClassAccess().lastAccess();
      while(a != null) {
        if(a.type().isCircular())
          return true;
        a = (a.isQualified() && a.qualifier().isTypeAccess()) ? (TypeAccess)a.qualifier() : null;
      }
    }
    for(int i = 0; i < getNumImplements(); i++) {
      Access a = getImplements(i).lastAccess();
      while(a != null) {
        if(a.type().isCircular())
          return true;
        a = (a.isQualified() && a.qualifier().isTypeAccess()) ? (TypeAccess)a.qualifier() : null;
      }
    }
    return false;
  }

    // Declared in AccessField.jrag at line 24
    public TypeDecl findFieldUpwards(FieldDeclaration fd) {
        TypeDecl findFieldUpwards_FieldDeclaration_value = findFieldUpwards_compute(fd);
        return findFieldUpwards_FieldDeclaration_value;
    }

    private TypeDecl findFieldUpwards_compute(FieldDeclaration fd)  {
		if(localFieldsMap().containsValue(fd))
			return this;
		boolean shadowed = localFieldsMap().containsKey(fd.getID());
		if(hasSuperclass()) {
			TypeDecl td = superclass().findFieldUpwards(fd);
			if(td == superclass() && !shadowed)
				return this;
			else if(td != null)
				return td;
		}
		for(Iterator i = interfacesIterator(); i.hasNext(); ) {
			InterfaceDecl idecl = (InterfaceDecl)i.next();
			TypeDecl td = idecl.findFieldUpwards(fd);
			if(td == idecl && !shadowed)
				return this;
			else if(td != null)
				return td;
		}
		return null;
	}

    // Declared in TypeAnalysis.jrag at line 569
    public TypeDecl Define_TypeDecl_hostType(ASTNode caller, ASTNode child) {
        if(caller == getImplementsListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  hostType();
        }
        if(caller == getSuperClassAccessOptNoTransform()) {
            return  hostType();
        }
        return super.Define_TypeDecl_hostType(caller, child);
    }

    // Declared in SyntacticClassification.jrag at line 64
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getImplementsListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.TYPE_NAME;
        }
        if(caller == getSuperClassAccessOptNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return super.Define_NameType_nameType(caller, child);
    }

    // Declared in Modifiers.jrag at line 244
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return super.Define_boolean_mayBeFinal(caller, child);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
