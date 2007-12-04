
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;


public abstract class Expr extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
        accessField_FieldDeclaration_values = null;
        accessType_TypeDecl_boolean_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        Expr node = (Expr)super.clone();
        node.accessField_FieldDeclaration_values = null;
        node.accessType_TypeDecl_boolean_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in LookupType.jrag at line 315

    
  public SimpleSet keepAccessibleTypes(SimpleSet oldSet) {
    SimpleSet newSet = SimpleSet.emptySet;
    TypeDecl hostType = hostType();
    for(Iterator iter = oldSet.iterator(); iter.hasNext(); ) {
      TypeDecl t = (TypeDecl)iter.next();
      if((hostType != null && t.accessibleFrom(hostType)) || (hostType == null && t.accessibleFromPackage(hostPackage())))
        newSet = newSet.add(t);
    }
    return newSet;
  }

    // Declared in LookupVariable.jrag at line 166


  // remove fields that are not accessible when using this Expr as qualifier
  public SimpleSet keepAccessibleFields(SimpleSet oldSet) {
    SimpleSet newSet = SimpleSet.emptySet;
    for(Iterator iter = oldSet.iterator(); iter.hasNext(); ) {
      Variable v = (Variable)iter.next();
      if(v instanceof FieldDeclaration) {
        FieldDeclaration f = (FieldDeclaration)v;
        if(mayAccess(f))
          newSet = newSet.add(f);
      }
    }
    return newSet;
  }

    // Declared in LookupVariable.jrag at line 189


  private boolean mayAccess(FieldDeclaration f) {
    if(f.isPublic()) 
      return true;
    else if(f.isProtected()) {
      if(f.hostPackage().equals(hostPackage()))
        return true;
      TypeDecl C = f.hostType();
      TypeDecl S = hostType().subclassWithinBody(C);
      TypeDecl Q = type();
      if(S == null)
        return false;
      if(f.isInstanceVariable() && !isSuperAccess())
        return Q.instanceOf(S);
      return true;
    }
    else if(f.isPrivate())
      return f.hostType().topLevelType() == hostType().topLevelType();
    else
      return f.hostPackage().equals(hostType().hostPackage());
  }

    // Declared in ResolveAmbiguousNames.jrag at line 90


  public Dot qualifiesAccess(Access access) {
    Dot dot = new Dot(this, access);
    dot.lastDot = dot;
    return dot;
  }

    // Declared in AccessType.jrag at line 168

	
	boolean isCastedThisAccess() { return false; }

    // Declared in AdjustAccess.jrag at line 45

	
	public void adjust(java.util.List changes, AdjustmentTable table) throws RefactoringException {
	}

    // Declared in MergeAccess.jrag at line 5


	// the field access f is either a simple name or qualified with this, super, ((A)this),
	// A.this, or ((A)B.this) for some classes A and B
	public Access mergeWithAccess(Access f) {
		if(f.isVariable() || f instanceof TypeAccess || f instanceof MethodAccess) {
			return this.qualifiesAccess(f);
		} else if(f instanceof AbstractDot) {
			Expr left = ((AbstractDot)f).getLeft();
			Access right = ((AbstractDot)f).getRight();
			if(left.isThisAccess())
				return mergeWithAccess(right);
			else if(left.isSuperAccess()) {
				if(type() instanceof ClassDecl) {
					ClassDecl cdcl = (ClassDecl)type();
					if(cdcl.hasSuperclass())
						return new ParExpr(new CastExpr((TypeAccess)cdcl.getSuperClassAccess().fullCopy(), this)).
									mergeWithAccess(right);
				}
				return null;
			} else if(left instanceof ParExpr) {
				Expr e = ((ParExpr)left).getExpr();
				return mergeWithAccess(new Dot(e, right));
			} else if(left instanceof CastExpr) {
				Access tp = ((CastExpr)left).getTypeAccess();
				Expr e = ((CastExpr)left).getExpr();
				if(e instanceof ThisAccess) {
					return new ParExpr(new CastExpr((TypeAccess)tp, this)).
								mergeWithAccess(right);
				}
			} else if(left instanceof AbstractDot) {
				Expr lleft = ((AbstractDot)left).getLeft();
				Access lright = ((AbstractDot)left).getRight();
				if(lright.isThisAccess()) {
					// apparently, there is no way to make this work...
					return null;
				} else
					return null;
			} else
				return null;
		} else {
			assert(false);
		}
		return null;
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 98

    public Expr() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    // Declared in TypeAnalysis.jrag at line 278
    public abstract TypeDecl type();
    // Declared in ConstantExpression.jrag at line 89
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute()  {
    throw new UnsupportedOperationException("ConstantExpression operation constant" +
      " not supported for type " + getClass().getName()); 
  }

    // Declared in ConstantExpression.jrag at line 219
    public boolean isPositive() {
        boolean isPositive_value = isPositive_compute();
        return isPositive_value;
    }

    private boolean isPositive_compute() {  return  false;  }

    // Declared in ConstantExpression.jrag at line 429
    public boolean representableIn(TypeDecl t) {
        boolean representableIn_TypeDecl_value = representableIn_compute(t);
        return representableIn_TypeDecl_value;
    }

    private boolean representableIn_compute(TypeDecl t)  {	
  	if (!type().isByte() && !type().isChar() && !type().isShort() && !type().isInt()) {
  		return false;
  	}
  	if (t.isByte())
  		return constant().intValue() >= Byte.MIN_VALUE && constant().intValue() <= Byte.MAX_VALUE;
  	if (t.isChar())
  		return constant().intValue() >= Character.MIN_VALUE && constant().intValue() <= Character.MAX_VALUE;
  	if (t.isShort())
  		return constant().intValue() >= Short.MIN_VALUE && constant().intValue() <= Short.MAX_VALUE;
    if(t.isInt()) 
      return constant().intValue() >= Integer.MIN_VALUE && constant().intValue() <= Integer.MAX_VALUE;
	  return false;
  }

    // Declared in ConstantExpression.jrag at line 457
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  false;  }

    // Declared in ConstantExpression.jrag at line 486
    public boolean isTrue() {
        boolean isTrue_value = isTrue_compute();
        return isTrue_value;
    }

    private boolean isTrue_compute() {  return  isConstant() && type() instanceof BooleanType && constant().booleanValue();  }

    // Declared in ConstantExpression.jrag at line 487
    public boolean isFalse() {
        boolean isFalse_value = isFalse_compute();
        return isFalse_value;
    }

    private boolean isFalse_compute() {  return  isConstant() && type() instanceof BooleanType && !constant().booleanValue();  }

    // Declared in DefiniteAssignment.jrag at line 47
    public Variable varDecl() {
        Variable varDecl_value = varDecl_compute();
        return varDecl_value;
    }

    private Variable varDecl_compute() {  return  null;  }

    // Declared in DefiniteAssignment.jrag at line 329
    public boolean isDAafterFalse(Variable v) {
        boolean isDAafterFalse_Variable_value = isDAafterFalse_compute(v);
        return isDAafterFalse_Variable_value;
    }

    private boolean isDAafterFalse_compute(Variable v) {  return  isTrue() || isDAbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 331
    public boolean isDAafterTrue(Variable v) {
        boolean isDAafterTrue_Variable_value = isDAafterTrue_compute(v);
        return isDAafterTrue_Variable_value;
    }

    private boolean isDAafterTrue_compute(Variable v) {  return  isFalse() || isDAbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 334
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v) {  return  (isDAafterFalse(v) && isDAafterTrue(v)) || isDAbefore(v);  }

    // Declared in DefiniteAssignment.jrag at line 778
    public boolean isDUafterFalse(Variable v) {
        boolean isDUafterFalse_Variable_value = isDUafterFalse_compute(v);
        return isDUafterFalse_Variable_value;
    }

    private boolean isDUafterFalse_compute(Variable v)  {
    if(isTrue())
      return true;
    return isDUbefore(v);
  }

    // Declared in DefiniteAssignment.jrag at line 784
    public boolean isDUafterTrue(Variable v) {
        boolean isDUafterTrue_Variable_value = isDUafterTrue_compute(v);
        return isDUafterTrue_Variable_value;
    }

    private boolean isDUafterTrue_compute(Variable v)  {
    if(isFalse())
      return true;
    return isDUbefore(v);
  }

    // Declared in DefiniteAssignment.jrag at line 794
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v) {  return  (isDUafterFalse(v) && isDUafterTrue(v)) || isDUbefore(v);  }

    // Declared in LookupConstructor.jrag at line 23
    public SimpleSet mostSpecificConstructor(Collection constructors) {
        SimpleSet mostSpecificConstructor_Collection_value = mostSpecificConstructor_compute(constructors);
        return mostSpecificConstructor_Collection_value;
    }

    private SimpleSet mostSpecificConstructor_compute(Collection constructors)  {
    SimpleSet maxSpecific = SimpleSet.emptySet;
    for(Iterator iter = constructors.iterator(); iter.hasNext(); ) {
      ConstructorDecl decl = (ConstructorDecl)iter.next();
      if(applicableAndAccessible(decl)) {
        if(maxSpecific.isEmpty())
          maxSpecific = maxSpecific.add(decl);
        else {
          if(decl.moreSpecificThan((ConstructorDecl)maxSpecific.iterator().next()))
            maxSpecific = SimpleSet.emptySet.add(decl);
          else if(!((ConstructorDecl)maxSpecific.iterator().next()).moreSpecificThan(decl))
            maxSpecific = maxSpecific.add(decl);
        }
      }
    }
    return maxSpecific;
  }

    // Declared in LookupConstructor.jrag at line 41
    public boolean applicableAndAccessible(ConstructorDecl decl) {
        boolean applicableAndAccessible_ConstructorDecl_value = applicableAndAccessible_compute(decl);
        return applicableAndAccessible_ConstructorDecl_value;
    }

    private boolean applicableAndAccessible_compute(ConstructorDecl decl) {  return  false;  }

    // Declared in LookupType.jrag at line 74
    public boolean hasQualifiedPackage(String packageName) {
        boolean hasQualifiedPackage_String_value = hasQualifiedPackage_compute(packageName);
        return hasQualifiedPackage_String_value;
    }

    private boolean hasQualifiedPackage_compute(String packageName) {  return  false;  }

    // Declared in LookupType.jrag at line 284
    public SimpleSet qualifiedLookupType(String name) {
        SimpleSet qualifiedLookupType_String_value = qualifiedLookupType_compute(name);
        return qualifiedLookupType_String_value;
    }

    private SimpleSet qualifiedLookupType_compute(String name) {  return 
    keepAccessibleTypes(type().memberTypes(name));  }

    // Declared in LookupVariable.jrag at line 148
    public SimpleSet qualifiedLookupVariable(String name) {
        SimpleSet qualifiedLookupVariable_String_value = qualifiedLookupVariable_compute(name);
        return qualifiedLookupVariable_String_value;
    }

    private SimpleSet qualifiedLookupVariable_compute(String name)  {
    if(type().accessibleFrom(hostType()))
      return keepAccessibleFields(type().memberFields(name));
    return SimpleSet.emptySet;
  }

    // Declared in QualifiedNames.jrag at line 16
    public String packageName() {
        String packageName_value = packageName_compute();
        return packageName_value;
    }

    private String packageName_compute() {  return  "";  }

    // Declared in QualifiedNames.jrag at line 53
    public String typeName() {
        String typeName_value = typeName_compute();
        return typeName_value;
    }

    private String typeName_compute() {  return  "";  }

    // Declared in ResolveAmbiguousNames.jrag at line 4
    public boolean isTypeAccess() {
        boolean isTypeAccess_value = isTypeAccess_compute();
        return isTypeAccess_value;
    }

    private boolean isTypeAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 8
    public boolean isMethodAccess() {
        boolean isMethodAccess_value = isMethodAccess_compute();
        return isMethodAccess_value;
    }

    private boolean isMethodAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 12
    public boolean isFieldAccess() {
        boolean isFieldAccess_value = isFieldAccess_compute();
        return isFieldAccess_value;
    }

    private boolean isFieldAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 16
    public boolean isSuperAccess() {
        boolean isSuperAccess_value = isSuperAccess_compute();
        return isSuperAccess_value;
    }

    private boolean isSuperAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 22
    public boolean isThisAccess() {
        boolean isThisAccess_value = isThisAccess_compute();
        return isThisAccess_value;
    }

    private boolean isThisAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 28
    public boolean isPackageAccess() {
        boolean isPackageAccess_value = isPackageAccess_compute();
        return isPackageAccess_value;
    }

    private boolean isPackageAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 32
    public boolean isArrayAccess() {
        boolean isArrayAccess_value = isArrayAccess_compute();
        return isArrayAccess_value;
    }

    private boolean isArrayAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 36
    public boolean isClassAccess() {
        boolean isClassAccess_value = isClassAccess_compute();
        return isClassAccess_value;
    }

    private boolean isClassAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 40
    public boolean isSuperConstructorAccess() {
        boolean isSuperConstructorAccess_value = isSuperConstructorAccess_compute();
        return isSuperConstructorAccess_value;
    }

    private boolean isSuperConstructorAccess_compute() {  return  false;  }

    // Declared in ResolveAmbiguousNames.jrag at line 46
    public boolean isLeftChildOfDot() {
        boolean isLeftChildOfDot_value = isLeftChildOfDot_compute();
        return isLeftChildOfDot_value;
    }

    private boolean isLeftChildOfDot_compute() {  return  hasParentDot() && parentDot().getLeft() == this;  }

    // Declared in ResolveAmbiguousNames.jrag at line 47
    public boolean isRightChildOfDot() {
        boolean isRightChildOfDot_value = isRightChildOfDot_compute();
        return isRightChildOfDot_value;
    }

    private boolean isRightChildOfDot_compute() {  return  hasParentDot() && parentDot().getRight() == this;  }

    // Declared in ResolveAmbiguousNames.jrag at line 60
    public AbstractDot parentDot() {
        AbstractDot parentDot_value = parentDot_compute();
        return parentDot_value;
    }

    private AbstractDot parentDot_compute() {  return  getParent() instanceof AbstractDot ? (AbstractDot)getParent() : null;  }

    // Declared in ResolveAmbiguousNames.jrag at line 61
    public boolean hasParentDot() {
        boolean hasParentDot_value = hasParentDot_compute();
        return hasParentDot_value;
    }

    private boolean hasParentDot_compute() {  return  parentDot() != null;  }

    // Declared in ResolveAmbiguousNames.jrag at line 63
    public Access nextAccess() {
        Access nextAccess_value = nextAccess_compute();
        return nextAccess_value;
    }

    private Access nextAccess_compute() {  return  parentDot().nextAccess();  }

    // Declared in ResolveAmbiguousNames.jrag at line 64
    public boolean hasNextAccess() {
        boolean hasNextAccess_value = hasNextAccess_compute();
        return hasNextAccess_value;
    }

    private boolean hasNextAccess_compute() {  return  isLeftChildOfDot();  }

    // Declared in TypeAnalysis.jrag at line 503
    public Stmt enclosingStmt() {
        Stmt enclosingStmt_value = enclosingStmt_compute();
        return enclosingStmt_value;
    }

    private Stmt enclosingStmt_compute()  {
    ASTNode node = this;
    while(node != null && !(node instanceof Stmt))
      node = node.getParent();
    return (Stmt)node;
  }

    // Declared in TypeCheck.jrag at line 6
    public boolean isVariable() {
        boolean isVariable_value = isVariable_compute();
        return isVariable_value;
    }

    private boolean isVariable_compute() {  return  false;  }

    // Declared in TypeHierarchyCheck.jrag at line 11
    public boolean isUnknown() {
        boolean isUnknown_value = isUnknown_compute();
        return isUnknown_value;
    }

    private boolean isUnknown_compute() {  return  type().isUnknown();  }

    // Declared in TypeHierarchyCheck.jrag at line 141
    public boolean staticContextQualifier() {
        boolean staticContextQualifier_value = staticContextQualifier_compute();
        return staticContextQualifier_value;
    }

    private boolean staticContextQualifier_compute() {  return  false;  }

    // Declared in AccessField.jrag at line 209
    public Access qualifiedAccessField(FieldDeclaration fd) {
        Access qualifiedAccessField_FieldDeclaration_value = qualifiedAccessField_compute(fd);
        return qualifiedAccessField_FieldDeclaration_value;
    }

    private Access qualifiedAccessField_compute(FieldDeclaration fd)  {
		return type().getBodyDecl(0).accessField(fd);
	}

    // Declared in AccessMethod.jrag at line 47
    public Access qualifiedAccessMethod(MethodDecl md, List args) {
        Access qualifiedAccessMethod_MethodDecl_List_value = qualifiedAccessMethod_compute(md, args);
        return qualifiedAccessMethod_MethodDecl_List_value;
    }

    private Access qualifiedAccessMethod_compute(MethodDecl md, List args)  {
		return type().getBodyDecl(0).accessMethod(md, args);
	}

    // Declared in AccessType.jrag at line 153
    public Access qualifiedAccessType(TypeDecl td, boolean ambiguous) {
        Access qualifiedAccessType_TypeDecl_boolean_value = qualifiedAccessType_compute(td, ambiguous);
        return qualifiedAccessType_TypeDecl_boolean_value;
    }

    private Access qualifiedAccessType_compute(TypeDecl td, boolean ambiguous)  {
		return type().getBodyDecl(0).accessType(td, ambiguous);
	}

    // Declared in DefiniteAssignment.jrag at line 6
    public boolean isDest() {
        boolean isDest_value = getParent().Define_boolean_isDest(this, null);
        return isDest_value;
    }

    // Declared in DefiniteAssignment.jrag at line 16
    public boolean isSource() {
        boolean isSource_value = getParent().Define_boolean_isSource(this, null);
        return isSource_value;
    }

    // Declared in DefiniteAssignment.jrag at line 38
    public boolean isIncOrDec() {
        boolean isIncOrDec_value = getParent().Define_boolean_isIncOrDec(this, null);
        return isIncOrDec_value;
    }

    // Declared in DefiniteAssignment.jrag at line 225
    public boolean isDAbefore(Variable v) {
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        return isDAbefore_Variable_value;
    }

    // Declared in DefiniteAssignment.jrag at line 690
    public boolean isDUbefore(Variable v) {
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        return isDUbefore_Variable_value;
    }

    // Declared in LookupMethod.jrag at line 14
    public Collection lookupMethod(String name) {
        Collection lookupMethod_String_value = getParent().Define_Collection_lookupMethod(this, null, name);
        return lookupMethod_String_value;
    }

    // Declared in LookupType.jrag at line 40
    public TypeDecl typeBoolean() {
        TypeDecl typeBoolean_value = getParent().Define_TypeDecl_typeBoolean(this, null);
        return typeBoolean_value;
    }

    // Declared in LookupType.jrag at line 41
    public TypeDecl typeByte() {
        TypeDecl typeByte_value = getParent().Define_TypeDecl_typeByte(this, null);
        return typeByte_value;
    }

    // Declared in LookupType.jrag at line 42
    public TypeDecl typeShort() {
        TypeDecl typeShort_value = getParent().Define_TypeDecl_typeShort(this, null);
        return typeShort_value;
    }

    // Declared in LookupType.jrag at line 43
    public TypeDecl typeChar() {
        TypeDecl typeChar_value = getParent().Define_TypeDecl_typeChar(this, null);
        return typeChar_value;
    }

    // Declared in LookupType.jrag at line 44
    public TypeDecl typeInt() {
        TypeDecl typeInt_value = getParent().Define_TypeDecl_typeInt(this, null);
        return typeInt_value;
    }

    // Declared in LookupType.jrag at line 45
    public TypeDecl typeLong() {
        TypeDecl typeLong_value = getParent().Define_TypeDecl_typeLong(this, null);
        return typeLong_value;
    }

    // Declared in LookupType.jrag at line 46
    public TypeDecl typeFloat() {
        TypeDecl typeFloat_value = getParent().Define_TypeDecl_typeFloat(this, null);
        return typeFloat_value;
    }

    // Declared in LookupType.jrag at line 47
    public TypeDecl typeDouble() {
        TypeDecl typeDouble_value = getParent().Define_TypeDecl_typeDouble(this, null);
        return typeDouble_value;
    }

    // Declared in LookupType.jrag at line 48
    public TypeDecl typeString() {
        TypeDecl typeString_value = getParent().Define_TypeDecl_typeString(this, null);
        return typeString_value;
    }

    // Declared in LookupType.jrag at line 49
    public TypeDecl typeVoid() {
        TypeDecl typeVoid_value = getParent().Define_TypeDecl_typeVoid(this, null);
        return typeVoid_value;
    }

    // Declared in LookupType.jrag at line 50
    public TypeDecl typeNull() {
        TypeDecl typeNull_value = getParent().Define_TypeDecl_typeNull(this, null);
        return typeNull_value;
    }

    // Declared in LookupType.jrag at line 63
    public TypeDecl unknownType() {
        TypeDecl unknownType_value = getParent().Define_TypeDecl_unknownType(this, null);
        return unknownType_value;
    }

    // Declared in LookupType.jrag at line 77
    public boolean hasPackage(String packageName) {
        boolean hasPackage_String_value = getParent().Define_boolean_hasPackage(this, null, packageName);
        return hasPackage_String_value;
    }

    // Declared in LookupType.jrag at line 85
    public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    // Declared in LookupType.jrag at line 176
    public SimpleSet lookupType(String name) {
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        return lookupType_String_value;
    }

    // Declared in LookupVariable.jrag at line 10
    public SimpleSet lookupVariable(String name) {
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        return lookupVariable_String_value;
    }

    // Declared in TypeAnalysis.jrag at line 561
    public String hostPackage() {
        String hostPackage_value = getParent().Define_String_hostPackage(this, null);
        return hostPackage_value;
    }

    // Declared in TypeAnalysis.jrag at line 576
    public TypeDecl hostType() {
        TypeDecl hostType_value = getParent().Define_TypeDecl_hostType(this, null);
        return hostType_value;
    }

    // Declared in TypeHierarchyCheck.jrag at line 2
    public String methodHost() {
        String methodHost_value = getParent().Define_String_methodHost(this, null);
        return methodHost_value;
    }

    // Declared in TypeHierarchyCheck.jrag at line 125
    public boolean inStaticContext() {
        boolean inStaticContext_value = getParent().Define_boolean_inStaticContext(this, null);
        return inStaticContext_value;
    }

    protected java.util.Map accessField_FieldDeclaration_values;
    // Declared in AccessField.jrag at line 11
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

    // Declared in AccessPackage.jrag at line 6
    public Access accessPackage(String pkg) {
        Access accessPackage_String_value = getParent().Define_Access_accessPackage(this, null, pkg);
        return accessPackage_String_value;
    }

    protected java.util.Map accessType_TypeDecl_boolean_values;
    // Declared in AccessType.jrag at line 8
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

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
