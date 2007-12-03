
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

 
public abstract class TypeDecl extends ASTNode implements Cloneable,  SimpleSet,  Iterator,  VariableScope {
    public void flushCache() {
        super.flushCache();
        accessibleFromPackage_String_values = null;
        accessibleFromExtend_TypeDecl_values = null;
        accessibleFrom_TypeDecl_values = null;
        dimension_computed = false;
        elementType_computed = false;
        elementType_value = null;
        arrayType_computed = false;
        arrayType_value = null;
        isException_computed = false;
        isCheckedException_computed = false;
        isUncheckedException_computed = false;
        mayCatch_TypeDecl_values = null;
        constructors_computed = false;
        constructors_value = null;
        unqualifiedLookupMethod_String_values = null;
        methodsNameMap_computed = false;
        methodsNameMap_value = null;
        localMethodsSignatureMap_computed = false;
        localMethodsSignatureMap_value = null;
        methodsSignatureMap_computed = false;
        methodsSignatureMap_value = null;
        ancestorMethods_String_values = null;
        memberTypes_String_values = null;
        memberFields_String_values = null;
        localFieldsMap_computed = false;
        localFieldsMap_value = null;
        hasAbstract_computed = false;
        unimplementedMethods_computed = false;
        unimplementedMethods_value = null;
        isPublic_computed = false;
        isStatic_computed = false;
        fullName_computed = false;
        fullName_value = null;
        typeName_computed = false;
        typeName_value = null;
        narrowingConversionTo_TypeDecl_values = null;
        methodInvocationConversionTo_TypeDecl_values = null;
        castingConversionTo_TypeDecl_values = null;
        isString_computed = false;
        isObject_computed = false;
        instanceOf_TypeDecl_values = null;
        isCircular_computed = false;
        componentType_computed = false;
        componentType_value = null;
        isDAbefore_Variable_values = null;
        isDUbefore_Variable_values = null;
        typeException_computed = false;
        typeException_value = null;
        typeRuntimeException_computed = false;
        typeRuntimeException_value = null;
        typeError_computed = false;
        typeError_value = null;
        lookupMethod_String_values = null;
        typeObject_computed = false;
        typeObject_value = null;
        lookupType_String_values = null;
        lookupVariable_String_values = null;
        packageName_computed = false;
        packageName_value = null;
        isAnonymous_computed = false;
        unknownType_computed = false;
        unknownType_value = null;
        inExplicitConstructorInvocation_computed = false;
        inStaticContext_computed = false;
        accessField_FieldDeclaration_values = null;
        accessMethod_MethodDecl_List_values = null;
        accessType_TypeDecl_boolean_values = null;
        TypeDecl_uses_visited = false;
        TypeDecl_uses_computed = false;
        TypeDecl_uses_value = null;
    TypeDecl_uses_contributors = new java.util.HashSet();
    }
    public Object clone() throws CloneNotSupportedException {
        TypeDecl node = (TypeDecl)super.clone();
        node.accessibleFromPackage_String_values = null;
        node.accessibleFromExtend_TypeDecl_values = null;
        node.accessibleFrom_TypeDecl_values = null;
        node.dimension_computed = false;
        node.elementType_computed = false;
        node.elementType_value = null;
        node.arrayType_computed = false;
        node.arrayType_value = null;
        node.isException_computed = false;
        node.isCheckedException_computed = false;
        node.isUncheckedException_computed = false;
        node.mayCatch_TypeDecl_values = null;
        node.constructors_computed = false;
        node.constructors_value = null;
        node.unqualifiedLookupMethod_String_values = null;
        node.methodsNameMap_computed = false;
        node.methodsNameMap_value = null;
        node.localMethodsSignatureMap_computed = false;
        node.localMethodsSignatureMap_value = null;
        node.methodsSignatureMap_computed = false;
        node.methodsSignatureMap_value = null;
        node.ancestorMethods_String_values = null;
        node.memberTypes_String_values = null;
        node.memberFields_String_values = null;
        node.localFieldsMap_computed = false;
        node.localFieldsMap_value = null;
        node.hasAbstract_computed = false;
        node.unimplementedMethods_computed = false;
        node.unimplementedMethods_value = null;
        node.isPublic_computed = false;
        node.isStatic_computed = false;
        node.fullName_computed = false;
        node.fullName_value = null;
        node.typeName_computed = false;
        node.typeName_value = null;
        node.narrowingConversionTo_TypeDecl_values = null;
        node.methodInvocationConversionTo_TypeDecl_values = null;
        node.castingConversionTo_TypeDecl_values = null;
        node.isString_computed = false;
        node.isObject_computed = false;
        node.instanceOf_TypeDecl_values = null;
        node.isCircular_computed = false;
        node.componentType_computed = false;
        node.componentType_value = null;
        node.isDAbefore_Variable_values = null;
        node.isDUbefore_Variable_values = null;
        node.typeException_computed = false;
        node.typeException_value = null;
        node.typeRuntimeException_computed = false;
        node.typeRuntimeException_value = null;
        node.typeError_computed = false;
        node.typeError_value = null;
        node.lookupMethod_String_values = null;
        node.typeObject_computed = false;
        node.typeObject_value = null;
        node.lookupType_String_values = null;
        node.lookupVariable_String_values = null;
        node.packageName_computed = false;
        node.packageName_value = null;
        node.isAnonymous_computed = false;
        node.unknownType_computed = false;
        node.unknownType_value = null;
        node.inExplicitConstructorInvocation_computed = false;
        node.inStaticContext_computed = false;
        node.accessField_FieldDeclaration_values = null;
        node.accessMethod_MethodDecl_List_values = null;
        node.accessType_TypeDecl_boolean_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in AnonymousClasses.jrag at line 19

  
  public int anonymousIndex = 0;

    // Declared in AnonymousClasses.jrag at line 36


  public int nextAnonymousIndex() {
    if(isNestedType())
      return enclosingType().nextAnonymousIndex();
    return anonymousIndex++;
  }

    // Declared in BoundNames.jrag at line 15


  // The memberMethods(String name) attribute is used to lookup member methods.
  // It uses the methodsNameMap() map where a name is mapped to a list of member
  // methods. We extend the map with the declaration m by either appending
  // it to an existing list of declarations or adding a new list. That list
  // will be used to name bind a new qualified name access.
  public MethodDecl addMemberMethod(MethodDecl m) {
    addBodyDecl(m);
    return (MethodDecl)getBodyDecl(getNumBodyDecl()-1);
    /*
    HashMap map = methodsNameMap();
    ArrayList list = (ArrayList)map.get(m.name());
    if(list == null) {
      list = new ArrayList(4);
      map.put(m.name(), list);
    }
    list.add(m);
    if(!memberMethods(m.name()).contains(m))
      throw new Error("The method " + m.signature() + " added to " + typeName() + " can not be found using lookupMemberMethod");
    */
  }

    // Declared in BoundNames.jrag at line 31


  public ConstructorDecl addConstructor(ConstructorDecl c) {
    addBodyDecl(c);
    return (ConstructorDecl)getBodyDecl(getNumBodyDecl()-1);
  }

    // Declared in BoundNames.jrag at line 36


  public ClassDecl addMemberClass(ClassDecl c) {
    addBodyDecl(new MemberClassDecl(c));
    return ((MemberClassDecl)getBodyDecl(getNumBodyDecl()-1)).getClassDecl();
  }

    // Declared in BoundNames.jrag at line 43



  // the new field must be unique otherwise an error occurs
  public FieldDeclaration addMemberField(FieldDeclaration f) {
    addBodyDecl(f);
    return (FieldDeclaration)getBodyDecl(getNumBodyDecl()-1);
    //if(!memberFields(f.name()).contains(f))
    //  throw new Error("The field " + f.name() + " added to " + typeName() + " can not be found using lookupMemberField");
  }

    // Declared in BoundNames.jrag at line 81


  public TypeAccess createBoundAccess() {
    return new BoundTypeAccess("", name(), this);
  }

    // Declared in DataStructures.jrag at line 109

  public SimpleSet add(Object o) {
    return new SimpleSetImpl().add(this).add(o);
  }

    // Declared in DataStructures.jrag at line 115

  private TypeDecl iterElem;

    // Declared in DataStructures.jrag at line 116

  public Iterator iterator() { iterElem = this; return this; }

    // Declared in DataStructures.jrag at line 117

  public boolean hasNext() { return iterElem != null; }

    // Declared in DataStructures.jrag at line 118

  public Object next() { Object o = iterElem; iterElem = null; return o; }

    // Declared in DataStructures.jrag at line 119

  public void remove() { throw new UnsupportedOperationException(); }

    // Declared in DeclareBeforeUse.jrag at line 32


  public boolean declaredBeforeUse(Variable decl, ASTNode use) {
    int indexDecl = ((ASTNode)decl).varChildIndex(this);
    int indexUse = use.varChildIndex(this);
    return indexDecl < indexUse;
  }

    // Declared in DeclareBeforeUse.jrag at line 37

  public boolean declaredBeforeUse(Variable decl, int indexUse) {
    int indexDecl = ((ASTNode)decl).varChildIndex(this);
    return indexDecl < indexUse;
  }

    // Declared in LookupConstructor.jrag at line 79

  public ConstructorDecl lookupConstructor(ConstructorDecl signature) {
    for(Iterator iter = constructors().iterator(); iter.hasNext(); ) {
      ConstructorDecl decl = (ConstructorDecl)iter.next();
      if(decl.sameSignature(signature)) {
        return decl;
      }
    }
    return null;
  }

    // Declared in LookupMethod.jrag at line 200



  public Iterator localMethodsIterator() {
    return new Iterator() {
      private Iterator outer = localMethodsSignatureMap().values().iterator();
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
    //return localMethodsSignatureMap().values().iterator();
  }

    // Declared in LookupMethod.jrag at line 268


  // iterate over all member methods in this type
  public Iterator methodsIterator() {
    return new Iterator() {
      private Iterator outer = methodsSignatureMap().values().iterator();
      private Iterator inner = null;
      public boolean hasNext() {
        if((inner == null || !inner.hasNext()) && outer.hasNext())
          inner = ((SimpleSet)outer.next()).iterator();
        return inner != null ? inner.hasNext() : false;
      }
      public Object next() {
        return inner.next();
      }
      public void remove() { throw new UnsupportedOperationException(); }
    };
  }

    // Declared in LookupMethod.jrag at line 333

  protected boolean allMethodsAbstract(SimpleSet set) {
    if(set == null) return true;
    for(Iterator iter = set.iterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(!m.isAbstract())
        return false;
    }
    return true;
  }

    // Declared in LookupVariable.jrag at line 210

  
  public TypeDecl subclassWithinBody(TypeDecl typeDecl) {
    if(instanceOf(typeDecl))
      return this;
    if(isNestedType()) {
      return enclosingType().subclassWithinBody(typeDecl);
    }
    return null;
  }

    // Declared in Modifiers.jrag at line 57


  public void checkModifiers() {
    super.checkModifiers();
    // 8.1.1
    if(isPublic() && !isTopLevelType() && !isMemberType())
      error("public pertains only to top level types and member types");

    // 8.1.1
    if((isProtected() || isPrivate()) && !(isMemberType() && enclosingType().isClassDecl()))
      error("protected and private may only be used on member types within a directly enclosing class declaration");

    // 8.1.1
    if(isStatic() && !isMemberType())
      error("static pertains only to member types");
    
    
    // 8.4.3.1
    // 8.1.1.1
    if(!isAbstract() && hasAbstract()) {
      StringBuffer s = new StringBuffer();
      s.append("" + name() + " is not declared abstract but contains abstract members: \n");
      for(Iterator iter = unimplementedMethods().iterator(); iter.hasNext(); ) {
        MethodDecl m = (MethodDecl)iter.next();
        s.append("  " + m.signature() + " in " + m.hostType().typeName() + "\n");
      }
      error(s.toString());
    }
  }

    // Declared in NameCheck.jrag at line 234


  public void nameCheck() {
    if(isTopLevelType() && lookupType(packageName(), name()) != this)
      error("duplicate member " + name() + " in compilation unit");
  
    if(!isTopLevelType() && !isAnonymous() && !isLocalClass() && extractSingleType(enclosingType().memberTypes(name())) != this)
      error("duplicate member type " + name() + " in type " + enclosingType().typeName());

    // 14.3
    if(isLocalClass()) {
      TypeDecl typeDecl = extractSingleType(lookupType(name()));
      if(typeDecl != null && typeDecl != this && typeDecl.isLocalClass() && enclosingBlock() == typeDecl.enclosingBlock())
        error("local class named " + name() + " may not be redeclared as a local class in the same block");
    }

    if(!packageName().equals("") && hasPackage(fullName()))
      error("duplicate member class and package " + name());
    
    // 8.1 & 9.1
    if(hasEnclosingTypeDecl(name())) {
      error("type may not have the same simple name as an enclosing type declaration");
    }
  }

    // Declared in QualifiedNames.jrag at line 87

  public Access createQualifiedAccess() {
    if(isLocalClass() || isAnonymous()) {
      return new TypeAccess(name());
    }
    else if(!isTopLevelType()) {
      return enclosingType().createQualifiedAccess().qualifiesAccess(new TypeAccess(name()));
    }
    else {
      return new TypeAccess(packageName(), name());
    }
  }

    // Declared in TypeAnalysis.jrag at line 226

  public FieldDeclaration findSingleVariable(String name) {
    return (FieldDeclaration)fields(name).iterator().next();
  }

    // Declared in TypeHierarchyCheck.jrag at line 148


  public void typeCheck() {
    // 8.4.6.4 & 9.4.1
    for(Iterator iter1 = localMethodsIterator(); iter1.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter1.next();
      ASTNode target = m.hostType() == this ? (ASTNode)m : (ASTNode)this;
      
      //for(Iterator i2 = overrides(m).iterator(); i2.hasNext(); ) {
      for(Iterator i2 = ancestorMethods(m.signature()).iterator(); i2.hasNext(); ) {
        MethodDecl decl = (MethodDecl)i2.next();
        if(m.overrides(decl)) {
          // 8.4.6.1
          if(!m.isStatic() && decl.isStatic())
            target.error("an instance method may not override a static method");
 
          // regardless of overriding
          // 8.4.6.3
          if(!m.mayOverrideReturn(decl))
            target.error("the return type of method " + m.signature() + " in " + m.hostType().typeName() + " does not match the return type of method " + decl.signature() + " in " + decl.hostType().typeName() + " and may thus not be overriden");
 
          // regardless of overriding
          // 8.4.4
          for(int i = 0; i < m.getNumException(); i++) {
            Access e = m.getException(i);
            boolean found = false;
            for(int j = 0; !found && j < decl.getNumException(); j++) {
              if(e.type().instanceOf(decl.getException(j).type()))
                found = true;
            }
            if(!found && e.type().isUncheckedException())
              target.error(m.signature() + " in " + m.hostType().typeName() + " may not throw more checked exceptions than overridden method " +
               decl.signature() + " in " + decl.hostType().typeName());
          }
          // 8.4.6.3
          if(decl.isPublic() && !m.isPublic())
            target.error("overriding access modifier error");
          // 8.4.6.3
          if(decl.isProtected() && !(m.isPublic() || m.isProtected()))
            target.error("overriding access modifier error");
          // 8.4.6.3
          if((!decl.isPrivate() && !decl.isProtected() && !decl.isPublic()) && m.isPrivate())
            target.error("overriding access modifier error");
 
          // regardless of overriding
          if(decl.isFinal())
            target.error("method " + m.signature() + " in " + hostType().typeName() + " can not override final method " + decl.signature() + " in " + decl.hostType().typeName());
        }
        if(m.hides(decl)) {
          // 8.4.6.2
          if(m.isStatic() && !decl.isStatic())
            target.error("a static method may not hide an instance method");
          // 8.4.6.3
          if(m.type() != decl.type())
            target.error("can not hide a method with a different return type");
          // 8.4.4
          for(int i = 0; i < m.getNumException(); i++) {
            Access e = m.getException(i);
            boolean found = false;
            for(int j = 0; !found && j < decl.getNumException(); j++) {
              if(e.type().instanceOf(decl.getException(j).type()))
                found = true;
            }
            if(!found)
              target.error("may not throw more checked exceptions than hidden method");
          }
          // 8.4.6.3
          if(decl.isPublic() && !m.isPublic())
            target.error("hiding access modifier error: public method " + decl.signature() + " in " + decl.hostType().typeName() + " is hidden by non public method " + m.signature() + " in " + m.hostType().typeName());
          // 8.4.6.3
          if(decl.isProtected() && !(m.isPublic() || m.isProtected()))
            target.error("hiding access modifier error: protected method " + decl.signature() + " in " + decl.hostType().typeName() + " is hidden by non (public|protected) method " + m.signature() + " in " + m.hostType().typeName());
          // 8.4.6.3
          if((!decl.isPrivate() && !decl.isProtected() && !decl.isPublic()) && m.isPrivate())
            target.error("hiding access modifier error: default method " + decl.signature() + " in " + decl.hostType().typeName() + " is hidden by private method " + m.signature() + " in " + m.hostType().typeName());
          if(decl.isFinal())
            target.error("method " + m.signature() + " in " + hostType().typeName() + " can not hide final method " + decl.signature() + " in " + decl.hostType().typeName());
        }
      }
    }
  }

    // Declared in AddMethod.jrag at line 5


	public void addMethod(java.util.List changes, MethodDecl md, String sig, boolean canCapture) throws RefactoringException {
		throw new RefactoringException("cannot add method");
	}

    // Declared in RenameType.jrag at line 3

	
	public java.util.List rename(String new_name) throws RefactoringException {
		java.util.List changes = new java.util.Vector();
		if(getID().equals(new_name))
			return changes;
		CompilationUnit cu = compilationUnit();
		checkRenamingPreconds(new_name);
		if(isTopLevelType() && /*isPublic()*/ getID().equals(cu.getID()))
			changes.add(new CompilationUnitRename(cu, new_name));
		adjustImportDecls(changes);
		String old_name = getID();
		AdjustmentTable table = find_uses(new_name);
		setID(new_name);
		changes.add(new TypeRename(this, new_name));
		programRoot().clear();
		try {
			table.adjust(changes);
		} finally {
			setID(old_name);
			programRoot().clear();
		}
		return changes;
	}

    // Declared in RenameType.jrag at line 26

	
	private AdjustmentTable find_uses(String new_name) {
		AdjustmentTable table = new AdjustmentTable();
		/* first, collect all uses of the type we are renaming */
		for(Iterator i = uses().iterator(); i.hasNext();) {
			Access a = (Access)i.next();
			table.add(a, this);
		}
		/* now, collect all uses of types and packages that the type
		 * might be shadowing after renaming */
		for(Iterator i = lookupType(new_name).iterator(); i.hasNext();) {
			TypeDecl d = (TypeDecl)i.next();
			for(Iterator j = d.uses().iterator(); j.hasNext();) {
				Access acc = (Access)j.next();
				table.add(acc, d);
			}
		}
		PackageDecl pd = programRoot().getPackageDecl(new_name);
		if(pd != null)
			for(Iterator j = pd.prefixUses().iterator(); j.hasNext();) {
				Access acc = (Access)j.next();
				if(acc.nameType() == NameType.AMBIGUOUS_NAME ||
						acc.nameType() == NameType.PACKAGE_OR_TYPE_NAME)
					table.add(acc, pd);
			}
		return table;
	}

    // Declared in RenameType.jrag at line 53

	
	private void checkRenamingPreconds(String new_name) throws RefactoringException {
        if(hasNestedType(new_name))
            throw new RefactoringException("nested type of the same name exists");
		if(isNestedType()) {
			TypeDecl enc = enclosingType();
			if(!enc.memberTypes(new_name).isEmpty())
				throw new RefactoringException("type of the same name exists in enclosing type");
			if(hasEnclosingType(new_name))
				throw new RefactoringException("enclosing type of the same name exists");
		} else if(isTopLevelType()) {
			String pkg = hostPackage();
            if(lookupType(pkg, new_name) != null)
				throw new RefactoringException("type of the same name exists in enclosing package");
			if(programRoot().hasPackage(pkg+"."+new_name))
				throw new RefactoringException("sub-package of the same name exists in enclosing package");
		}
	}

    // Declared in RenameType.jrag at line 71

	
    private boolean hasNestedType(String name) {
        for(int i=0;i<getNumBodyDecl();++i) {
            BodyDecl d = getBodyDecl(i);
            if(d instanceof MemberTypeDecl) {
                TypeDecl a = ((MemberTypeDecl)d).typeDecl();
                if(a.getID().equals(name) || a.hasNestedType(name))
                    return true;
            }
        }
        return false;
    }

    // Declared in RenameType.jrag at line 83

    
    private boolean hasEnclosingType(String name) {
    	if(!isNestedType()) return false;
    	TypeDecl enc = enclosingType();
    	if(enc.getID().equals(name))
    		return true;
    	return enc.hasEnclosingType(name);
    }

    // Declared in RenameType.jrag at line 91


	private void adjustImportDecls(java.util.List changes) throws RefactoringException {
		Program p = programRoot();
		for(int i = 0; i < p.getNumCompilationUnit(); i++) {
			for(int j=0;i<p.getCompilationUnit(i).getNumImportDecl();++j) {
				ImportDecl id = p.getCompilationUnit(i).getImportDecl(j);
				if(id instanceof SingleTypeImportDecl) {
					Access acc = ((SingleTypeImportDecl)id).getAccess();
					if(acc.type() == this) {
						Access tacc = id.accessType(this, false);
						if(tacc == null)
							throw new RefactoringException("couldn't access type "+this+" from import decl "+id);
						changes.add(new NodeReplace(id, new SingleTypeImportDecl(tacc)));
					}
				}
			}
		}
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 37

    public TypeDecl() {
        super();

        setChild(null, 0);
        setChild(new List(), 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 37
    public TypeDecl(Modifiers p0, String p1, List p2) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 18


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 21

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 37
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
    // Declared in java.ast line 37
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
    // Declared in java.ast line 37
    public void setBodyDeclList(List list) {
        setChild(list, 1);
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
        return (List)getChild(1);
    }

    // Declared in java.ast at line 28


    public List getBodyDeclListNoTransform() {
        return (List)getChildNoTransform(1);
    }

    protected java.util.Map accessibleFromPackage_String_values;
    // Declared in AccessControl.jrag at line 6
    public boolean accessibleFromPackage(String packageName) {
        Object _parameters = packageName;
if(accessibleFromPackage_String_values == null) accessibleFromPackage_String_values = new java.util.HashMap(4);
        if(accessibleFromPackage_String_values.containsKey(_parameters))
            return ((Boolean)accessibleFromPackage_String_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessibleFromPackage_String_value = accessibleFromPackage_compute(packageName);
        if(isFinal && num == boundariesCrossed)
            accessibleFromPackage_String_values.put(_parameters, Boolean.valueOf(accessibleFromPackage_String_value));
        return accessibleFromPackage_String_value;
    }

    private boolean accessibleFromPackage_compute(String packageName) {  return 
    !isPrivate() && (isPublic() || hostPackage().equals(packageName));  }

    protected java.util.Map accessibleFromExtend_TypeDecl_values;
    // Declared in AccessControl.jrag at line 9
    public boolean accessibleFromExtend(TypeDecl type) {
        Object _parameters = type;
if(accessibleFromExtend_TypeDecl_values == null) accessibleFromExtend_TypeDecl_values = new java.util.HashMap(4);
        if(accessibleFromExtend_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)accessibleFromExtend_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessibleFromExtend_TypeDecl_value = accessibleFromExtend_compute(type);
        if(isFinal && num == boundariesCrossed)
            accessibleFromExtend_TypeDecl_values.put(_parameters, Boolean.valueOf(accessibleFromExtend_TypeDecl_value));
        return accessibleFromExtend_TypeDecl_value;
    }

    private boolean accessibleFromExtend_compute(TypeDecl type)  {
    if(type == this)
      return true;
    if(isInnerType()) { 
      if(!enclosingType().accessibleFrom(type)) {
        return false;
      }
    }
    if(isPublic()) 
      return true;
    else if(isProtected()) {
      // isProtected implies a nested type
      if(hostPackage().equals(type.hostPackage())) {
        return true;
      }
      if(type.isNestedType() && type.enclosingType().withinBodyThatSubclasses(enclosingType()) != null)
        return true;
      return false;
    }
    else if(isPrivate()) {
      return topLevelType() == type.topLevelType();
    }
    else
      return hostPackage().equals(type.hostPackage());
  }

    protected java.util.Map accessibleFrom_TypeDecl_values;
    // Declared in AccessControl.jrag at line 35
    public boolean accessibleFrom(TypeDecl type) {
        Object _parameters = type;
if(accessibleFrom_TypeDecl_values == null) accessibleFrom_TypeDecl_values = new java.util.HashMap(4);
        if(accessibleFrom_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)accessibleFrom_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessibleFrom_TypeDecl_value = accessibleFrom_compute(type);
        if(isFinal && num == boundariesCrossed)
            accessibleFrom_TypeDecl_values.put(_parameters, Boolean.valueOf(accessibleFrom_TypeDecl_value));
        return accessibleFrom_TypeDecl_value;
    }

    private boolean accessibleFrom_compute(TypeDecl type)  {
    if(type == this)
      return true;
    if(isInnerType()) { 
      if(!enclosingType().accessibleFrom(type)) {
        return false;
      }
    }
    if(isPublic()) {  
      return true;
    }
    else if(isProtected()) {
      if(hostPackage().equals(type.hostPackage())) {
        return true;
      }
      if(isMemberType()) {
        TypeDecl typeDecl = type;
        while(typeDecl != null && !typeDecl.instanceOf(enclosingType()))
          typeDecl = typeDecl.enclosingType();
        if(typeDecl != null) {
          return true;
        }
      }
      return false;
    }
    else if(isPrivate()) {
      return topLevelType() == type.topLevelType();
    }
    else {
      return hostPackage().equals(type.hostPackage());
    }
  }

    protected boolean dimension_computed = false;
    protected int dimension_value;
    // Declared in Arrays.jrag at line 2
    public int dimension() {
        if(dimension_computed)
            return dimension_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        dimension_value = dimension_compute();
        if(isFinal && num == boundariesCrossed)
            dimension_computed = true;
        return dimension_value;
    }

    private int dimension_compute() {  return  0;  }

    protected boolean elementType_computed = false;
    protected TypeDecl elementType_value;
    // Declared in Arrays.jrag at line 7
    public TypeDecl elementType() {
        if(elementType_computed)
            return elementType_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        elementType_value = elementType_compute();
        if(isFinal && num == boundariesCrossed)
            elementType_computed = true;
        return elementType_value;
    }

    private TypeDecl elementType_compute() {  return  this;  }

    protected boolean arrayType_computed = false;
    protected TypeDecl arrayType_value;
    // Declared in Arrays.jrag at line 15
    public TypeDecl arrayType() {
        if(arrayType_computed)
            return arrayType_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        arrayType_value = arrayType_compute();
        arrayType_value.setParent(this);
        arrayType_value.is$Final = true;
        if(true)
            arrayType_computed = true;
        return arrayType_value;
    }

    private TypeDecl arrayType_compute()  {
    String name = name() + "[]";
    TypeDecl typeDecl =
      new ArrayDecl(
        new Modifiers(new List().add(new Modifier("public"))),
        name,
        new Opt(typeObject().createQualifiedAccess()), // [SuperClassAccess]
        new List().add(typeCloneable().createQualifiedAccess()).add(typeSerializable().createQualifiedAccess()), // Implements*
        new List().add( // BodyDecl*
          new FieldDeclaration(
            new Modifiers(new List().add(new Modifier("public")).add(new Modifier("final"))),
            new PrimitiveTypeAccess("int"),
            "length",
            new Opt() // [Init:Expr]
          )).add(
          new MethodDecl(
            new Modifiers(new List().add(new Modifier("public"))),
            typeObject().createQualifiedAccess(),
            "clone",
            new List(),
            new List(),
            new List(),
            new Opt(new Block())
          )
        )
      );
    return typeDecl;
  }

    // Declared in ConstantExpression.jrag at line 281
    public Constant cast(Constant c) {
        Constant cast_Constant_value = cast_compute(c);
        return cast_Constant_value;
    }

    private Constant cast_compute(Constant c)  {
    throw new UnsupportedOperationException("ConstantExpression operation cast" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 295
    public Constant plus(Constant c) {
        Constant plus_Constant_value = plus_compute(c);
        return plus_Constant_value;
    }

    private Constant plus_compute(Constant c)  {
    throw new UnsupportedOperationException("ConstantExpression operation plus" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 304
    public Constant minus(Constant c) {
        Constant minus_Constant_value = minus_compute(c);
        return minus_Constant_value;
    }

    private Constant minus_compute(Constant c)  {
    throw new UnsupportedOperationException("ConstantExpression operation minus" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 313
    public Constant bitNot(Constant c) {
        Constant bitNot_Constant_value = bitNot_compute(c);
        return bitNot_Constant_value;
    }

    private Constant bitNot_compute(Constant c)  {
    throw new UnsupportedOperationException("ConstantExpression operation bitNot" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 320
    public Constant mul(Constant c1, Constant c2) {
        Constant mul_Constant_Constant_value = mul_compute(c1, c2);
        return mul_Constant_Constant_value;
    }

    private Constant mul_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation mul" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 329
    public Constant div(Constant c1, Constant c2) {
        Constant div_Constant_Constant_value = div_compute(c1, c2);
        return div_Constant_Constant_value;
    }

    private Constant div_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation div" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 338
    public Constant mod(Constant c1, Constant c2) {
        Constant mod_Constant_Constant_value = mod_compute(c1, c2);
        return mod_Constant_Constant_value;
    }

    private Constant mod_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation mod" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 347
    public Constant add(Constant c1, Constant c2) {
        Constant add_Constant_Constant_value = add_compute(c1, c2);
        return add_Constant_Constant_value;
    }

    private Constant add_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation add" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 357
    public Constant sub(Constant c1, Constant c2) {
        Constant sub_Constant_Constant_value = sub_compute(c1, c2);
        return sub_Constant_Constant_value;
    }

    private Constant sub_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation sub" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 366
    public Constant lshift(Constant c1, Constant c2) {
        Constant lshift_Constant_Constant_value = lshift_compute(c1, c2);
        return lshift_Constant_Constant_value;
    }

    private Constant lshift_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation lshift" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 373
    public Constant rshift(Constant c1, Constant c2) {
        Constant rshift_Constant_Constant_value = rshift_compute(c1, c2);
        return rshift_Constant_Constant_value;
    }

    private Constant rshift_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation rshift" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 380
    public Constant urshift(Constant c1, Constant c2) {
        Constant urshift_Constant_Constant_value = urshift_compute(c1, c2);
        return urshift_Constant_Constant_value;
    }

    private Constant urshift_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation urshift" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 387
    public Constant andBitwise(Constant c1, Constant c2) {
        Constant andBitwise_Constant_Constant_value = andBitwise_compute(c1, c2);
        return andBitwise_Constant_Constant_value;
    }

    private Constant andBitwise_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation andBitwise" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 395
    public Constant xorBitwise(Constant c1, Constant c2) {
        Constant xorBitwise_Constant_Constant_value = xorBitwise_compute(c1, c2);
        return xorBitwise_Constant_Constant_value;
    }

    private Constant xorBitwise_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation xorBitwise" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 403
    public Constant orBitwise(Constant c1, Constant c2) {
        Constant orBitwise_Constant_Constant_value = orBitwise_compute(c1, c2);
        return orBitwise_Constant_Constant_value;
    }

    private Constant orBitwise_compute(Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation orBitwise" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 411
    public Constant questionColon(Constant cond, Constant c1, Constant c2) {
        Constant questionColon_Constant_Constant_Constant_value = questionColon_compute(cond, c1, c2);
        return questionColon_Constant_Constant_Constant_value;
    }

    private Constant questionColon_compute(Constant cond, Constant c1, Constant c2)  {
    throw new UnsupportedOperationException("ConstantExpression operation questionColon" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 515
    public boolean eqIsTrue(Expr left, Expr right) {
        boolean eqIsTrue_Expr_Expr_value = eqIsTrue_compute(left, right);
        return eqIsTrue_Expr_Expr_value;
    }

    private boolean eqIsTrue_compute(Expr left, Expr right)  {
    System.err.println("Evaluation eqIsTrue for unknown type: " + getClass().getName());
    return false;
  }

    // Declared in ConstantExpression.jrag at line 526
    public boolean ltIsTrue(Expr left, Expr right) {
        boolean ltIsTrue_Expr_Expr_value = ltIsTrue_compute(left, right);
        return ltIsTrue_Expr_Expr_value;
    }

    private boolean ltIsTrue_compute(Expr left, Expr right) {  return  false;  }

    // Declared in ConstantExpression.jrag at line 532
    public boolean leIsTrue(Expr left, Expr right) {
        boolean leIsTrue_Expr_Expr_value = leIsTrue_compute(left, right);
        return leIsTrue_Expr_Expr_value;
    }

    private boolean leIsTrue_compute(Expr left, Expr right) {  return  false;  }

    // Declared in DataStructures.jrag at line 107
    public int size() {
        int size_value = size_compute();
        return size_value;
    }

    private int size_compute() {  return  1;  }

    // Declared in DataStructures.jrag at line 108
    public boolean isEmpty() {
        boolean isEmpty_value = isEmpty_compute();
        return isEmpty_value;
    }

    private boolean isEmpty_compute() {  return  false;  }

    // Declared in DataStructures.jrag at line 112
    public boolean contains(Object o) {
        boolean contains_Object_value = contains_compute(o);
        return contains_Object_value;
    }

    private boolean contains_compute(Object o) {  return  this == o;  }

    protected boolean isException_computed = false;
    protected boolean isException_value;
    // Declared in ExceptionHandling.jrag at line 15
    public boolean isException() {
        if(isException_computed)
            return isException_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isException_value = isException_compute();
        if(isFinal && num == boundariesCrossed)
            isException_computed = true;
        return isException_value;
    }

    private boolean isException_compute() {  return  instanceOf(typeException());  }

    protected boolean isCheckedException_computed = false;
    protected boolean isCheckedException_value;
    // Declared in ExceptionHandling.jrag at line 16
    public boolean isCheckedException() {
        if(isCheckedException_computed)
            return isCheckedException_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isCheckedException_value = isCheckedException_compute();
        if(isFinal && num == boundariesCrossed)
            isCheckedException_computed = true;
        return isCheckedException_value;
    }

    private boolean isCheckedException_compute() {  return  isException() &&
    (instanceOf(typeRuntimeException()) || instanceOf(typeError()));  }

    protected boolean isUncheckedException_computed = false;
    protected boolean isUncheckedException_value;
    // Declared in ExceptionHandling.jrag at line 18
    public boolean isUncheckedException() {
        if(isUncheckedException_computed)
            return isUncheckedException_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isUncheckedException_value = isUncheckedException_compute();
        if(isFinal && num == boundariesCrossed)
            isUncheckedException_computed = true;
        return isUncheckedException_value;
    }

    private boolean isUncheckedException_compute() {  return  isException() && !isCheckedException();  }

    protected java.util.Map mayCatch_TypeDecl_values;
    // Declared in ExceptionHandling.jrag at line 212
    public boolean mayCatch(TypeDecl thrownType) {
        Object _parameters = thrownType;
if(mayCatch_TypeDecl_values == null) mayCatch_TypeDecl_values = new java.util.HashMap(4);
        if(mayCatch_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)mayCatch_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayCatch_TypeDecl_value = mayCatch_compute(thrownType);
        if(isFinal && num == boundariesCrossed)
            mayCatch_TypeDecl_values.put(_parameters, Boolean.valueOf(mayCatch_TypeDecl_value));
        return mayCatch_TypeDecl_value;
    }

    private boolean mayCatch_compute(TypeDecl thrownType) {  return 
    thrownType.instanceOf(this) || this.instanceOf(thrownType);  }

    // Declared in LookupConstructor.jrag at line 12
    public Collection lookupSuperConstructor() {
        Collection lookupSuperConstructor_value = lookupSuperConstructor_compute();
        return lookupSuperConstructor_value;
    }

    private Collection lookupSuperConstructor_compute() {  return  Collections.EMPTY_LIST;  }

    protected boolean constructors_computed = false;
    protected Collection constructors_value;
    // Declared in LookupConstructor.jrag at line 90
    public Collection constructors() {
        if(constructors_computed)
            return constructors_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        constructors_value = constructors_compute();
        if(isFinal && num == boundariesCrossed)
            constructors_computed = true;
        return constructors_value;
    }

    private Collection constructors_compute()  {
    Collection c = new ArrayList();
    for(int i = 0; i < getNumBodyDecl(); i++) {
      if(getBodyDecl(i) instanceof ConstructorDecl) {
        c.add(getBodyDecl(i));
      }
    }
    if(c.isEmpty() && isClassDecl()) {
      Modifiers m = new Modifiers();
      if(isPublic()) m.addModifier(new Modifier("public"));
      else if(isProtected()) m.addModifier(new Modifier("protected"));
      else if(isPrivate()) m.addModifier(new Modifier("private"));
      addBodyDecl(
          new ConstructorDecl(
            m,
            name(),
            new List(),
            new List(),
            new Opt(),
            new Block()
          )
      );
      c.add(getBodyDecl(getNumBodyDecl()-1));
    }
    return c;
  }

    protected java.util.Map unqualifiedLookupMethod_String_values;
    // Declared in LookupMethod.jrag at line 24
    public Collection unqualifiedLookupMethod(String name) {
        Object _parameters = name;
if(unqualifiedLookupMethod_String_values == null) unqualifiedLookupMethod_String_values = new java.util.HashMap(4);
        if(unqualifiedLookupMethod_String_values.containsKey(_parameters))
            return (Collection)unqualifiedLookupMethod_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Collection unqualifiedLookupMethod_String_value = unqualifiedLookupMethod_compute(name);
        if(isFinal && num == boundariesCrossed)
            unqualifiedLookupMethod_String_values.put(_parameters, unqualifiedLookupMethod_String_value);
        return unqualifiedLookupMethod_String_value;
    }

    private Collection unqualifiedLookupMethod_compute(String name)  {
    Collection c = memberMethods(name);
    if(!c.isEmpty()) return c;
    if(isInnerType())
      return lookupMethod(name);
    return removeInstanceMethods(lookupMethod(name));
  }

    // Declared in LookupMethod.jrag at line 179
    public Collection memberMethods(String name) {
        Collection memberMethods_String_value = memberMethods_compute(name);
        return memberMethods_String_value;
    }

    private Collection memberMethods_compute(String name)  {
    Collection c = (Collection)methodsNameMap().get(name);
    if(c != null) return c;
    return Collections.EMPTY_LIST;
  }

    protected boolean methodsNameMap_computed = false;
    protected HashMap methodsNameMap_value;
    // Declared in LookupMethod.jrag at line 185
    public HashMap methodsNameMap() {
        if(methodsNameMap_computed)
            return methodsNameMap_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        methodsNameMap_value = methodsNameMap_compute();
        if(isFinal && num == boundariesCrossed)
            methodsNameMap_computed = true;
        return methodsNameMap_value;
    }

    private HashMap methodsNameMap_compute()  {
    HashMap map = new HashMap();
    for(Iterator iter = methodsIterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      ArrayList list = (ArrayList)map.get(m.name());
      if(list == null) {
        list = new ArrayList(4);
        map.put(m.name(), list);
      }
      list.add(m);
    }
    return map;
  }

    // Declared in LookupMethod.jrag at line 216
    public SimpleSet localMethodsSignature(String signature) {
        SimpleSet localMethodsSignature_String_value = localMethodsSignature_compute(signature);
        return localMethodsSignature_String_value;
    }

    private SimpleSet localMethodsSignature_compute(String signature)  {
    SimpleSet set = (SimpleSet)localMethodsSignatureMap().get(signature);
    if(set != null) return set;
    return SimpleSet.emptySet;
  }

    protected boolean localMethodsSignatureMap_computed = false;
    protected HashMap localMethodsSignatureMap_value;
    // Declared in LookupMethod.jrag at line 222
    public HashMap localMethodsSignatureMap() {
        if(localMethodsSignatureMap_computed)
            return localMethodsSignatureMap_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        localMethodsSignatureMap_value = localMethodsSignatureMap_compute();
        if(isFinal && num == boundariesCrossed)
            localMethodsSignatureMap_computed = true;
        return localMethodsSignatureMap_value;
    }

    private HashMap localMethodsSignatureMap_compute()  {
    HashMap map = new HashMap(getNumBodyDecl());
    for(int i = 0; i < getNumBodyDecl(); i++) {
      if(getBodyDecl(i) instanceof MethodDecl) {
        MethodDecl decl = (MethodDecl)getBodyDecl(i);
        map.put(decl.signature(), decl);
      }
    }
    return map;
  }

    // Declared in LookupMethod.jrag at line 284
    public SimpleSet methodsSignature(String signature) {
        SimpleSet methodsSignature_String_value = methodsSignature_compute(signature);
        return methodsSignature_String_value;
    }

    private SimpleSet methodsSignature_compute(String signature)  {
    SimpleSet set = (SimpleSet)methodsSignatureMap().get(signature);
    if(set != null) return set;
    return SimpleSet.emptySet;
  }

    protected boolean methodsSignatureMap_computed = false;
    protected HashMap methodsSignatureMap_value;
    // Declared in LookupMethod.jrag at line 290
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

    private HashMap methodsSignatureMap_compute() {  return  localMethodsSignatureMap();  }

    protected java.util.Map ancestorMethods_String_values;
    // Declared in LookupMethod.jrag at line 347
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

    private SimpleSet ancestorMethods_compute(String signature) {  return  SimpleSet.emptySet;  }

    // Declared in LookupType.jrag at line 332
    public boolean hasType(String name) {
        boolean hasType_String_value = hasType_compute(name);
        return hasType_String_value;
    }

    private boolean hasType_compute(String name) {  return  !memberTypes(name).isEmpty();  }

    protected java.util.Map memberTypes_String_values;
    // Declared in LookupType.jrag at line 342
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

    private SimpleSet memberTypes_compute(String name) {  return  SimpleSet.emptySet;  }

    protected java.util.Map memberFields_String_values;
    // Declared in LookupVariable.jrag at line 256
    public SimpleSet memberFields(String name) {
        Object _parameters = name;
if(memberFields_String_values == null) memberFields_String_values = new java.util.HashMap(4);
        if(memberFields_String_values.containsKey(_parameters))
            return (SimpleSet)memberFields_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet memberFields_String_value = memberFields_compute(name);
        if(isFinal && num == boundariesCrossed)
            memberFields_String_values.put(_parameters, memberFields_String_value);
        return memberFields_String_value;
    }

    private SimpleSet memberFields_compute(String name) {  return  fields(name);  }

    // Declared in LookupVariable.jrag at line 271
    public SimpleSet localFields(String name) {
        SimpleSet localFields_String_value = localFields_compute(name);
        return localFields_String_value;
    }

    private SimpleSet localFields_compute(String name) {  return  localFieldsMap().containsKey(name) ? (SimpleSet)localFieldsMap().get(name) : SimpleSet.emptySet;  }

    protected boolean localFieldsMap_computed = false;
    protected HashMap localFieldsMap_value;
    // Declared in LookupVariable.jrag at line 273
    public HashMap localFieldsMap() {
        if(localFieldsMap_computed)
            return localFieldsMap_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        localFieldsMap_value = localFieldsMap_compute();
        if(isFinal && num == boundariesCrossed)
            localFieldsMap_computed = true;
        return localFieldsMap_value;
    }

    private HashMap localFieldsMap_compute()  {
    HashMap map = new HashMap();
    for(int i = 0; i < getNumBodyDecl(); i++) {
      if(getBodyDecl(i) instanceof FieldDeclaration) {
        FieldDeclaration decl = (FieldDeclaration)getBodyDecl(i);
        SimpleSet fields = (SimpleSet)map.get(decl.name());
        if(fields == null) fields = SimpleSet.emptySet;
        fields = fields.add(decl);
        map.put(decl.name(), fields);
      }
    }
    return map;
  }

    // Declared in LookupVariable.jrag at line 287
    public SimpleSet fields(String name) {
        SimpleSet fields_String_value = fields_compute(name);
        return fields_String_value;
    }

    private SimpleSet fields_compute(String name) {  return  localFields(name);  }

    protected boolean hasAbstract_computed = false;
    protected boolean hasAbstract_value;
    // Declared in Modifiers.jrag at line 5
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

    private boolean hasAbstract_compute() {  return  false;  }

    protected boolean unimplementedMethods_computed = false;
    protected Collection unimplementedMethods_value;
    // Declared in Modifiers.jrag at line 7
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

    private Collection unimplementedMethods_compute() {  return  Collections.EMPTY_LIST;  }

    protected boolean isPublic_computed = false;
    protected boolean isPublic_value;
    // Declared in Modifiers.jrag at line 189
    public boolean isPublic() {
        if(isPublic_computed)
            return isPublic_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isPublic_value = isPublic_compute();
        if(isFinal && num == boundariesCrossed)
            isPublic_computed = true;
        return isPublic_value;
    }

    private boolean isPublic_compute() {  return  getModifiers().isPublic() || isMemberType() && enclosingType().isInterfaceDecl();  }

    // Declared in Modifiers.jrag at line 191
    public boolean isPrivate() {
        boolean isPrivate_value = isPrivate_compute();
        return isPrivate_value;
    }

    private boolean isPrivate_compute() {  return  getModifiers().isPrivate();  }

    // Declared in Modifiers.jrag at line 192
    public boolean isProtected() {
        boolean isProtected_value = isProtected_compute();
        return isProtected_value;
    }

    private boolean isProtected_compute() {  return  getModifiers().isProtected();  }

    // Declared in Modifiers.jrag at line 193
    public boolean isAbstract() {
        boolean isAbstract_value = isAbstract_compute();
        return isAbstract_value;
    }

    private boolean isAbstract_compute() {  return  getModifiers().isAbstract();  }

    protected boolean isStatic_computed = false;
    protected boolean isStatic_value;
    // Declared in Modifiers.jrag at line 195
    public boolean isStatic() {
        if(isStatic_computed)
            return isStatic_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isStatic_value = isStatic_compute();
        if(isFinal && num == boundariesCrossed)
            isStatic_computed = true;
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return  getModifiers().isStatic() || isMemberType() && enclosingType().isInterfaceDecl();  }

    // Declared in Modifiers.jrag at line 198
    public boolean isFinal() {
        boolean isFinal_value = isFinal_compute();
        return isFinal_value;
    }

    private boolean isFinal_compute() {  return  getModifiers().isFinal();  }

    // Declared in Modifiers.jrag at line 199
    public boolean isStrictfp() {
        boolean isStrictfp_value = isStrictfp_compute();
        return isStrictfp_value;
    }

    private boolean isStrictfp_compute() {  return  getModifiers().isStrictfp();  }

    // Declared in Modifiers.jrag at line 201
    public boolean isSynthetic() {
        boolean isSynthetic_value = isSynthetic_compute();
        return isSynthetic_value;
    }

    private boolean isSynthetic_compute() {  return  getModifiers().isSynthetic();  }

    // Declared in NameCheck.jrag at line 257
    public boolean hasEnclosingTypeDecl(String name) {
        boolean hasEnclosingTypeDecl_String_value = hasEnclosingTypeDecl_compute(name);
        return hasEnclosingTypeDecl_String_value;
    }

    private boolean hasEnclosingTypeDecl_compute(String name)  {
    TypeDecl enclosingType = enclosingType();
    if(enclosingType != null) {
      return enclosingType.name().equals(name) || enclosingType.hasEnclosingTypeDecl(name);
    }
    return false;
  }

    // Declared in NameCheck.jrag at line 417
    public boolean assignableToInt() {
        boolean assignableToInt_value = assignableToInt_compute();
        return assignableToInt_value;
    }

    private boolean assignableToInt_compute() {  return  false;  }

    // Declared in PrettyPrint.jadd at line 945
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 59
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    protected boolean fullName_computed = false;
    protected String fullName_value;
    // Declared in QualifiedNames.jrag at line 61
    public String fullName() {
        if(fullName_computed)
            return fullName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        fullName_value = fullName_compute();
        if(isFinal && num == boundariesCrossed)
            fullName_computed = true;
        return fullName_value;
    }

    private String fullName_compute()  {
    if(isNestedType())
      return enclosingType().fullName() + "." + name();
    String packageName = packageName();
    if(packageName.equals(""))
      return name();
    return packageName + "." + name();
  }

    protected boolean typeName_computed = false;
    protected String typeName_value;
    // Declared in QualifiedNames.jrag at line 70
    public String typeName() {
        if(typeName_computed)
            return typeName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeName_value = typeName_compute();
        if(isFinal && num == boundariesCrossed)
            typeName_computed = true;
        return typeName_value;
    }

    private String typeName_compute()  {
    if(isNestedType())
      return enclosingType().typeName() + "." + name();
    String packageName = packageName();
    if(packageName.equals("") || packageName.equals(PRIMITIVE_PACKAGE_NAME))
      return name();
    return packageName + "." + name();
  }

    // Declared in TypeAnalysis.jrag at line 7
    public boolean identityConversionTo(TypeDecl type) {
        boolean identityConversionTo_TypeDecl_value = identityConversionTo_compute(type);
        return identityConversionTo_TypeDecl_value;
    }

    private boolean identityConversionTo_compute(TypeDecl type) {  return  this == type;  }

    // Declared in TypeAnalysis.jrag at line 9
    public boolean wideningConversionTo(TypeDecl type) {
        boolean wideningConversionTo_TypeDecl_value = wideningConversionTo_compute(type);
        return wideningConversionTo_TypeDecl_value;
    }

    private boolean wideningConversionTo_compute(TypeDecl type) {  return  instanceOf(type);  }

    protected java.util.Map narrowingConversionTo_TypeDecl_values;
    // Declared in TypeAnalysis.jrag at line 10
    public boolean narrowingConversionTo(TypeDecl type) {
        Object _parameters = type;
if(narrowingConversionTo_TypeDecl_values == null) narrowingConversionTo_TypeDecl_values = new java.util.HashMap(4);
        if(narrowingConversionTo_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)narrowingConversionTo_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean narrowingConversionTo_TypeDecl_value = narrowingConversionTo_compute(type);
        if(isFinal && num == boundariesCrossed)
            narrowingConversionTo_TypeDecl_values.put(_parameters, Boolean.valueOf(narrowingConversionTo_TypeDecl_value));
        return narrowingConversionTo_TypeDecl_value;
    }

    private boolean narrowingConversionTo_compute(TypeDecl type) {  return  instanceOf(type);  }

    // Declared in TypeAnalysis.jrag at line 47
    public boolean stringConversion() {
        boolean stringConversion_value = stringConversion_compute();
        return stringConversion_value;
    }

    private boolean stringConversion_compute() {  return  true;  }

    // Declared in TypeAnalysis.jrag at line 51
    public boolean assignConversionTo(TypeDecl type, Expr expr) {
        boolean assignConversionTo_TypeDecl_Expr_value = assignConversionTo_compute(type, expr);
        return assignConversionTo_TypeDecl_Expr_value;
    }

    private boolean assignConversionTo_compute(TypeDecl type, Expr expr)  {
    //System.out.println("@@@ " + fullName() + " assign conversion to " + type.fullName() + ", expr: " + expr);
    boolean sourceIsConstant = expr != null ? expr.isConstant() : false;
    //System.out.println("@@@ sourceIsConstant: " + sourceIsConstant);
    if(identityConversionTo(type) || wideningConversionTo(type))
      return true;
    //System.out.println("@@@ narrowing conversion needed");
    //System.out.println("@@@ value: " + expr.value());
    if(sourceIsConstant && (isInt() || isChar() || isShort() || isByte()) &&
        (type.isByte() || type.isShort() || type.isChar()) &&
        narrowingConversionTo(type) && expr.representableIn(type))
      return true;
    //System.out.println("@@@ false");
    return false;
  }

    protected java.util.Map methodInvocationConversionTo_TypeDecl_values;
    // Declared in TypeAnalysis.jrag at line 68
    public boolean methodInvocationConversionTo(TypeDecl type) {
        Object _parameters = type;
if(methodInvocationConversionTo_TypeDecl_values == null) methodInvocationConversionTo_TypeDecl_values = new java.util.HashMap(4);
        if(methodInvocationConversionTo_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)methodInvocationConversionTo_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean methodInvocationConversionTo_TypeDecl_value = methodInvocationConversionTo_compute(type);
        if(isFinal && num == boundariesCrossed)
            methodInvocationConversionTo_TypeDecl_values.put(_parameters, Boolean.valueOf(methodInvocationConversionTo_TypeDecl_value));
        return methodInvocationConversionTo_TypeDecl_value;
    }

    private boolean methodInvocationConversionTo_compute(TypeDecl type)  {
    return identityConversionTo(type) || wideningConversionTo(type);
  }

    protected java.util.Map castingConversionTo_TypeDecl_values;
    // Declared in TypeAnalysis.jrag at line 73
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

    private boolean castingConversionTo_compute(TypeDecl type) {  return  identityConversionTo(type) ||
    wideningConversionTo(type) || narrowingConversionTo(type);  }

    // Declared in TypeAnalysis.jrag at line 138
    public TypeDecl unaryNumericPromotion() {
        TypeDecl unaryNumericPromotion_value = unaryNumericPromotion_compute();
        return unaryNumericPromotion_value;
    }

    private TypeDecl unaryNumericPromotion_compute() {  return  this;  }

    // Declared in TypeAnalysis.jrag at line 146
    public TypeDecl binaryNumericPromotion(TypeDecl type) {
        TypeDecl binaryNumericPromotion_TypeDecl_value = binaryNumericPromotion_compute(type);
        return binaryNumericPromotion_TypeDecl_value;
    }

    private TypeDecl binaryNumericPromotion_compute(TypeDecl type) {  return  unknownType();  }

    // Declared in TypeAnalysis.jrag at line 157
    public boolean isReferenceType() {
        boolean isReferenceType_value = isReferenceType_compute();
        return isReferenceType_value;
    }

    private boolean isReferenceType_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 160
    public boolean isPrimitiveType() {
        boolean isPrimitiveType_value = isPrimitiveType_compute();
        return isPrimitiveType_value;
    }

    private boolean isPrimitiveType_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 165
    public boolean isNumericType() {
        boolean isNumericType_value = isNumericType_compute();
        return isNumericType_value;
    }

    private boolean isNumericType_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 169
    public boolean isIntegralType() {
        boolean isIntegralType_value = isIntegralType_compute();
        return isIntegralType_value;
    }

    private boolean isIntegralType_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 173
    public boolean isBoolean() {
        boolean isBoolean_value = isBoolean_compute();
        return isBoolean_value;
    }

    private boolean isBoolean_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 177
    public boolean isByte() {
        boolean isByte_value = isByte_compute();
        return isByte_value;
    }

    private boolean isByte_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 179
    public boolean isChar() {
        boolean isChar_value = isChar_compute();
        return isChar_value;
    }

    private boolean isChar_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 181
    public boolean isShort() {
        boolean isShort_value = isShort_compute();
        return isShort_value;
    }

    private boolean isShort_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 183
    public boolean isInt() {
        boolean isInt_value = isInt_compute();
        return isInt_value;
    }

    private boolean isInt_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 187
    public boolean isFloat() {
        boolean isFloat_value = isFloat_compute();
        return isFloat_value;
    }

    private boolean isFloat_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 189
    public boolean isLong() {
        boolean isLong_value = isLong_compute();
        return isLong_value;
    }

    private boolean isLong_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 191
    public boolean isDouble() {
        boolean isDouble_value = isDouble_compute();
        return isDouble_value;
    }

    private boolean isDouble_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 194
    public boolean isVoid() {
        boolean isVoid_value = isVoid_compute();
        return isVoid_value;
    }

    private boolean isVoid_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 197
    public boolean isNull() {
        boolean isNull_value = isNull_compute();
        return isNull_value;
    }

    private boolean isNull_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 201
    public boolean isClassDecl() {
        boolean isClassDecl_value = isClassDecl_compute();
        return isClassDecl_value;
    }

    private boolean isClassDecl_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 203
    public boolean isInterfaceDecl() {
        boolean isInterfaceDecl_value = isInterfaceDecl_compute();
        return isInterfaceDecl_value;
    }

    private boolean isInterfaceDecl_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 205
    public boolean isArrayDecl() {
        boolean isArrayDecl_value = isArrayDecl_compute();
        return isArrayDecl_value;
    }

    private boolean isArrayDecl_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 213
    public boolean isPrimitive() {
        boolean isPrimitive_value = isPrimitive_compute();
        return isPrimitive_value;
    }

    private boolean isPrimitive_compute() {  return  false;  }

    protected boolean isString_computed = false;
    protected boolean isString_value;
    // Declared in TypeAnalysis.jrag at line 216
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

    private boolean isString_compute() {  return  false;  }

    protected boolean isObject_computed = false;
    protected boolean isObject_value;
    // Declared in TypeAnalysis.jrag at line 219
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

    private boolean isObject_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 222
    public boolean isUnknown() {
        boolean isUnknown_value = isUnknown_compute();
        return isUnknown_value;
    }

    private boolean isUnknown_compute() {  return  false;  }

    protected java.util.Map instanceOf_TypeDecl_values;
    // Declared in TypeAnalysis.jrag at line 407
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

    private boolean instanceOf_compute(TypeDecl type) {  return  type == this;  }

    // Declared in TypeAnalysis.jrag at line 422
    public boolean isSupertypeOfClassDecl(ClassDecl type) {
        boolean isSupertypeOfClassDecl_ClassDecl_value = isSupertypeOfClassDecl_compute(type);
        return isSupertypeOfClassDecl_ClassDecl_value;
    }

    private boolean isSupertypeOfClassDecl_compute(ClassDecl type) {  return  type == this;  }

    // Declared in TypeAnalysis.jrag at line 439
    public boolean isSupertypeOfInterfaceDecl(InterfaceDecl type) {
        boolean isSupertypeOfInterfaceDecl_InterfaceDecl_value = isSupertypeOfInterfaceDecl_compute(type);
        return isSupertypeOfInterfaceDecl_InterfaceDecl_value;
    }

    private boolean isSupertypeOfInterfaceDecl_compute(InterfaceDecl type) {  return  type == this;  }

    // Declared in TypeAnalysis.jrag at line 452
    public boolean isSupertypeOfArrayDecl(ArrayDecl type) {
        boolean isSupertypeOfArrayDecl_ArrayDecl_value = isSupertypeOfArrayDecl_compute(type);
        return isSupertypeOfArrayDecl_ArrayDecl_value;
    }

    private boolean isSupertypeOfArrayDecl_compute(ArrayDecl type) {  return  this == type;  }

    // Declared in TypeAnalysis.jrag at line 474
    public boolean isSupertypeOfPrimitiveType(PrimitiveType type) {
        boolean isSupertypeOfPrimitiveType_PrimitiveType_value = isSupertypeOfPrimitiveType_compute(type);
        return isSupertypeOfPrimitiveType_PrimitiveType_value;
    }

    private boolean isSupertypeOfPrimitiveType_compute(PrimitiveType type) {  return  type == this;  }

    // Declared in TypeAnalysis.jrag at line 481
    public boolean isSupertypeOfNullType(NullType type) {
        boolean isSupertypeOfNullType_NullType_value = isSupertypeOfNullType_compute(type);
        return isSupertypeOfNullType_NullType_value;
    }

    private boolean isSupertypeOfNullType_compute(NullType type) {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 485
    public boolean isSupertypeOfVoidType(VoidType type) {
        boolean isSupertypeOfVoidType_VoidType_value = isSupertypeOfVoidType_compute(type);
        return isSupertypeOfVoidType_VoidType_value;
    }

    private boolean isSupertypeOfVoidType_compute(VoidType type) {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 497
    public TypeDecl topLevelType() {
        TypeDecl topLevelType_value = topLevelType_compute();
        return topLevelType_value;
    }

    private TypeDecl topLevelType_compute()  {
    if(isTopLevelType())
      return this;
    return enclosingType().topLevelType();
  }

    // Declared in TypeAnalysis.jrag at line 517
    public boolean isTopLevelType() {
        boolean isTopLevelType_value = isTopLevelType_compute();
        return isTopLevelType_value;
    }

    private boolean isTopLevelType_compute() {  return  !isNestedType();  }

    // Declared in TypeAnalysis.jrag at line 528
    public boolean isInnerClass() {
        boolean isInnerClass_value = isInnerClass_compute();
        return isInnerClass_value;
    }

    private boolean isInnerClass_compute() {  return  false;  }

    // Declared in TypeAnalysis.jrag at line 530
    public boolean isInnerType() {
        boolean isInnerType_value = isInnerType_compute();
        return isInnerType_value;
    }

    private boolean isInnerType_compute() {  return  (isLocalClass() || isAnonymous() || (isMemberType() && !isStatic())) && !inStaticContext();  }

    // Declared in TypeAnalysis.jrag at line 532
    public boolean isInnerTypeOf(TypeDecl typeDecl) {
        boolean isInnerTypeOf_TypeDecl_value = isInnerTypeOf_compute(typeDecl);
        return isInnerTypeOf_TypeDecl_value;
    }

    private boolean isInnerTypeOf_compute(TypeDecl typeDecl) {  return  typeDecl == this || (isInnerType() && enclosingType().isInnerTypeOf(typeDecl));  }

    // Declared in TypeAnalysis.jrag at line 539
    public TypeDecl withinBodyThatSubclasses(TypeDecl type) {
        TypeDecl withinBodyThatSubclasses_TypeDecl_value = withinBodyThatSubclasses_compute(type);
        return withinBodyThatSubclasses_TypeDecl_value;
    }

    private TypeDecl withinBodyThatSubclasses_compute(TypeDecl type)  {
    if(instanceOf(type))
      return this;
    if(!isTopLevelType())
      return enclosingType().withinBodyThatSubclasses(type);
    return null;
  }

    // Declared in TypeAnalysis.jrag at line 547
    public boolean encloses(TypeDecl type) {
        boolean encloses_TypeDecl_value = encloses_compute(type);
        return encloses_TypeDecl_value;
    }

    private boolean encloses_compute(TypeDecl type) {  return  type.enclosedBy(this);  }

    // Declared in TypeAnalysis.jrag at line 549
    public boolean enclosedBy(TypeDecl type) {
        boolean enclosedBy_TypeDecl_value = enclosedBy_compute(type);
        return enclosedBy_TypeDecl_value;
    }

    private boolean enclosedBy_compute(TypeDecl type)  {
    if(this == type)
      return true;
    if(isTopLevelType())
      return false;
    return enclosingType().enclosedBy(type);
  }

    // Declared in TypeAnalysis.jrag at line 563
    public TypeDecl hostType() {
        TypeDecl hostType_value = hostType_compute();
        return hostType_value;
    }

    private TypeDecl hostType_compute() {  return  this;  }

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

    private boolean isCircular_compute() {  return  false;  }

    // Declared in AccessField.jrag at line 16
    public Access accessFieldInSupertypes(FieldDeclaration fd) {
        Access accessFieldInSupertypes_FieldDeclaration_value = accessFieldInSupertypes_compute(fd);
        return accessFieldInSupertypes_FieldDeclaration_value;
    }

    private Access accessFieldInSupertypes_compute(FieldDeclaration fd) {  return  null;  }

    protected boolean componentType_computed = false;
    protected TypeDecl componentType_value;
    // Declared in Arrays.jrag at line 13
    public TypeDecl componentType() {
        if(componentType_computed)
            return componentType_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        componentType_value = getParent().Define_TypeDecl_componentType(this, null);
        if(isFinal && num == boundariesCrossed)
            componentType_computed = true;
        return componentType_value;
    }

    // Declared in Arrays.jrag at line 43
    public TypeDecl typeCloneable() {
        TypeDecl typeCloneable_value = getParent().Define_TypeDecl_typeCloneable(this, null);
        return typeCloneable_value;
    }

    // Declared in Arrays.jrag at line 44
    public TypeDecl typeSerializable() {
        TypeDecl typeSerializable_value = getParent().Define_TypeDecl_typeSerializable(this, null);
        return typeSerializable_value;
    }

    protected java.util.Map isDAbefore_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 231
    public boolean isDAbefore(Variable v) {
        Object _parameters = v;
if(isDAbefore_Variable_values == null) isDAbefore_Variable_values = new java.util.HashMap(4);
        if(isDAbefore_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAbefore_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        if(isFinal && num == boundariesCrossed)
            isDAbefore_Variable_values.put(_parameters, Boolean.valueOf(isDAbefore_Variable_value));
        return isDAbefore_Variable_value;
    }

    protected java.util.Map isDUbefore_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 702
    public boolean isDUbefore(Variable v) {
        Object _parameters = v;
if(isDUbefore_Variable_values == null) isDUbefore_Variable_values = new java.util.HashMap(4);
        if(isDUbefore_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUbefore_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        if(isFinal && num == boundariesCrossed)
            isDUbefore_Variable_values.put(_parameters, Boolean.valueOf(isDUbefore_Variable_value));
        return isDUbefore_Variable_value;
    }

    protected boolean typeException_computed = false;
    protected TypeDecl typeException_value;
    // Declared in ExceptionHandling.jrag at line 5
    public TypeDecl typeException() {
        if(typeException_computed)
            return typeException_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeException_value = getParent().Define_TypeDecl_typeException(this, null);
        if(isFinal && num == boundariesCrossed)
            typeException_computed = true;
        return typeException_value;
    }

    protected boolean typeRuntimeException_computed = false;
    protected TypeDecl typeRuntimeException_value;
    // Declared in ExceptionHandling.jrag at line 7
    public TypeDecl typeRuntimeException() {
        if(typeRuntimeException_computed)
            return typeRuntimeException_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeRuntimeException_value = getParent().Define_TypeDecl_typeRuntimeException(this, null);
        if(isFinal && num == boundariesCrossed)
            typeRuntimeException_computed = true;
        return typeRuntimeException_value;
    }

    protected boolean typeError_computed = false;
    protected TypeDecl typeError_value;
    // Declared in ExceptionHandling.jrag at line 9
    public TypeDecl typeError() {
        if(typeError_computed)
            return typeError_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeError_value = getParent().Define_TypeDecl_typeError(this, null);
        if(isFinal && num == boundariesCrossed)
            typeError_computed = true;
        return typeError_value;
    }

    protected java.util.Map lookupMethod_String_values;
    // Declared in LookupMethod.jrag at line 15
    public Collection lookupMethod(String name) {
        Object _parameters = name;
if(lookupMethod_String_values == null) lookupMethod_String_values = new java.util.HashMap(4);
        if(lookupMethod_String_values.containsKey(_parameters))
            return (Collection)lookupMethod_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Collection lookupMethod_String_value = getParent().Define_Collection_lookupMethod(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupMethod_String_values.put(_parameters, lookupMethod_String_value);
        return lookupMethod_String_value;
    }

    // Declared in LookupType.jrag at line 53
    public TypeDecl typeInt() {
        TypeDecl typeInt_value = getParent().Define_TypeDecl_typeInt(this, null);
        return typeInt_value;
    }

    protected boolean typeObject_computed = false;
    protected TypeDecl typeObject_value;
    // Declared in LookupType.jrag at line 56
    public TypeDecl typeObject() {
        if(typeObject_computed)
            return typeObject_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeObject_value = getParent().Define_TypeDecl_typeObject(this, null);
        if(isFinal && num == boundariesCrossed)
            typeObject_computed = true;
        return typeObject_value;
    }

    // Declared in LookupType.jrag at line 88
    public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    protected java.util.Map lookupType_String_values;
    // Declared in LookupType.jrag at line 174
    public SimpleSet lookupType(String name) {
        Object _parameters = name;
if(lookupType_String_values == null) lookupType_String_values = new java.util.HashMap(4);
        if(lookupType_String_values.containsKey(_parameters))
            return (SimpleSet)lookupType_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupType_String_values.put(_parameters, lookupType_String_value);
        return lookupType_String_value;
    }

    protected java.util.Map lookupVariable_String_values;
    // Declared in LookupVariable.jrag at line 5
    public SimpleSet lookupVariable(String name) {
        Object _parameters = name;
if(lookupVariable_String_values == null) lookupVariable_String_values = new java.util.HashMap(4);
        if(lookupVariable_String_values.containsKey(_parameters))
            return (SimpleSet)lookupVariable_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupVariable_String_values.put(_parameters, lookupVariable_String_value);
        return lookupVariable_String_value;
    }

    // Declared in NameCheck.jrag at line 225
    public boolean hasPackage(String packageName) {
        boolean hasPackage_String_value = getParent().Define_boolean_hasPackage(this, null, packageName);
        return hasPackage_String_value;
    }

    // Declared in NameCheck.jrag at line 228
    public ASTNode enclosingBlock() {
        ASTNode enclosingBlock_value = getParent().Define_ASTNode_enclosingBlock(this, null);
        return enclosingBlock_value;
    }

    protected boolean packageName_computed = false;
    protected String packageName_value;
    // Declared in QualifiedNames.jrag at line 80
    public String packageName() {
        if(packageName_computed)
            return packageName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        packageName_value = getParent().Define_String_packageName(this, null);
        if(isFinal && num == boundariesCrossed)
            packageName_computed = true;
        return packageName_value;
    }

    protected boolean isAnonymous_computed = false;
    protected boolean isAnonymous_value;
    // Declared in TypeAnalysis.jrag at line 208
    public boolean isAnonymous() {
        if(isAnonymous_computed)
            return isAnonymous_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isAnonymous_value = getParent().Define_boolean_isAnonymous(this, null);
        if(isFinal && num == boundariesCrossed)
            isAnonymous_computed = true;
        return isAnonymous_value;
    }

    // Declared in TypeAnalysis.jrag at line 496
    public TypeDecl enclosingType() {
        TypeDecl enclosingType_value = getParent().Define_TypeDecl_enclosingType(this, null);
        return enclosingType_value;
    }

    // Declared in TypeAnalysis.jrag at line 512
    public boolean isNestedType() {
        boolean isNestedType_value = getParent().Define_boolean_isNestedType(this, null);
        return isNestedType_value;
    }

    // Declared in TypeAnalysis.jrag at line 520
    public boolean isMemberType() {
        boolean isMemberType_value = getParent().Define_boolean_isMemberType(this, null);
        return isMemberType_value;
    }

    // Declared in TypeAnalysis.jrag at line 534
    public boolean isLocalClass() {
        boolean isLocalClass_value = getParent().Define_boolean_isLocalClass(this, null);
        return isLocalClass_value;
    }

    // Declared in TypeAnalysis.jrag at line 559
    public String hostPackage() {
        String hostPackage_value = getParent().Define_String_hostPackage(this, null);
        return hostPackage_value;
    }

    protected boolean unknownType_computed = false;
    protected TypeDecl unknownType_value;
    // Declared in TypeAnalysis.jrag at line 674
    public TypeDecl unknownType() {
        if(unknownType_computed)
            return unknownType_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        unknownType_value = getParent().Define_TypeDecl_unknownType(this, null);
        if(isFinal && num == boundariesCrossed)
            unknownType_computed = true;
        return unknownType_value;
    }

    // Declared in TypeCheck.jrag at line 391
    public TypeDecl typeVoid() {
        TypeDecl typeVoid_value = getParent().Define_TypeDecl_typeVoid(this, null);
        return typeVoid_value;
    }

    // Declared in TypeCheck.jrag at line 425
    public TypeDecl enclosingInstance() {
        TypeDecl enclosingInstance_value = getParent().Define_TypeDecl_enclosingInstance(this, null);
        return enclosingInstance_value;
    }

    protected boolean inExplicitConstructorInvocation_computed = false;
    protected boolean inExplicitConstructorInvocation_value;
    // Declared in TypeHierarchyCheck.jrag at line 118
    public boolean inExplicitConstructorInvocation() {
        if(inExplicitConstructorInvocation_computed)
            return inExplicitConstructorInvocation_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        inExplicitConstructorInvocation_value = getParent().Define_boolean_inExplicitConstructorInvocation(this, null);
        if(isFinal && num == boundariesCrossed)
            inExplicitConstructorInvocation_computed = true;
        return inExplicitConstructorInvocation_value;
    }

    protected boolean inStaticContext_computed = false;
    protected boolean inStaticContext_value;
    // Declared in TypeHierarchyCheck.jrag at line 126
    public boolean inStaticContext() {
        if(inStaticContext_computed)
            return inStaticContext_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        inStaticContext_value = getParent().Define_boolean_inStaticContext(this, null);
        if(isFinal && num == boundariesCrossed)
            inStaticContext_computed = true;
        return inStaticContext_value;
    }

    // Declared in ASTUtil.jrag at line 7
    public Program programRoot() {
        Program programRoot_value = getParent().Define_Program_programRoot(this, null);
        return programRoot_value;
    }

    // Declared in ASTUtil.jrag at line 14
    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit_value = getParent().Define_CompilationUnit_compilationUnit(this, null);
        return compilationUnit_value;
    }

    protected java.util.Map accessField_FieldDeclaration_values;
    // Declared in AccessField.jrag at line 8
    public Access accessField(FieldDeclaration fd) {
        Object _parameters = fd;
if(accessField_FieldDeclaration_values == null) accessField_FieldDeclaration_values = new java.util.HashMap(4);
        if(accessField_FieldDeclaration_values.containsKey(_parameters))
            return (Access)accessField_FieldDeclaration_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Access accessField_FieldDeclaration_value = getParent().Define_Access_accessField(this, null, fd);
        if(isFinal && num == boundariesCrossed)
            accessField_FieldDeclaration_values.put(_parameters, accessField_FieldDeclaration_value);
        return accessField_FieldDeclaration_value;
    }

    protected java.util.Map accessMethod_MethodDecl_List_values;
    // Declared in AccessMethod.jrag at line 5
    public Access accessMethod(MethodDecl md, List args) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(md);
        _parameters.add(args);
if(accessMethod_MethodDecl_List_values == null) accessMethod_MethodDecl_List_values = new java.util.HashMap(4);
        if(accessMethod_MethodDecl_List_values.containsKey(_parameters))
            return (Access)accessMethod_MethodDecl_List_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Access accessMethod_MethodDecl_List_value = getParent().Define_Access_accessMethod(this, null, md, args);
        if(isFinal && num == boundariesCrossed)
            accessMethod_MethodDecl_List_values.put(_parameters, accessMethod_MethodDecl_List_value);
        return accessMethod_MethodDecl_List_value;
    }

    protected java.util.Map accessType_TypeDecl_boolean_values;
    // Declared in AccessType.jrag at line 5
    public Access accessType(TypeDecl td, boolean ambiguous) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(td);
        _parameters.add(Boolean.valueOf(ambiguous));
if(accessType_TypeDecl_boolean_values == null) accessType_TypeDecl_boolean_values = new java.util.HashMap(4);
        if(accessType_TypeDecl_boolean_values.containsKey(_parameters))
            return (Access)accessType_TypeDecl_boolean_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Access accessType_TypeDecl_boolean_value = getParent().Define_Access_accessType(this, null, td, ambiguous);
        if(isFinal && num == boundariesCrossed)
            accessType_TypeDecl_boolean_values.put(_parameters, accessType_TypeDecl_boolean_value);
        return accessType_TypeDecl_boolean_value;
    }

    // Declared in Modifiers.jrag at line 288
    public boolean Define_boolean_mayBePrivate(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBePrivate(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 154
    public boolean Define_boolean_hasEnclosingTryStmt(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_hasEnclosingTryStmt(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 158
    public boolean Define_boolean_isFinallyBlock(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isFinallyBlock(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 149
    public TryStmt Define_TryStmt_enclosingTryStmt(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  null;
        }
        return getParent().Define_TryStmt_enclosingTryStmt(this, caller);
    }

    // Declared in Modifiers.jrag at line 290
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_mayBeFinal(this, caller);
    }

    // Declared in LookupConstructor.jrag at line 7
    public Collection Define_Collection_lookupConstructor(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  constructors();
        }
        return getParent().Define_Collection_lookupConstructor(this, caller);
    }

    // Declared in NameCheck.jrag at line 359
    public boolean Define_boolean_insideLoop(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_insideLoop(this, caller);
    }

    // Declared in Modifiers.jrag at line 286
    public boolean Define_boolean_mayBePublic(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBePublic(this, caller);
    }

    // Declared in LookupConstructor.jrag at line 11
    public Collection Define_Collection_lookupSuperConstructor(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  lookupSuperConstructor();
        }
        return getParent().Define_Collection_lookupSuperConstructor(this, caller);
    }

    // Declared in Modifiers.jrag at line 287
    public boolean Define_boolean_mayBeProtected(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeProtected(this, caller);
    }

    // Declared in Modifiers.jrag at line 293
    public boolean Define_boolean_mayBeTransient(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_mayBeTransient(this, caller);
    }

    // Declared in Modifiers.jrag at line 296
    public boolean Define_boolean_mayBeNative(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_mayBeNative(this, caller);
    }

    // Declared in Arrays.jrag at line 12
    public TypeDecl Define_TypeDecl_componentType(ASTNode caller, ASTNode child) {
        if(caller == arrayType_value) {
            return  this;
        }
        return getParent().Define_TypeDecl_componentType(this, caller);
    }

    // Declared in AccessPackage.jrag at line 22
    public Access Define_Access_accessPackage(ASTNode caller, ASTNode child, String pkg) {
        if(caller == getBodyDeclListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		String[] path = pkg.split("\\.");
		if(getBodyDecl(i).lookupType(path[0]).isEmpty() && 
				getBodyDecl(i).lookupVariable(path[0]).isEmpty() && hasPackage(pkg)) {
			return new PackageAccess(pkg);
		}
		return null;
	}
}
        return getParent().Define_Access_accessPackage(this, caller, pkg);
    }

    // Declared in SyntacticClassification.jrag at line 108
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.EXPRESSION_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in TypeHierarchyCheck.jrag at line 129
    public boolean Define_boolean_inStaticContext(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  isStatic() || inStaticContext();
        }
        return getParent().Define_boolean_inStaticContext(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 709
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBodyDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    BodyDecl b = getBodyDecl(childIndex);
    if(b instanceof MethodDecl || b instanceof MemberTypeDecl) {
      return false;
    }
    if(v.isClassVariable() || v.isInstanceVariable()) {
      int index = childIndex - 1;
      if(b instanceof ConstructorDecl)
        index = getNumBodyDecl() - 1;
        
      for(int i = index; i >= 0; i--) {
        b = getBodyDecl(i);
        if(b instanceof FieldDeclaration) {
          FieldDeclaration f = (FieldDeclaration)b;
          //System.err.println("  working on field " + f.name() + " which is child " + i);
          if(f == v)
            return !f.hasInit();
          if((v.isClassVariable() && f.isStatic()) || (v.isInstanceVariable() && !f.isStatic()))
            return f.isDUafter(v);
          //System.err.println("  field " + f.name() + " can not affect " + v.name());
        }
        else if(b instanceof StaticInitializer && v.isClassVariable()) {
          StaticInitializer si = (StaticInitializer)b;
          //System.err.println("  working on static initializer which is child " + i);
          return si.isDUafter(v);
        }
        else if(b instanceof InstanceInitializer && v.isInstanceVariable()) {
          InstanceInitializer ii = (InstanceInitializer)b;
          //System.err.println("  working on instance initializer which is child " + i);
          return ii.isDUafter(v);
        }
      }
    }
    //System.err.println("Reached TypeDecl when searching for DU for variable");
    return isDUbefore(v);
  }
}
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

    // Declared in Modifiers.jrag at line 294
    public boolean Define_boolean_mayBeStrictfp(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeStrictfp(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 236
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBodyDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    BodyDecl b = getBodyDecl(childIndex);
    //if(b instanceof MethodDecl || b instanceof MemberTypeDecl) {
    if(!v.isInstanceVariable() && !v.isClassVariable()) {
      if(v.hostType() != this)
        return isDAbefore(v);
      return false;
    }
    if(b instanceof FieldDeclaration && !((FieldDeclaration)b).isStatic() && v.isClassVariable())
      return true;

    if(b instanceof MethodDecl) {
      return true;
    }
    if(b instanceof MemberTypeDecl && v.isBlank() && v.isFinal() && v.hostType() == this)
      return true;
    if(v.isClassVariable() || v.isInstanceVariable()) {
      if(v.isFinal() &&  v.hostType() != this && instanceOf(v.hostType()))
        return true;
      int index = childIndex - 1;
      if(b instanceof ConstructorDecl)
        index = getNumBodyDecl() - 1;
        
      for(int i = index; i >= 0; i--) {
        b = getBodyDecl(i);
        if(b instanceof FieldDeclaration) {
          FieldDeclaration f = (FieldDeclaration)b;
          if((v.isClassVariable() && f.isStatic()) || (v.isInstanceVariable() && !f.isStatic())) {
            boolean c = f.isDAafter(v);
            //System.err.println("DefiniteAssignment: is " + v.name() + " DA after index " + i + ", " + f + ": " + c);
            return c;
            //return f.isDAafter(v);
          }
        }
        else if(b instanceof StaticInitializer && v.isClassVariable()) {
          StaticInitializer si = (StaticInitializer)b;
          return si.isDAafter(v);
        }
        else if(b instanceof InstanceInitializer && v.isInstanceVariable()) {
          InstanceInitializer ii = (InstanceInitializer)b;
          return ii.isDAafter(v);
        }
      }
    }
    return isDAbefore(v);
  }
}
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in NameCheck.jrag at line 366
    public boolean Define_boolean_insideSwitch(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_insideSwitch(this, caller);
    }

    // Declared in TypeCheck.jrag at line 393
    public TypeDecl Define_TypeDecl_returnType(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  typeVoid();
        }
        return getParent().Define_TypeDecl_returnType(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 416
    public Stmt Define_Stmt_entryBlock(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  getBodyDecl(i).entryBlock();
        }
        return getParent().Define_Stmt_entryBlock(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 536
    public boolean Define_boolean_isLocalClass(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isLocalClass(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 514
    public boolean Define_boolean_isNestedType(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        return getParent().Define_boolean_isNestedType(this, caller);
    }

    // Declared in TypeHierarchyCheck.jrag at line 3
    public String Define_String_methodHost(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  typeName();
        }
        return getParent().Define_String_methodHost(this, caller);
    }

    // Declared in LookupType.jrag at line 212
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getBodyDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    SimpleSet c = memberTypes(name);
    if(!c.isEmpty()) 
      return c;
    if(name().equals(name))
      return SimpleSet.emptySet.add(this);

    c = lookupType(name);
    // 8.5.2
    if(isClassDecl() && isStatic() && !isTopLevelType()) {
      SimpleSet newSet = SimpleSet.emptySet;
      for(Iterator iter = c.iterator(); iter.hasNext(); ) {
        TypeDecl d = (TypeDecl)iter.next();
        //if(d.isStatic() || d.isTopLevelType() || this.instanceOf(d.enclosingType())) {
          newSet = newSet.add(d);
        //}
      }
      c = newSet;
    }
    return c;
  }
}
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

    // Declared in Modifiers.jrag at line 289
    public boolean Define_boolean_mayBeStatic(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeStatic(this, caller);
    }

    // Declared in LookupMethod.jrag at line 22
    public Collection Define_Collection_lookupMethod(ASTNode caller, ASTNode child, String name) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  unqualifiedLookupMethod(name);
        }
        return getParent().Define_Collection_lookupMethod(this, caller, name);
    }

    // Declared in ControlFlowGraph.jrag at line 175
    public boolean Define_boolean_withInCatchClause(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_withInCatchClause(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 565
    public TypeDecl Define_TypeDecl_hostType(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  hostType();
        }
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  hostType();
        }
        return getParent().Define_TypeDecl_hostType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 11
    public boolean Define_boolean_isDest(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isDest(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 21
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        return getParent().Define_boolean_isSource(this, caller);
    }

    // Declared in AccessMethod.jrag at line 12
    public Access Define_Access_accessMethod(ASTNode caller, ASTNode child, MethodDecl md, List args) {
        if(caller == getBodyDeclListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		if(lookupMethod(md.getID()).contains(md)) {
			return new MethodAccess(md.getID(), args);
		} else if(md.hostType().encloses(hostType())) {
			// we need an expression of the form A.this.x
			Access typacc = getBodyDecl(i).accessType(md.hostType(), true);
			if(typacc == null)
				return null;
			return typacc.qualifiesAccess(new ThisAccess("this").qualifiesAccess(
					new MethodAccess(md.getID(), args)));
		} else if(hostType().instanceOf(md.hostType())) {
			// we need to qualify either with `super' or with `(A)this'
			if(hostType() instanceof ClassDecl && 
					((ClassDecl)hostType()).superclass() == md.hostType()) {
				return new SuperAccess("super").qualifiesAccess(
						new MethodAccess(md.getID(), args));
			} else if(!md.isStatic()) {
				// non-static overridden methods of the superclass can be accessed,
				// anything further up is off-limits
				return null;
			} else {
				Access typacc = getBodyDecl(i).accessType(md.hostType(), true);
				if(typacc == null) return null;
				return new ParExpr(new CastExpr(typacc, new ThisAccess("this"))).
					qualifiesAccess(new MethodAccess(md.getID(), args));
			}
		} else {
			assert(false);
		}
	}
}
        return getParent().Define_Access_accessMethod(this, caller, md, args);
    }

    // Declared in ControlFlowGraph.jrag at line 206
    public ConstCase Define_ConstCase_followingConstCase(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  null;
        }
        return getParent().Define_ConstCase_followingConstCase(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 238
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  Set.empty();
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in LocalDeclaration.jrag at line 35
    public Collection Define_Collection_visibleLocalDecls(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  new ArrayList();
        }
        return getParent().Define_Collection_visibleLocalDecls(this, caller);
    }

    // Declared in AccessType.jrag at line 40
    public Access Define_Access_accessType(ASTNode caller, ASTNode child, TypeDecl td, boolean ambiguous) {
        if(caller == getBodyDeclListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		if(memberTypes(td.getID()).contains(td)) {
			if(ambiguous && !getBodyDecl(i).lookupVariable(td.getID()).isEmpty()) {
				// if we are in an ambiguous context, we cannot access a member
				// type shadowed by a field of the same name
				return null;
			} else {
				return new TypeAccess(td.getID());
			}
		}
		if(td.isNestedType()) {
			TypeDecl enc = td.enclosingType();
			Access encacc = getBodyDecl(i).accessType(enc, ambiguous);
			if(encacc == null) return null;
			Access acc = enc.getBodyDecl(0).accessType(td, ambiguous);
			if(acc == null) return null;
			return encacc.qualifiesAccess(acc);
		} else {
			Access ta = accessType(td, ambiguous);
			if(ta == null) return null;
			String pkgname = "";
			if(ta instanceof TypeAccess) {
				pkgname = ((TypeAccess)ta).getPackage();
			} else if(ta instanceof AbstractDot) {
				pkgname = ((TypeAccess)((AbstractDot)ta).getRight()).getPackage();
			} else {
				assert(false);
			}
			if(pkgname.equals("") && memberTypes(td.getID()).isEmpty() 
					&& memberFields(td.getID()).isEmpty()) {
				return ta;
			} else {
				Access pkgacc = getBodyDecl(i).accessPackage(td.packageName());
				if(pkgacc == null) return null;
				return pkgacc.qualifiesAccess(ta);
			}
		}
	}
}
        return getParent().Define_Access_accessType(this, caller, td, ambiguous);
    }

    // Declared in LookupVariable.jrag at line 18
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getBodyDeclListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
    SimpleSet list = memberFields(name);
    if(!list.isEmpty()) return list;
    list = lookupVariable(name);
    if(inStaticContext() || isStatic())
      list = removeInstanceVariables(list);
    else if(isAnonymous() && inExplicitConstructorInvocation()) {
      TypeDecl typeDecl = enclosingType();
      SimpleSet newSet = SimpleSet.emptySet;
      for(Iterator iter = list.iterator(); iter.hasNext(); ) {
        Variable v = (Variable)iter.next();
        if(!v.isInstanceVariable() || !typeDecl.memberFields(name).contains(v))
          newSet = newSet.add(v);
      }
      return newSet;
    }
    return list;
  }
}
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in TypeAnalysis.jrag at line 494
    public TypeDecl Define_TypeDecl_enclosingType(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  this;
        }
        return getParent().Define_TypeDecl_enclosingType(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 221
    public DefaultCase Define_DefaultCase_followingDefaultCase(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  null;
        }
        return getParent().Define_DefaultCase_followingDefaultCase(this, caller);
    }

    // Declared in Modifiers.jrag at line 291
    public boolean Define_boolean_mayBeAbstract(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeAbstract(this, caller);
    }

    // Declared in AccessField.jrag at line 45
    public Access Define_Access_accessField(ASTNode caller, ASTNode child, FieldDeclaration fd) {
        if(caller == getBodyDeclListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		if(localFieldsMap().containsValue(fd))
			return new VarAccess(fd.getID());
		if(localFieldsMap().containsKey(fd.getID())) {
			// we need to qualify
			if(fd.hostType().encloses(hostType())) {
				// we need an expression of the form A.this.x
				Access typacc = getBodyDecl(i).accessType(fd.hostType(), true);
				if(typacc == null)
					return null;
				return typacc.qualifiesAccess(new ThisAccess("this")).qualifiesAccess(new VarAccess(fd.getID()));
			} else if(hostType().instanceOf(fd.hostType())) {
				// first try looking it up in super class, if any
				if(hostType() instanceof ClassDecl && ((ClassDecl)hostType()).superclass().getBodyDecl(0).accessField(fd) != null) {
					ClassDecl host = (ClassDecl)hostType();
					ClassDecl supclass = host.superclass();
					Access acc = supclass.getBodyDecl(0).accessField(fd);
					if(acc instanceof AbstractDot) {
						Expr left = ((AbstractDot)acc).getLeft();
						Access right = ((AbstractDot)acc).getRight();
						if(left.isThisAccess()) {
							// if it is this.x in superclass, it's super.x in here
							return new SuperAccess("super").qualifiesAccess(right);
						} else if(left.isSuperAccess()) {
							// if it is super.x in superclass, we need a cast
							ClassDecl supsupclass = supclass.superclass();
							Access supsupacc = getBodyDecl(i).accessType(supsupclass, true);
							if(supsupacc == null) return null;
							return new ParExpr(new CastExpr(supsupacc, new ThisAccess("this"))).
								qualifiesAccess(right);
						} else {
							// it must be of the form ((A)this).x, which we can reuse
							return acc;
						}
					} else if(acc instanceof VarAccess) {
						return new SuperAccess("super").qualifiesAccess(acc);
					} else {
						assert(false);
					}
				} else {
					Access typacc = getBodyDecl(i).accessType(fd.hostType(), true);
					if(typacc == null) return null;
					return new ParExpr(new CastExpr(typacc, new ThisAccess("this"))).qualifiesAccess(new VarAccess(fd.getID()));
				}
			} else {
				assert(false);
			}
		} else {
			if(lookupVariable(fd.getID()) == fd) {
				return new VarAccess(fd.getID());
			}
            // first, try finding it in supertypes
			Access acc = accessFieldInSupertypes(fd);
            if(acc != null) {
                if(acc instanceof AbstractDot) {
                    Expr left = ((AbstractDot)acc).getLeft();
                    Access right = ((AbstractDot)acc).getRight();
                    if(left.isSuperAccess()) {
                        Access typacc = getBodyDecl(i).accessType(fd.hostType(), true);
                        if(typacc == null) return null;
                        return new ParExpr(new CastExpr(typacc, new ThisAccess("this"))).
                                    qualifiesAccess(right);
                    }
                }
				return acc;
            }
            // otherwise, it must be in an enclosing type
            if(!isNestedType()) return null;
            acc = accessField(fd);
            if(acc == null) return null;

            // if the field can be reached by unqualified access in enclosing type,
            // we are good; otherwise, we may have to fiddle with the qualifier
			if(acc instanceof AbstractDot) {
	            Expr left = ((AbstractDot)acc).getLeft();
				Access right = ((AbstractDot)acc).getRight();
                if(left instanceof ThisAccess) {
                    // this happens if we are an anonymous class and the enclosing method
                    // shadows the field we want to access
                    Access tacc = getBodyDecl(i).accessType(enclosingType(), true);
                    if(tacc == null) return null;
                    return tacc.qualifiesAccess(acc);
                } else if(left.isCastedThisAccess()) {
					Access tacc1 = ((CastExpr)((ParExpr)left).getExpr()).getTypeAccess();
					Access tacc2 = getBodyDecl(i).accessType(enclosingType(), true);
					if(tacc2 == null) return null;
					Access res = new ParExpr(new CastExpr(tacc1, tacc2.qualifiesAccess(new ThisAccess("this")))).
						qualifiesAccess(right);
					//System.out.println("now changed access "+acc.dumpTree()+" in enclosing type into "+res.dumpTree());
					return res;
				} else if(left.isSuperAccess()) {
					ClassDecl supclass = ((ClassDecl)enclosingType()).superclass();
					Access tacc1 = getBodyDecl(i).accessType(supclass, true);
					if(tacc1 == null) return null;
					Access tacc2 = getBodyDecl(i).accessType(enclosingType(), true);
					if(tacc2 == null) return null;
					Access res = new ParExpr(new CastExpr(tacc1, tacc2.qualifiesAccess(new ThisAccess("this")))).
						qualifiesAccess(right);
					//System.out.println("changed access "+acc.dumpTree()+" in enclosing class into "+res.dumpTree());
					return res;
				} else {
					return acc;
				}
			} else {
				return acc;
			}
		}
		return null;
	}
}
        return getParent().Define_Access_accessField(this, caller, fd);
    }

    // Declared in TypeAnalysis.jrag at line 210
    public boolean Define_boolean_isAnonymous(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isAnonymous(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 413
    public Stmt Define_Stmt_exitBlock(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  getBodyDecl(i).exitBlock();
        }
        return getParent().Define_Stmt_exitBlock(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 168
    public Set Define_Set_enclosingFinally(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  Set.empty();
        }
        return getParent().Define_Set_enclosingFinally(this, caller);
    }

    // Declared in Modifiers.jrag at line 292
    public boolean Define_boolean_mayBeVolatile(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_mayBeVolatile(this, caller);
    }

    // Declared in NameCheck.jrag at line 293
    public BodyDecl Define_BodyDecl_enclosingBodyDecl(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  getBodyDecl(i);
        }
        return getParent().Define_BodyDecl_enclosingBodyDecl(this, caller);
    }

    // Declared in Modifiers.jrag at line 295
    public boolean Define_boolean_mayBeSynchronized(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_mayBeSynchronized(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 148
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  true;
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in NameCheck.jrag at line 280
    public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  this;
        }
        return getParent().Define_VariableScope_outerScope(this, caller);
    }

    // Declared in TypeCheck.jrag at line 429
    public TypeDecl Define_TypeDecl_enclosingInstance(ASTNode caller, ASTNode child) {
        if(caller == getBodyDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    if(getBodyDecl(childIndex) instanceof MemberTypeDecl && !((MemberTypeDecl)getBodyDecl(childIndex)).typeDecl().isInnerType())
      return null;
    if(getBodyDecl(childIndex) instanceof ConstructorDecl)
      return enclosingInstance();
    return this;
  }
}
        return getParent().Define_TypeDecl_enclosingInstance(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected boolean TypeDecl_uses_visited = false;
    protected boolean TypeDecl_uses_computed = false;
    protected HashSet TypeDecl_uses_value;
    // Declared in Uses.jrag at line 57
    public HashSet uses() {
        if(TypeDecl_uses_computed)
            return TypeDecl_uses_value;
        if(TypeDecl_uses_visited)
            throw new RuntimeException("Circular definition of attr: uses in class: ");
        TypeDecl_uses_visited = true;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        TypeDecl_uses_value = uses_compute();
        if(isFinal && num == boundariesCrossed)
            TypeDecl_uses_computed = true;
        TypeDecl_uses_visited = false;
        return TypeDecl_uses_value;
    }

    java.util.HashSet TypeDecl_uses_contributors = new java.util.HashSet();
    private HashSet uses_compute() {
        ASTNode node = this;
        while(node.getParent() != null)
            node = node.getParent();
        Program root = (Program)node;
        root.collect_contributors_TypeDecl_uses();
        TypeDecl_uses_value = new HashSet();
        for(java.util.Iterator iter = TypeDecl_uses_contributors.iterator(); iter.hasNext(); ) {
            ASTNode contributor = (ASTNode)iter.next();
            contributor.contributeTo_TypeDecl_TypeDecl_uses(TypeDecl_uses_value);
        }
        return TypeDecl_uses_value;
    }

}
