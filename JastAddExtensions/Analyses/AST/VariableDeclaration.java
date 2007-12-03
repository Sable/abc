
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class VariableDeclaration extends Stmt implements Cloneable,  SimpleSet,  Iterator,  Variable,  LocalDeclaration {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        constant_computed = false;
        constant_value = null;
        getBlock_computed = false;
        getBlock_value = null;
        shouldMoveInto_Stmt_Stmt_values = null;
        mayDef_Variable_values = null;
        isLiveBetween_Stmt_Stmt_values = null;
        isLiveAfter_Stmt_values = null;
        isLiveAtOrAfter_Stmt_values = null;
        mayDefBetween_Stmt_Stmt_values = null;
        accessedOutside_Stmt_Stmt_values = null;
        accessedBefore_Stmt_values = null;
        accessedAfter_Stmt_values = null;
        isValueParmFor_Stmt_Stmt_values = null;
        isOutParmFor_Stmt_Stmt_values = null;
        shouldMoveOutOf_Stmt_Stmt_values = null;
        shouldDuplicate_Stmt_Stmt_values = null;
        VariableDeclaration_uses_visited = false;
        VariableDeclaration_uses_computed = false;
        VariableDeclaration_uses_value = null;
    VariableDeclaration_uses_contributors = new java.util.HashSet();
    }
    public Object clone() throws CloneNotSupportedException {
        VariableDeclaration node = (VariableDeclaration)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.constant_computed = false;
        node.constant_value = null;
        node.getBlock_computed = false;
        node.getBlock_value = null;
        node.shouldMoveInto_Stmt_Stmt_values = null;
        node.mayDef_Variable_values = null;
        node.isLiveBetween_Stmt_Stmt_values = null;
        node.isLiveAfter_Stmt_values = null;
        node.isLiveAtOrAfter_Stmt_values = null;
        node.mayDefBetween_Stmt_Stmt_values = null;
        node.accessedOutside_Stmt_Stmt_values = null;
        node.accessedBefore_Stmt_values = null;
        node.accessedAfter_Stmt_values = null;
        node.isValueParmFor_Stmt_Stmt_values = null;
        node.isOutParmFor_Stmt_Stmt_values = null;
        node.shouldMoveOutOf_Stmt_Stmt_values = null;
        node.shouldDuplicate_Stmt_Stmt_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          VariableDeclaration node = (VariableDeclaration)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        VariableDeclaration res = (VariableDeclaration)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DataStructures.jrag at line 76

  public SimpleSet add(Object o) {
    return new SimpleSetImpl().add(this).add(o);
  }

    // Declared in DataStructures.jrag at line 82

  private VariableDeclaration iterElem;

    // Declared in DataStructures.jrag at line 83

  public Iterator iterator() { iterElem = this; return this; }

    // Declared in DataStructures.jrag at line 84

  public boolean hasNext() { return iterElem != null; }

    // Declared in DataStructures.jrag at line 85

  public Object next() { Object o = iterElem; iterElem = null; return o; }

    // Declared in DataStructures.jrag at line 86

  public void remove() { throw new UnsupportedOperationException(); }

    // Declared in NameCheck.jrag at line 295

  
  public void nameCheck() {
    SimpleSet decls = outerScope().lookupVariable(name());
    for(Iterator iter = decls.iterator(); iter.hasNext(); ) {
      Variable var = (Variable)iter.next();
      if(var instanceof VariableDeclaration) {
        VariableDeclaration decl = (VariableDeclaration)var;
        if(decl != this && decl.enclosingBodyDecl() == enclosingBodyDecl())
  	      error("duplicate declaration of local variable " + name() + " in enclosing scope");
      }
      // 8.4.1
      else if(var instanceof ParameterDeclaration) {
        ParameterDeclaration decl = (ParameterDeclaration)var;
	      if(decl.enclosingBodyDecl() == enclosingBodyDecl())
  	      error("duplicate declaration of local variable and parameter " + name());
      }
    }
    if(getParent().getParent() instanceof Block) {
      Block block = (Block)getParent().getParent();
      for(int i = 0; i < block.getNumStmt(); i++) {
        if(block.getStmt(i) instanceof Variable) {
          Variable v = (Variable)block.getStmt(i);
          if(v.name().equals(name()) && v != this) {
     	    error("duplicate declaration of local variable " + name());
          }
	}
      }
    }
  }

    // Declared in NodeConstructors.jrag at line 69


  public VariableDeclaration(Access type, String name, Expr init) {
    this(new Modifiers(new List()), type, name, new Opt(init));
  }

    // Declared in NodeConstructors.jrag at line 73


  public VariableDeclaration(Access type, String name) {
    this(new Modifiers(new List()), type, name, new Opt());
  }

    // Declared in PrettyPrint.jadd at line 885


  public void toString(StringBuffer s) {
    super.toString(s);
    getModifiers().toString(s);
    getTypeAccess().toString(s);
    s.append(" " + name());
    if(hasInit()) {
      s.append(" = ");
      getInit().toString(s);
    }
    s.append(";\n");
  }

    // Declared in TypeCheck.jrag at line 13

 
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

    // Declared in RenameLocalVariable.jrag at line 15

	
	// checks that the renaming doesn't lead to a name clash
	private RefactoringException canRenameTo(String new_name) {
		BodyDecl bd = hostBodyDecl();
		if(bd instanceof Methodoid) {
			Methodoid m = (Methodoid)bd;
			if(!m.parameterDeclaration(new_name).isEmpty())
				return new RefactoringException("parameter of the same name exists");
			if(m.hasBody())
				return m.getBlock().acceptLocal(new_name);
		} else if(bd instanceof Initializer) {
			return ((Initializer)bd).getBlock().acceptLocal(new_name);
		} else {
			assert(false);
		}
		return null;
	}

    // Declared in RenameLocalVariable.jrag at line 31

	
	public java.util.List rename(String new_name) throws RefactoringException {
		java.util.List changes = new java.util.Vector();
		if(getID().equals(new_name))
			return changes;
		RefactoringException e = canRenameTo(new_name);
		if(e != null) throw e;
		String old_name = getID();
		AdjustmentTable table = find_uses(new_name);
		setID(new_name);
		changes.add(new LocalVariableRename(this, new_name));
		programRoot().clear();
		try {
			table.adjust(changes);
		} finally {
			setID(old_name);
			programRoot().clear();
		}
		return changes;
	}

    // Declared in LocalDeclaration.jrag at line 20

	
	public ParameterDeclaration asParameterDeclaration() {
		return new ParameterDeclaration((Access)getTypeAccess().fullCopy(), getID());
	}

    // Declared in LocalDeclaration.jrag at line 28

	
	public VariableDeclaration asVariableDeclaration() {
		return (VariableDeclaration)fullCopy();
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 79

    public VariableDeclaration() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(new Opt(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 79
    public VariableDeclaration(Modifiers p0, Access p1, String p2, Opt p3) {
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
    // Declared in java.ast line 79
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
    // Declared in java.ast line 79
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
    // Declared in java.ast line 79
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
    // Declared in java.ast line 79
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

    // Declared in DataStructures.jrag at line 74
    public int size() {
        int size_value = size_compute();
        return size_value;
    }

    private int size_compute() {  return  1;  }

    // Declared in DataStructures.jrag at line 75
    public boolean isEmpty() {
        boolean isEmpty_value = isEmpty_compute();
        return isEmpty_value;
    }

    private boolean isEmpty_compute() {  return  false;  }

    // Declared in DataStructures.jrag at line 79
    public boolean contains(Object o) {
        boolean contains_Object_value = contains_compute(o);
        return contains_Object_value;
    }

    private boolean contains_compute(Object o) {  return  this == o;  }

    // Declared in DefiniteAssignment.jrag at line 80
    public boolean isBlankFinal() {
        boolean isBlankFinal_value = isBlankFinal_compute();
        return isBlankFinal_value;
    }

    private boolean isBlankFinal_compute() {  return  isFinal() && (!hasInit() || !getInit().isConstant());  }

    // Declared in DefiniteAssignment.jrag at line 81
    public boolean isValue() {
        boolean isValue_value = isValue_compute();
        return isValue_value;
    }

    private boolean isValue_compute() {  return  isFinal() && hasInit() && getInit().isConstant();  }

    // Declared in DefiniteAssignment.jrag at line 489
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

    // Declared in DefiniteAssignment.jrag at line 881
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

    // Declared in LookupVariable.jrag at line 131
    public boolean declaresVariable(String name) {
        boolean declaresVariable_String_value = declaresVariable_compute(name);
        return declaresVariable_String_value;
    }

    private boolean declaresVariable_compute(String name) {  return  name().equals(name);  }

    // Declared in PrettyPrint.jadd at line 947
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in TypeAnalysis.jrag at line 244
    public TypeDecl type() {
        TypeDecl type_value = type_compute();
        return type_value;
    }

    private TypeDecl type_compute() {  return  getTypeAccess().type();  }

    // Declared in VariableDeclaration.jrag at line 28
    public boolean isClassVariable() {
        boolean isClassVariable_value = isClassVariable_compute();
        return isClassVariable_value;
    }

    private boolean isClassVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 29
    public boolean isInstanceVariable() {
        boolean isInstanceVariable_value = isInstanceVariable_compute();
        return isInstanceVariable_value;
    }

    private boolean isInstanceVariable_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 30
    public boolean isMethodParameter() {
        boolean isMethodParameter_value = isMethodParameter_compute();
        return isMethodParameter_value;
    }

    private boolean isMethodParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 31
    public boolean isConstructorParameter() {
        boolean isConstructorParameter_value = isConstructorParameter_compute();
        return isConstructorParameter_value;
    }

    private boolean isConstructorParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 32
    public boolean isExceptionHandlerParameter() {
        boolean isExceptionHandlerParameter_value = isExceptionHandlerParameter_compute();
        return isExceptionHandlerParameter_value;
    }

    private boolean isExceptionHandlerParameter_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 33
    public boolean isLocalVariable() {
        boolean isLocalVariable_value = isLocalVariable_compute();
        return isLocalVariable_value;
    }

    private boolean isLocalVariable_compute() {  return  true;  }

    // Declared in VariableDeclaration.jrag at line 35
    public boolean isFinal() {
        boolean isFinal_value = isFinal_compute();
        return isFinal_value;
    }

    private boolean isFinal_compute() {  return  getModifiers().isFinal();  }

    // Declared in VariableDeclaration.jrag at line 36
    public boolean isBlank() {
        boolean isBlank_value = isBlank_compute();
        return isBlank_value;
    }

    private boolean isBlank_compute() {  return  !hasInit();  }

    // Declared in VariableDeclaration.jrag at line 37
    public boolean isStatic() {
        boolean isStatic_value = isStatic_compute();
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return  false;  }

    // Declared in VariableDeclaration.jrag at line 39
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    protected boolean constant_computed = false;
    protected Constant constant_value;
    // Declared in VariableDeclaration.jrag at line 41
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

    // Declared in LocalVarNesting.jrag at line 56
    public RefactoringException acceptLocal(String name) {
        RefactoringException acceptLocal_String_value = acceptLocal_compute(name);
        return acceptLocal_String_value;
    }

    private RefactoringException acceptLocal_compute(String name)  {
		if(name.equals(getID()))
			return new RefactoringException("local variable of same name exists");
		return null;
	}

    protected boolean getBlock_computed = false;
    protected Block getBlock_value;
    // Declared in Domination.jrag at line 58
    public Block getBlock() {
        if(getBlock_computed)
            return getBlock_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getBlock_value = getBlock_compute();
        if(isFinal && num == boundariesCrossed)
            getBlock_computed = true;
        return getBlock_value;
    }

    private Block getBlock_compute() {  return  hostBlock();  }

    protected java.util.Map shouldMoveInto_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 16
    public boolean shouldMoveInto(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldMoveInto_Stmt_Stmt_values == null) shouldMoveInto_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldMoveInto_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldMoveInto_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldMoveInto_Stmt_Stmt_value = shouldMoveInto_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldMoveInto_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldMoveInto_Stmt_Stmt_value));
        return shouldMoveInto_Stmt_Stmt_value;
    }

    private boolean shouldMoveInto_compute(Stmt begin, Stmt end) {  return 
		!between(begin, end) && !isValueParmFor(begin, end);  }

    // Declared in VarDefUse.jrag at line 11
    public boolean mayDef(Variable v) {
        Object _parameters = v;
if(mayDef_Variable_values == null) mayDef_Variable_values = new java.util.HashMap(4);
        if(mayDef_Variable_values.containsKey(_parameters))
            return ((Boolean)mayDef_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayDef_Variable_value = mayDef_compute(v);
        if(isFinal && num == boundariesCrossed)
            mayDef_Variable_values.put(_parameters, Boolean.valueOf(mayDef_Variable_value));
        return mayDef_Variable_value;
    }

    private boolean mayDef_compute(Variable v)  {
		return hasInit() && (this == v || getInit().mayDef(v));
	}

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

    protected java.util.Set mayDefBetween_Stmt_Stmt_visited;
    protected java.util.Set mayDefBetween_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set mayDefBetween_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map mayDefBetween_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean mayDefBetween(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(mayDefBetween_Stmt_Stmt_visited == null) mayDefBetween_Stmt_Stmt_visited = new java.util.HashSet(4);
if(mayDefBetween_Stmt_Stmt_values == null) mayDefBetween_Stmt_Stmt_values = new java.util.HashMap(4);
        if(mayDefBetween_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!mayDefBetween_Stmt_Stmt_initialized.contains(_parameters)) {
            mayDefBetween_Stmt_Stmt_initialized.add(_parameters);
            mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            mayDefBetween_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_mayDefBetween_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_mayDefBetween_Stmt_Stmt_value = mayDefBetween_compute(begin, end);
                if (new_mayDefBetween_Stmt_Stmt_value!=((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_mayDefBetween_Stmt_Stmt_value));
            } while (CHANGE);
            mayDefBetween_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            mayDefBetween_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            mayDefBetween_compute(begin, end);
            RESET_CYCLE = false;
            mayDefBetween_Stmt_Stmt_computed.remove(_parameters);
            mayDefBetween_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_mayDefBetween_Stmt_Stmt_value;
        }
        if(!mayDefBetween_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                mayDefBetween_Stmt_Stmt_computed.remove(_parameters);
                mayDefBetween_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            mayDefBetween_Stmt_Stmt_visited.add(_parameters);
            boolean new_mayDefBetween_Stmt_Stmt_value = mayDefBetween_compute(begin, end);
            if (new_mayDefBetween_Stmt_Stmt_value!=((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            mayDefBetween_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_mayDefBetween_Stmt_Stmt_value));
            mayDefBetween_Stmt_Stmt_visited.remove(_parameters);
            return new_mayDefBetween_Stmt_Stmt_value;
        }
        return ((Boolean)mayDefBetween_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean mayDefBetween_compute(Stmt begin, Stmt end)  {
		if(begin.mayDef(this)) return true;
		if(begin == end) return false;
		for(Iterator i=begin.gsucc(begin, end).iterator();i.hasNext();)
			if(mayDefBetween((Stmt)i.next(), end))
				return true;
		return false;
	}

    protected java.util.Map accessedOutside_Stmt_Stmt_values;
    // Declared in Liveness.jrag at line 50
    public boolean accessedOutside(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(accessedOutside_Stmt_Stmt_values == null) accessedOutside_Stmt_Stmt_values = new java.util.HashMap(4);
        if(accessedOutside_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)accessedOutside_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessedOutside_Stmt_Stmt_value = accessedOutside_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            accessedOutside_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(accessedOutside_Stmt_Stmt_value));
        return accessedOutside_Stmt_Stmt_value;
    }

    private boolean accessedOutside_compute(Stmt begin, Stmt end) {  return  
		accessedBefore(begin) || accessedAfter(end);  }

    protected java.util.Set accessedBefore_Stmt_visited;
    protected java.util.Set accessedBefore_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set accessedBefore_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map accessedBefore_Stmt_values = new java.util.HashMap(4);
    public boolean accessedBefore(Stmt stmt) {
        Object _parameters = stmt;
if(accessedBefore_Stmt_visited == null) accessedBefore_Stmt_visited = new java.util.HashSet(4);
if(accessedBefore_Stmt_values == null) accessedBefore_Stmt_values = new java.util.HashMap(4);
        if(accessedBefore_Stmt_computed.contains(_parameters))
            return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
        if (!accessedBefore_Stmt_initialized.contains(_parameters)) {
            accessedBefore_Stmt_initialized.add(_parameters);
            accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            accessedBefore_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_accessedBefore_Stmt_value;
            do {
                CHANGE = false;
                new_accessedBefore_Stmt_value = accessedBefore_compute(stmt);
                if (new_accessedBefore_Stmt_value!=((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedBefore_Stmt_value));
            } while (CHANGE);
            accessedBefore_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            accessedBefore_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            accessedBefore_compute(stmt);
            RESET_CYCLE = false;
            accessedBefore_Stmt_computed.remove(_parameters);
            accessedBefore_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_accessedBefore_Stmt_value;
        }
        if(!accessedBefore_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                accessedBefore_Stmt_computed.remove(_parameters);
                accessedBefore_Stmt_initialized.remove(_parameters);
                return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
            }
            accessedBefore_Stmt_visited.add(_parameters);
            boolean new_accessedBefore_Stmt_value = accessedBefore_compute(stmt);
            if (new_accessedBefore_Stmt_value!=((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            accessedBefore_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedBefore_Stmt_value));
            accessedBefore_Stmt_visited.remove(_parameters);
            return new_accessedBefore_Stmt_value;
        }
        return ((Boolean)accessedBefore_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean accessedBefore_compute(Stmt stmt)  {
		for(Iterator i=stmt.pred().iterator();i.hasNext();) {
			Stmt before = (Stmt)i.next();
			if(!before.between(getBlock(), -1, Integer.MAX_VALUE))
				continue;
			if(before.mayAccess(this))
				return true;
			if(accessedBefore(before))
				return true;
		}
		return false;
	}

    protected java.util.Set accessedAfter_Stmt_visited;
    protected java.util.Set accessedAfter_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set accessedAfter_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map accessedAfter_Stmt_values = new java.util.HashMap(4);
    public boolean accessedAfter(Stmt stmt) {
        Object _parameters = stmt;
if(accessedAfter_Stmt_visited == null) accessedAfter_Stmt_visited = new java.util.HashSet(4);
if(accessedAfter_Stmt_values == null) accessedAfter_Stmt_values = new java.util.HashMap(4);
        if(accessedAfter_Stmt_computed.contains(_parameters))
            return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
        if (!accessedAfter_Stmt_initialized.contains(_parameters)) {
            accessedAfter_Stmt_initialized.add(_parameters);
            accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            accessedAfter_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_accessedAfter_Stmt_value;
            do {
                CHANGE = false;
                new_accessedAfter_Stmt_value = accessedAfter_compute(stmt);
                if (new_accessedAfter_Stmt_value!=((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedAfter_Stmt_value));
            } while (CHANGE);
            accessedAfter_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            accessedAfter_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            accessedAfter_compute(stmt);
            RESET_CYCLE = false;
            accessedAfter_Stmt_computed.remove(_parameters);
            accessedAfter_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_accessedAfter_Stmt_value;
        }
        if(!accessedAfter_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                accessedAfter_Stmt_computed.remove(_parameters);
                accessedAfter_Stmt_initialized.remove(_parameters);
                return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
            }
            accessedAfter_Stmt_visited.add(_parameters);
            boolean new_accessedAfter_Stmt_value = accessedAfter_compute(stmt);
            if (new_accessedAfter_Stmt_value!=((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            accessedAfter_Stmt_values.put(_parameters, Boolean.valueOf(new_accessedAfter_Stmt_value));
            accessedAfter_Stmt_visited.remove(_parameters);
            return new_accessedAfter_Stmt_value;
        }
        return ((Boolean)accessedAfter_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean accessedAfter_compute(Stmt stmt)  {
		for(Iterator i=stmt.succ().iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.between(getBlock(), -1, Integer.MAX_VALUE))
				continue;
			if(next.mayAccess(this))
				return true;
			if(accessedAfter(next))
				return true;
		}
		return false;
	}

    protected java.util.Map isValueParmFor_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 3
    public boolean isValueParmFor(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isValueParmFor_Stmt_Stmt_values == null) isValueParmFor_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isValueParmFor_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)isValueParmFor_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isValueParmFor_Stmt_Stmt_value = isValueParmFor_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            isValueParmFor_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(isValueParmFor_Stmt_Stmt_value));
        return isValueParmFor_Stmt_Stmt_value;
    }

    private boolean isValueParmFor_compute(Stmt begin, Stmt end)  {
		return isLiveBetween(begin, end);
	}

    protected java.util.Map isOutParmFor_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 7
    public boolean isOutParmFor(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(isOutParmFor_Stmt_Stmt_values == null) isOutParmFor_Stmt_Stmt_values = new java.util.HashMap(4);
        if(isOutParmFor_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)isOutParmFor_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isOutParmFor_Stmt_Stmt_value = isOutParmFor_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            isOutParmFor_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(isOutParmFor_Stmt_Stmt_value));
        return isOutParmFor_Stmt_Stmt_value;
    }

    private boolean isOutParmFor_compute(Stmt begin, Stmt end)  {
		return isLiveAfter(end) && mayDefBetween(begin, end);
	}

    protected java.util.Map shouldMoveOutOf_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 11
    public boolean shouldMoveOutOf(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldMoveOutOf_Stmt_Stmt_values == null) shouldMoveOutOf_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldMoveOutOf_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldMoveOutOf_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldMoveOutOf_Stmt_Stmt_value = shouldMoveOutOf_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldMoveOutOf_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldMoveOutOf_Stmt_Stmt_value));
        return shouldMoveOutOf_Stmt_Stmt_value;
    }

    private boolean shouldMoveOutOf_compute(Stmt begin, Stmt end) {  return 
		between(begin, end)	&& accessedAfter(end);  }

    protected java.util.Map shouldDuplicate_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 21
    public boolean shouldDuplicate(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(shouldDuplicate_Stmt_Stmt_values == null) shouldDuplicate_Stmt_Stmt_values = new java.util.HashMap(4);
        if(shouldDuplicate_Stmt_Stmt_values.containsKey(_parameters))
            return ((Boolean)shouldDuplicate_Stmt_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean shouldDuplicate_Stmt_Stmt_value = shouldDuplicate_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            shouldDuplicate_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(shouldDuplicate_Stmt_Stmt_value));
        return shouldDuplicate_Stmt_Stmt_value;
    }

    private boolean shouldDuplicate_compute(Stmt begin, Stmt end)  {
		return (shouldMoveInto(begin, end) || between(begin, end))
					&& accessedOutside(begin, end);
	}

    // Declared in LookupVariable.jrag at line 12
    public SimpleSet lookupVariable(String name) {
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        return lookupVariable_String_value;
    }

    // Declared in NameCheck.jrag at line 277
    public VariableScope outerScope() {
        VariableScope outerScope_value = getParent().Define_VariableScope_outerScope(this, null);
        return outerScope_value;
    }

    // Declared in NameCheck.jrag at line 289
    public BodyDecl enclosingBodyDecl() {
        BodyDecl enclosingBodyDecl_value = getParent().Define_BodyDecl_enclosingBodyDecl(this, null);
        return enclosingBodyDecl_value;
    }

    // Declared in TypeAnalysis.jrag at line 577
    public TypeDecl hostType() {
        TypeDecl hostType_value = getParent().Define_TypeDecl_hostType(this, null);
        return hostType_value;
    }

    // Declared in ASTUtil.jrag at line 10
    public Program programRoot() {
        Program programRoot_value = getParent().Define_Program_programRoot(this, null);
        return programRoot_value;
    }

    // Declared in DefiniteAssignment.jrag at line 29
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isSource(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 258
    public TypeDecl Define_TypeDecl_declType(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  type();
        }
        return getParent().Define_TypeDecl_declType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 75
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getTypeAccessNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 494
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getInitOptNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in Modifiers.jrag at line 271
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        if(caller == getModifiersNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_mayBeFinal(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 886
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getInitOptNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected boolean VariableDeclaration_uses_visited = false;
    protected boolean VariableDeclaration_uses_computed = false;
    protected HashSet VariableDeclaration_uses_value;
    // Declared in Uses.jrag at line 15
    public HashSet uses() {
        if(VariableDeclaration_uses_computed)
            return VariableDeclaration_uses_value;
        if(VariableDeclaration_uses_visited)
            throw new RuntimeException("Circular definition of attr: uses in class: ");
        VariableDeclaration_uses_visited = true;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        VariableDeclaration_uses_value = uses_compute();
        if(isFinal && num == boundariesCrossed)
            VariableDeclaration_uses_computed = true;
        VariableDeclaration_uses_visited = false;
        return VariableDeclaration_uses_value;
    }

    java.util.HashSet VariableDeclaration_uses_contributors = new java.util.HashSet();
    private HashSet uses_compute() {
        ASTNode node = this;
        while(node.getParent() != null)
            node = node.getParent();
        Program root = (Program)node;
        root.collect_contributors_VariableDeclaration_uses();
        VariableDeclaration_uses_value = new HashSet();
        for(java.util.Iterator iter = VariableDeclaration_uses_contributors.iterator(); iter.hasNext(); ) {
            ASTNode contributor = (ASTNode)iter.next();
            contributor.contributeTo_VariableDeclaration_VariableDeclaration_uses(VariableDeclaration_uses_value);
        }
        return VariableDeclaration_uses_value;
    }

}
