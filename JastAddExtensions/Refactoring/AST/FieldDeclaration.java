
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

public class FieldDeclaration extends MemberDecl implements Cloneable,  SimpleSet,  Iterator,  Variable,  Named {
    public void flushCache() {
        super.flushCache();
        accessibleFrom_TypeDecl_values = null;
        exceptions_computed = false;
        exceptions_value = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        constant_computed = false;
        constant_value = null;
        isLiveBetween_Stmt_Stmt_values = null;
        isLiveAfter_Stmt_values = null;
        isLiveAtOrAfter_Stmt_values = null;
        FieldDeclaration_uses_visited = false;
        FieldDeclaration_uses_computed = false;
        FieldDeclaration_uses_value = null;
    FieldDeclaration_uses_contributors = new java.util.HashSet();
    }
    public Object clone() throws CloneNotSupportedException {
        FieldDeclaration node = (FieldDeclaration)super.clone();
        node.accessibleFrom_TypeDecl_values = null;
        node.exceptions_computed = false;
        node.exceptions_value = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.constant_computed = false;
        node.constant_value = null;
        node.isLiveBetween_Stmt_Stmt_values = null;
        node.isLiveAfter_Stmt_values = null;
        node.isLiveAtOrAfter_Stmt_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          FieldDeclaration node = (FieldDeclaration)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        FieldDeclaration res = (FieldDeclaration)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BoundNames.jrag at line 2

  public Access createQualifiedBoundAccess() {
    if(isStatic())
      return hostType().createQualifiedAccess().qualifiesAccess(new BoundFieldAccess(this));
    else
      return new ThisAccess("this").qualifiesAccess(
        new BoundFieldAccess(this));
  }

    // Declared in BoundNames.jrag at line 77


  public Access createBoundFieldAccess() {
    return createQualifiedBoundAccess();
  }

    // Declared in DataStructures.jrag at line 60

  public SimpleSet add(Object o) {
    return new SimpleSetImpl().add(this).add(o);
  }

    // Declared in DataStructures.jrag at line 66

  private FieldDeclaration iterElem;

    // Declared in DataStructures.jrag at line 67

  public Iterator iterator() { iterElem = this; return this; }

    // Declared in DataStructures.jrag at line 68

  public boolean hasNext() { return iterElem != null; }

    // Declared in DataStructures.jrag at line 69

  public Object next() { Object o = iterElem; iterElem = null; return o; }

    // Declared in DataStructures.jrag at line 70

  public void remove() { throw new UnsupportedOperationException(); }

    // Declared in DefiniteAssignment.jrag at line 168

  
  public void definiteAssignment() {
    super.definiteAssignment();
    if(isBlank() && isFinal() && isClassVariable()) {
      boolean found = false;
      TypeDecl typeDecl = hostType();
      for(int i = 0; i < typeDecl.getNumBodyDecl(); i++) {
        if(typeDecl.getBodyDecl(i) instanceof StaticInitializer) {
          StaticInitializer s = (StaticInitializer)typeDecl.getBodyDecl(i);
          if(s.isDAafter(this))
            found = true;
        }
        
        else if(typeDecl.getBodyDecl(i) instanceof FieldDeclaration) {
          FieldDeclaration f = (FieldDeclaration)typeDecl.getBodyDecl(i);
          if(f.isStatic() && f.isDAafter(this))
            found = true;
        }
        
      }
      if(!found)
        error("blank final class variable " + name() + " in " + hostType().typeName() + " is not definitely assigned in static initializer");

    }
    if(isBlank() && isFinal() && isInstanceVariable()) {
      TypeDecl typeDecl = hostType();
      boolean found = false;
      for(int i = 0; !found && i < typeDecl.getNumBodyDecl(); i++) {
        if(typeDecl.getBodyDecl(i) instanceof FieldDeclaration) {
          FieldDeclaration f = (FieldDeclaration)typeDecl.getBodyDecl(i);
          if(!f.isStatic() && f.isDAafter(this))
            found = true;
        }
        else if(typeDecl.getBodyDecl(i) instanceof InstanceInitializer) {
          InstanceInitializer ii = (InstanceInitializer)typeDecl.getBodyDecl(i);
          if(ii.getBlock().isDAafter(this))
            found = true;
        }
      }
      for(Iterator iter = typeDecl.constructors().iterator(); !found && iter.hasNext(); ) {
        ConstructorDecl c = (ConstructorDecl)iter.next();
        if(!c.isDAafter(this)) {
          error("blank final instance variable " + name() + " in " + hostType().typeName() + " is not definitely assigned after " + c.signature());
          }
      }
    }
    if(isBlank() && hostType().isInterfaceDecl()) {
            error("variable  " + name() + " in " + hostType().typeName() + " which is an interface must have an initializer");
    }

  }

    // Declared in Modifiers.jrag at line 103

 
  public void checkModifiers() {
    super.checkModifiers();
    if(hostType().isInterfaceDecl()) {
      if(isProtected())
        error("an interface field may not be protected");
      if(isPrivate())
        error("an interface field may not be private");
      if(isTransient())
        error("an interface field may not be transient");
      if(isVolatile())
        error("an interface field may not be volatile");
    }
  }

    // Declared in NameCheck.jrag at line 265


  public void nameCheck() {
    super.nameCheck();
    // 8.3
    for(Iterator iter = hostType().memberFields(name()).iterator(); iter.hasNext(); ) {
      Variable v = (Variable)iter.next();
      if(v != this && v.hostType() == hostType())
        error("field named " + name() + " is multiply declared in type " + hostType().typeName());
    }

  }

    // Declared in NodeConstructors.jrag at line 81


  public FieldDeclaration(Modifiers m, Access type, String name) {
    this(m, type, name, new Opt());
  }

    // Declared in NodeConstructors.jrag at line 85

  
  public FieldDeclaration(Modifiers m, Access type, String name, Expr init) {
    this(m, type, name, new Opt(init));
  }

    // Declared in PrettyPrint.jadd at line 177

  
  public void toString(StringBuffer s) {
    s.append(indent());
    getModifiers().toString(s);
    getTypeAccess().toString(s);
    s.append(" " + name());
    if(hasInit()) {
      s.append(" = ");
      getInit().toString(s);
    }
    s.append(";\n");
  }

    // Declared in TypeCheck.jrag at line 24


  // 5.2
  public void typeCheck() {
    if(hasInit()) {
      TypeDecl source = getInit().type();
      TypeDecl dest = type();
      if(!source.assignConversionTo(dest, getInit()))
        error("can not assign " + name() + " of type " + dest.typeName() +
              " a value of type " + source.typeName());
    }
  }

    // Declared in ASTUtil.jrag at line 24


    public void refined_ASTUtil_makePrivate() {
        if(isPrivate())
            return;
        Modifiers m = getModifiers();
        for(int i=0;i<m.getNumModifier();++i) {
            String id = m.getModifier(i).getID();
            if(id.equals("protected") || id.equals("public")) {
                m.setModifier(new Modifier("private"), i);
                return;
            }
        }
        m.addModifier(new Modifier("private"));
    }

    // Declared in Encapsulate.jrag at line 3


	public void encapsulate() throws RefactoringException {
		Modifiers mod = getModifiers();
		String ucase_id = capitalize(getID());
		String getter_name = "get"+ucase_id;
		String setter_name = "set"+ucase_id;
		java.util.Set uses = uses();
		hostType().addMethod(makeGetter(getter_name, mod), false, false, false);
		hostType().addMethod(makeSetter(setter_name, mod), false, false, false);
		makePrivate();
		for(Iterator i = uses.iterator(); i.hasNext();) {
			VarAccess va = (VarAccess)i.next();
			va.encapsulate(getter_name, setter_name);
		}
	}

    // Declared in Encapsulate.jrag at line 18


	public static String capitalize(String str) {
		StringBuffer buf = new StringBuffer(str);
		if(buf.length() > 0)
			buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
		return buf.toString();
	}

    // Declared in Encapsulate.jrag at line 25


	private MethodDecl makeGetter(String getter_name, Modifiers mod) {
		Block getter_body = new Block();
		getter_body.addStmt(new ReturnStmt(new VarAccess(getID())));
		return new MethodDecl((Modifiers)mod.fullCopy(), (Access)getTypeAccess().fullCopy(),
				getter_name, new List(), new List(), 
				new List(), new Opt(getter_body));
	}

    // Declared in Encapsulate.jrag at line 33


	private MethodDecl makeSetter(String setter_name, Modifiers mod) {
		Access fieldacc = new ThisAccess("this").qualifiesAccess(new VarAccess(getID()));
		Access parmacc = new VarAccess(getID());
		Block setter_body = new Block();
		setter_body.addStmt(new ReturnStmt(new AssignSimpleExpr(fieldacc, parmacc)));
		ParameterDeclaration pd = new ParameterDeclaration((Access)getTypeAccess().fullCopy(), getID());
		List parms = new List();
		parms.add(pd);
		return new MethodDecl((Modifiers)mod.fullCopy(), (Access)getTypeAccess().fullCopy(),
				setter_name, parms, new List(), new List(), 
				new Opt(setter_body));
	}

    // Declared in Names.jadd at line 18

	public void refined_Names_changeID(String id) { setID(id); }

    // Declared in RenameField.jrag at line 7


	public void rename(String new_name) throws RefactoringException {
		if(getID().equals(new_name))
			// haha, very funny
			return;
		if(!hostType().localFields(new_name).isEmpty())
			throw new RefactoringException("couldn't rename: field name clash");
		AdjustmentTable table = find_uses(new_name);
		changeID(new_name);
		programRoot().clear();
		table.adjust();
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 76

    public FieldDeclaration() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(new Opt(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 76
    public FieldDeclaration(Modifiers p0, Access p1, String p2, Opt p3) {
        setChild(p0, 0);
        setChild(p1, 1);
        setID(p2);
        setChild(p3, 2);
    }

    // Declared in java.ast at line 20


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 23

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 76
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
    // Declared in java.ast line 76
    public void setTypeAccess(Access node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Access getTypeAccess() {
        return (Access)getChild(1);
    }

    // Declared in java.ast at line 9


    public Access getTypeAccessNoTransform() {
        return (Access)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 76
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
    // Declared in java.ast line 76
    public void setInitOpt(Opt opt) {
        setChild(opt, 2);
    }

    // Declared in java.ast at line 6


    public boolean hasInit() {
        return getInitOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Expr getInit() {
        return (Expr)getInitOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setInit(Expr node) {
        getInitOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getInitOpt() {
        return (Opt)getChild(2);
    }

    // Declared in java.ast at line 21


    public Opt getInitOptNoTransform() {
        return (Opt)getChildNoTransform(2);
    }

    // Declared in Undo.jadd at line 74

	
	  void makePrivate() {
		programRoot().pushUndo(new PrivatiseField(this));
		refined_ASTUtil_makePrivate();
	}

    // Declared in Undo.jadd at line 25

	  public void changeID(String id) {
		programRoot().pushUndo(new Rename(this, id));
		refined_Names_changeID(id);
	}

    // Declared in Liveness.jrag at line 3

	
	public boolean isLiveIn(Block blk) {
		if(blk.getNumStmt() == 0)
			return false;
		return isLiveBetween(blk.getStmt(0), blk.getStmt(blk.getNumStmt()-1));
	}

    // Declared in Uses.jrag at line 5

	
	public HashSet collectedUses() {
		return uses();
	}

    // Declared in Uses.jrag at line 93


	/* in preparation for renaming a variable to new_name, this method finds all
	 * uses of the variable before renaming and all uses of fields, types and
	 * packages that might become shadowed by the renaming and collects them into
	 * an adjustment table */ 
	public AdjustmentTable find_uses(String new_name) {
		AdjustmentTable table = new AdjustmentTable();
		/* first, collect all uses of the variable we are renaming */
		for(Iterator i = uses().iterator(); i.hasNext();) {
			VarAccess va = (VarAccess)i.next();
			table.add(va, this);
		}
		/* now, collect all uses of fields, types, and packages that the variable
		 * might be shadowing after renaming */
		for(Iterator i = lookupVariable(new_name).iterator(); i.hasNext();) {
			Variable v = (Variable)i.next();
			for(Iterator j = v.collectedUses().iterator(); j.hasNext();) {
				Access acc = (Access)j.next();
				table.add(acc, (ASTNode)v);
			}
		}
		for(Iterator i = lookupType(new_name).iterator(); i.hasNext();) {
			TypeDecl d = (TypeDecl)i.next();
			for(Iterator j = d.uses().iterator(); j.hasNext();) {
				Access acc = (Access)j.next();
				// only a type in an ambiguous position can be shadowed by a variable
				if(acc.nameType() == NameType.AMBIGUOUS_NAME)
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

    protected java.util.Map accessibleFrom_TypeDecl_values;
    // Declared in AccessControl.jrag at line 100
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
    if(isPublic())
      return true;
    else if(isProtected()) {
      if(hostPackage().equals(type.hostPackage()))
        return true;
      if(type.withinBodyThatSubclasses(hostType()) != null)
        return true;
      return false;
    }
    else if(isPrivate())
      return hostType().topLevelType() == type.topLevelType();
    else
      return hostPackage().equals(type.hostPackage());
  }

    protected boolean exceptions_computed = false;
    protected Collection exceptions_value;
    // Declared in AnonymousClasses.jrag at line 103
    public Collection exceptions() {
        if(exceptions_computed)
            return exceptions_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        exceptions_value = exceptions_compute();
        if(isFinal && num == boundariesCrossed)
            exceptions_computed = true;
        return exceptions_value;
    }

    private Collection exceptions_compute()  {
    HashSet set = new HashSet();
    if(isInstanceVariable() && hasInit()) {
      collectExceptions(set, this);
      for(Iterator iter = set.iterator(); iter.hasNext(); ) {
        TypeDecl typeDecl = (TypeDecl)iter.next();
        if(!getInit().reachedException(typeDecl))
          iter.remove();
      }
    }
    return set;
  }

    // Declared in ConstantExpression.jrag at line 454
    public boolean isConstant() {
        boolean isConstant_value = isConstant_compute();
        return isConstant_value;
    }

    private boolean isConstant_compute() {  return  isFinal() && hasInit() && getInit().isConstant() && (type().isPrimitive() || type().isString());  }

    // Declared in DataStructures.jrag at line 58
    public int size() {
        int size_value = size_compute();
        return size_value;
    }

    private int size_compute() {  return  1;  }

    // Declared in DataStructures.jrag at line 59
    public boolean isEmpty() {
        boolean isEmpty_value = isEmpty_compute();
        return isEmpty_value;
    }

    private boolean isEmpty_compute() {  return  false;  }

    // Declared in DataStructures.jrag at line 63
    public boolean contains(Object o) {
        boolean contains_Object_value = contains_compute(o);
        return contains_Object_value;
    }

    private boolean contains_compute(Object o) {  return  this == o;  }

    // Declared in DefiniteAssignment.jrag at line 305
    public boolean isDAafter(Variable v) {
        Object _parameters = v;
if(isDAafter_Variable_values == null) isDAafter_Variable_values = new java.util.HashMap(4);
        if(isDAafter_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAafter_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDAafter_Variable_values.put(_parameters, Boolean.valueOf(isDAafter_Variable_value));
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v)  {
    if(v == this)
      return hasInit();
    return hasInit() ? getInit().isDAafter(v) : isDAbefore(v);
  }

    // Declared in DefiniteAssignment.jrag at line 768
    public boolean isDUafter(Variable v) {
        Object _parameters = v;
if(isDUafter_Variable_values == null) isDUafter_Variable_values = new java.util.HashMap(4);
        if(isDUafter_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUafter_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDUafter_Variable_values.put(_parameters, Boolean.valueOf(isDUafter_Variable_value));
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v)  {
    if(v == this)
      return !hasInit();
    return hasInit() ? getInit().isDUafter(v) : isDUbefore(v);
  }

    // Declared in Modifiers.jrag at line 205
    public boolean isSynthetic() {
        boolean isSynthetic_value = isSynthetic_compute();
        return isSynthetic_value;
    }

    private boolean isSynthetic_compute() {  return  getModifiers().isSynthetic();  }

    // Declared in Modifiers.jrag at line 224
    public boolean isPublic() {
        boolean isPublic_value = isPublic_compute();
        return isPublic_value;
    }

    private boolean isPublic_compute() {  return  getModifiers().isPublic() || hostType().isInterfaceDecl();  }

    // Declared in Modifiers.jrag at line 225
    public boolean isPrivate() {
        boolean isPrivate_value = isPrivate_compute();
        return isPrivate_value;
    }

    private boolean isPrivate_compute() {  return  getModifiers().isPrivate();  }

    // Declared in Modifiers.jrag at line 226
    public boolean isProtected() {
        boolean isProtected_value = isProtected_compute();
        return isProtected_value;
    }

    private boolean isProtected_compute() {  return  getModifiers().isProtected();  }

    // Declared in Modifiers.jrag at line 227
    public boolean isStatic() {
        boolean isStatic_value = isStatic_compute();
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return  getModifiers().isStatic() || hostType().isInterfaceDecl();  }

    // Declared in Modifiers.jrag at line 229
    public boolean isFinal() {
        boolean isFinal_value = isFinal_compute();
        return isFinal_value;
    }

    private boolean isFinal_compute() {  return  getModifiers().isFinal() || hostType().isInterfaceDecl();  }

    // Declared in Modifiers.jrag at line 230
    public boolean isTransient() {
        boolean isTransient_value = isTransient_compute();
        return isTransient_value;
    }

    private boolean isTransient_compute() {  return  getModifiers().isTransient();  }

    // Declared in Modifiers.jrag at line 231
    public boolean isVolatile() {
        boolean isVolatile_value = isVolatile_compute();
        return isVolatile_value;
    }

    private boolean isVolatile_compute() {  return  getModifiers().isVolatile();  }

    // Declared in PrettyPrint.jadd at line 946
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in TypeAnalysis.jrag at line 243
    public TypeDecl type() {
        TypeDecl type_value = type_compute();
        return type_value;
    }

    private TypeDecl type_compute() {  return  getTypeAccess().type();  }

    // Declared in TypeAnalysis.jrag at line 275
    public boolean isVoid() {
        boolean isVoid_value = isVoid_compute();
        return isVoid_value;
    }

    private boolean isVoid_compute() {  return  type().isVoid();  }

    // Declared in VariableDeclaration.jrag at line 45
    public boolean isClassVariable() {
        boolean isClassVariable_value = isClassVariable_compute();
        return isClassVariable_value;
    }

    private boolean isClassVariable_compute() {  return  isStatic() || hostType().isInterfaceDecl();  }

    // Declared in VariableDeclaration.jrag at line 46
    public boolean isInstanceVariable() {
        boolean isInstanceVariable_value = isInstanceVariable_compute();
        return isInstanceVariable_value;
    }

    private boolean isInstanceVariable_compute() {  return  (hostType().isClassDecl() || hostType().isAnonymous() )&& !isStatic();  }

    // Declared in VariableDeclaration.jrag at line 47
    public boolean isMethodParameter() {
        boolean isMethodParameter_value = isMethodParameter_compute();
        return isMethodParameter_value;
    }

    private boolean isMethodParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 48
    public boolean isConstructorParameter() {
        boolean isConstructorParameter_value = isConstructorParameter_compute();
        return isConstructorParameter_value;
    }

    private boolean isConstructorParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 49
    public boolean isExceptionHandlerParameter() {
        boolean isExceptionHandlerParameter_value = isExceptionHandlerParameter_compute();
        return isExceptionHandlerParameter_value;
    }

    private boolean isExceptionHandlerParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 50
    public boolean isLocalVariable() {
        boolean isLocalVariable_value = isLocalVariable_compute();
        return isLocalVariable_value;
    }

    private boolean isLocalVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 52
    public boolean isBlank() {
        boolean isBlank_value = isBlank_compute();
        return isBlank_value;
    }

    private boolean isBlank_compute() {  return  !hasInit();  }

    // Declared in VariableDeclaration.jrag at line 54
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    protected boolean constant_computed = false;
    protected Constant constant_value;
    // Declared in VariableDeclaration.jrag at line 55
    public Constant constant() {
        if(constant_computed)
            return constant_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        constant_value = constant_compute();
        if(isFinal && num == boundariesCrossed)
            constant_computed = true;
        return constant_value;
    }

    private Constant constant_compute() {  return  type().cast(getInit().constant());  }

    protected java.util.Set isLiveBetween_Stmt_Stmt_visited;
    protected java.util.Set isLiveBetween_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set isLiveBetween_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map isLiveBetween_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean isLiveBetween(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isLiveBetween_Stmt_Stmt_visited == null) isLiveBetween_Stmt_Stmt_visited = new java.util.HashSet(4);
if(isLiveBetween_Stmt_Stmt_values == null) isLiveBetween_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isLiveBetween_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!isLiveBetween_Stmt_Stmt_initialized.contains(_parameters)) {
            isLiveBetween_Stmt_Stmt_initialized.add(_parameters);
            isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isLiveBetween_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_isLiveBetween_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_isLiveBetween_Stmt_Stmt_value = isLiveBetween_compute(begin, end);
                if (new_isLiveBetween_Stmt_Stmt_value!=((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveBetween_Stmt_Stmt_value));
            } while (CHANGE);
            isLiveBetween_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            isLiveBetween_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            isLiveBetween_compute(begin, end);
            RESET_CYCLE = false;
            isLiveBetween_Stmt_Stmt_computed.remove(_parameters);
            isLiveBetween_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_isLiveBetween_Stmt_Stmt_value;
        }
        if(!isLiveBetween_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                isLiveBetween_Stmt_Stmt_computed.remove(_parameters);
                isLiveBetween_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            isLiveBetween_Stmt_Stmt_visited.add(_parameters);
            boolean new_isLiveBetween_Stmt_Stmt_value = isLiveBetween_compute(begin, end);
            if (new_isLiveBetween_Stmt_Stmt_value!=((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            isLiveBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveBetween_Stmt_Stmt_value));
            isLiveBetween_Stmt_Stmt_visited.remove(_parameters);
            return new_isLiveBetween_Stmt_Stmt_value;
        }
        return ((Boolean)isLiveBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean isLiveBetween_compute(Stmt begin, Stmt end)  {
		if(begin.mayUse(this)) return true;
		if(begin.mayDef(this)) return false;
		if(begin == end) return false;
		for(Iterator i=begin.gsucc(begin, end).iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(isLiveBetween(next, end))
				return true;
		}
		return false;
	}

    protected java.util.Set isLiveAfter_Stmt_visited;
    protected java.util.Set isLiveAfter_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set isLiveAfter_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map isLiveAfter_Stmt_values = new java.util.HashMap(4);
    public boolean isLiveAfter(Stmt stmt) {
        Object _parameters = stmt;
if(isLiveAfter_Stmt_visited == null) isLiveAfter_Stmt_visited = new java.util.HashSet(4);
if(isLiveAfter_Stmt_values == null) isLiveAfter_Stmt_values = new java.util.HashMap(4);
        if(isLiveAfter_Stmt_computed.contains(_parameters))
            return ((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue();
        if (!isLiveAfter_Stmt_initialized.contains(_parameters)) {
            isLiveAfter_Stmt_initialized.add(_parameters);
            isLiveAfter_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isLiveAfter_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_isLiveAfter_Stmt_value;
            do {
                CHANGE = false;
                new_isLiveAfter_Stmt_value = isLiveAfter_compute(stmt);
                if (new_isLiveAfter_Stmt_value!=((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                isLiveAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveAfter_Stmt_value));
            } while (CHANGE);
            isLiveAfter_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            isLiveAfter_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            isLiveAfter_compute(stmt);
            RESET_CYCLE = false;
            isLiveAfter_Stmt_computed.remove(_parameters);
            isLiveAfter_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_isLiveAfter_Stmt_value;
        }
        if(!isLiveAfter_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                isLiveAfter_Stmt_computed.remove(_parameters);
                isLiveAfter_Stmt_initialized.remove(_parameters);
                return ((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue();
            }
            isLiveAfter_Stmt_visited.add(_parameters);
            boolean new_isLiveAfter_Stmt_value = isLiveAfter_compute(stmt);
            if (new_isLiveAfter_Stmt_value!=((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            isLiveAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveAfter_Stmt_value));
            isLiveAfter_Stmt_visited.remove(_parameters);
            return new_isLiveAfter_Stmt_value;
        }
        return ((Boolean)isLiveAfter_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean isLiveAfter_compute(Stmt stmt)  {
		Block host = stmt.hostBlock();
		for(Iterator i=stmt.following().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.between(host, -1, Integer.MAX_VALUE))
				continue;
			if(isLiveAtOrAfter(next))
				return true;
		}
		return false;
	}

    protected java.util.Set isLiveAtOrAfter_Stmt_visited;
    protected java.util.Set isLiveAtOrAfter_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set isLiveAtOrAfter_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map isLiveAtOrAfter_Stmt_values = new java.util.HashMap(4);
    public boolean isLiveAtOrAfter(Stmt stmt) {
        Object _parameters = stmt;
if(isLiveAtOrAfter_Stmt_visited == null) isLiveAtOrAfter_Stmt_visited = new java.util.HashSet(4);
if(isLiveAtOrAfter_Stmt_values == null) isLiveAtOrAfter_Stmt_values = new java.util.HashMap(4);
        if(isLiveAtOrAfter_Stmt_computed.contains(_parameters))
            return ((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue();
        if (!isLiveAtOrAfter_Stmt_initialized.contains(_parameters)) {
            isLiveAtOrAfter_Stmt_initialized.add(_parameters);
            isLiveAtOrAfter_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isLiveAtOrAfter_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_isLiveAtOrAfter_Stmt_value;
            do {
                CHANGE = false;
                new_isLiveAtOrAfter_Stmt_value = isLiveAtOrAfter_compute(stmt);
                if (new_isLiveAtOrAfter_Stmt_value!=((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                isLiveAtOrAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveAtOrAfter_Stmt_value));
            } while (CHANGE);
            isLiveAtOrAfter_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            isLiveAtOrAfter_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            isLiveAtOrAfter_compute(stmt);
            RESET_CYCLE = false;
            isLiveAtOrAfter_Stmt_computed.remove(_parameters);
            isLiveAtOrAfter_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_isLiveAtOrAfter_Stmt_value;
        }
        if(!isLiveAtOrAfter_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                isLiveAtOrAfter_Stmt_computed.remove(_parameters);
                isLiveAtOrAfter_Stmt_initialized.remove(_parameters);
                return ((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue();
            }
            isLiveAtOrAfter_Stmt_visited.add(_parameters);
            boolean new_isLiveAtOrAfter_Stmt_value = isLiveAtOrAfter_compute(stmt);
            if (new_isLiveAtOrAfter_Stmt_value!=((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            isLiveAtOrAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_isLiveAtOrAfter_Stmt_value));
            isLiveAtOrAfter_Stmt_visited.remove(_parameters);
            return new_isLiveAtOrAfter_Stmt_value;
        }
        return ((Boolean)isLiveAtOrAfter_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean isLiveAtOrAfter_compute(Stmt stmt)  {
		if(stmt.mayUse(this)) return true;
		if(stmt.mayDef(this)) return false;
		Block host = stmt.hostBlock();
		for(Iterator i=stmt.succ().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.between(host, -1, Integer.MAX_VALUE))
				continue;
			if(isLiveAtOrAfter(next))
				return true;
		}
		return false;
	}

    // Declared in ExceptionHandling.jrag at line 25
    public boolean handlesException(TypeDecl exceptionType) {
        boolean handlesException_TypeDecl_value = getParent().Define_boolean_handlesException(this, null, exceptionType);
        return handlesException_TypeDecl_value;
    }

    // Declared in Modifiers.jrag at line 249
    public boolean Define_boolean_mayBePrivate(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBePrivate(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 134
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getInitOptNoTransform()) {
    if(hostType().isAnonymous())
      return true;
    if(!exceptionType.isUncheckedException())
      return true;
    for(Iterator iter = hostType().constructors().iterator(); iter.hasNext(); ) {
      ConstructorDecl decl = (ConstructorDecl)iter.next();
      if(!decl.throwsException(exceptionType))
        return false;
    }
    return true;
  }
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in DefiniteAssignment.jrag at line 311
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getInitOptNoTransform()) {
    return isDAbefore(v);
  }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in Modifiers.jrag at line 251
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeFinal(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 587
    public BodyDecl Define_BodyDecl_hostBodyDecl(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  this;
        }
        return getParent().Define_BodyDecl_hostBodyDecl(this, caller);
    }

    // Declared in Modifiers.jrag at line 247
    public boolean Define_boolean_mayBePublic(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBePublic(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 28
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isSource(this, caller);
    }

    // Declared in Modifiers.jrag at line 253
    public boolean Define_boolean_mayBeVolatile(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeVolatile(this, caller);
    }

    // Declared in Modifiers.jrag at line 248
    public boolean Define_boolean_mayBeProtected(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeProtected(this, caller);
    }

    // Declared in Modifiers.jrag at line 252
    public boolean Define_boolean_mayBeTransient(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeTransient(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 257
    public TypeDecl Define_TypeDecl_declType(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  type();
        }
        return getParent().Define_TypeDecl_declType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 68
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getTypeAccessNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in TypeHierarchyCheck.jrag at line 132
    public boolean Define_boolean_inStaticContext(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  isStatic() || hostType().isInterfaceDecl();
        }
        return getParent().Define_boolean_inStaticContext(this, caller);
    }

    // Declared in Modifiers.jrag at line 250
    public boolean Define_boolean_mayBeStatic(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeStatic(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected boolean FieldDeclaration_uses_visited = false;
    protected boolean FieldDeclaration_uses_computed = false;
    protected HashSet FieldDeclaration_uses_value;
    // Declared in Uses.jrag at line 10
    public HashSet uses() {
        if(FieldDeclaration_uses_computed)
            return FieldDeclaration_uses_value;
        if(FieldDeclaration_uses_visited)
            throw new RuntimeException("Circular definition of attr: uses in class: ");
        FieldDeclaration_uses_visited = true;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        FieldDeclaration_uses_value = uses_compute();
        if(isFinal && num == boundariesCrossed)
            FieldDeclaration_uses_computed = true;
        FieldDeclaration_uses_visited = false;
        return FieldDeclaration_uses_value;
    }

    java.util.HashSet FieldDeclaration_uses_contributors = new java.util.HashSet();
    private HashSet uses_compute() {
        ASTNode node = this;
        while(node.getParent() != null)
            node = node.getParent();
        Program root = (Program)node;
        root.collect_contributors_FieldDeclaration_uses();
        FieldDeclaration_uses_value = new HashSet();
        for(java.util.Iterator iter = FieldDeclaration_uses_contributors.iterator(); iter.hasNext(); ) {
            ASTNode contributor = (ASTNode)iter.next();
            contributor.contributeTo_FieldDeclaration_FieldDeclaration_uses(FieldDeclaration_uses_value);
        }
        return FieldDeclaration_uses_value;
    }

}
