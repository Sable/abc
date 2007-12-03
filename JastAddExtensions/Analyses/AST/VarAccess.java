
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;


public class VarAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isConstant_computed = false;
        decls_computed = false;
        decls_value = null;
        decl_computed = false;
        decl_value = null;
        isFieldAccess_computed = false;
        type_computed = false;
        type_value = null;
        mayDef_Variable_values = null;
        mayUse_Variable_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        VarAccess node = (VarAccess)super.clone();
        node.isConstant_computed = false;
        node.decls_computed = false;
        node.decls_value = null;
        node.decl_computed = false;
        node.decl_value = null;
        node.isFieldAccess_computed = false;
        node.type_computed = false;
        node.type_value = null;
        node.mayDef_Variable_values = null;
        node.mayUse_Variable_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          VarAccess node = (VarAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        VarAccess res = (VarAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DefiniteAssignment.jrag at line 83

  
  public void definiteAssignment() {
    if(isSource()) {
      if(decl() instanceof VariableDeclaration) {
        VariableDeclaration v = (VariableDeclaration)decl();
        //System.err.println("Is " + v + " final? " + v.isFinal() + ", DAbefore: " + isDAbefore(v));
        if(v.isValue()) {
        }
        else if(v.isBlankFinal()) {
          //if(!isDAbefore(v) && !v.hasInit() && !v.getInit().isConstant())
          if(!isDAbefore(v))
            error("Final variable " + v.name() + " is not assigned before used");
        }
        else {
          //if(!v.hasInit() && !isDAbefore(v)) {
          if(!isDAbefore(v))
          error("Local variable " + v.name() + " in not assigned before used");
        }
      }
      
      else if(decl() instanceof FieldDeclaration && !isQualified()) {
        FieldDeclaration f = (FieldDeclaration)decl();
        //if(f.isFinal() && f.isInstanceVariable() && !isDAbefore(f)) {
        //if(f.isFinal() && !isDAbefore(f) && (!f.hasInit() || !f.getInit().isConstant())) {
        //if(f.isFinal() && (!f.hasInit() || !f.getInit().isConstant()) && !isDAbefore(f)) {
        if(f.isFinal() && !f.hasInit() && !isDAbefore(f)) {
          error("Final field " + f + " is not assigned before used");
        }
      }
      
    }
    if(isDest()) {
      Variable v = decl();
      // Blank final field
      if(v.isFinal() && v.isBlank() && !hostType().instanceOf(v.hostType()))
        error("The final variable is not a blank final in this context, so it may not be assigned.");
      else if(v.isFinal() && isQualified() && (!qualifier().isThisAccess() || ((Access)qualifier()).isQualified()))
        error("the blank final field " + v.name() + " may only be assigned by simple name");
      
      // local variable or parameter
      else if(v instanceof VariableDeclaration) {
        VariableDeclaration var = (VariableDeclaration)v;
        //System.out.println("### is variable");
        if(!var.isValue() && var.getParent().getParent().getParent() instanceof SwitchStmt && var.isFinal()) {
          if(!isDUbefore(var))
            error("Final variable " + var.name() + " may only be assigned once");
        }
        else if(var.isValue()) {
          if(var.hasInit() || !isDUbefore(var))
            error("Final variable " + var.name() + " may only be assigned once");
        }
        else if(var.isBlankFinal()) {
          if(var.hasInit() || !isDUbefore(var))
            error("Final variable " + var.name() + " may only be assigned once");
        }
        if(var.isFinal() && (var.hasInit() || !isDUbefore(var))) {
        //if(var.isFinal() && ((var.hasInit() && var.getInit().isConstant()) || !isDUbefore(var))) {
        }
      }
      // field
      else if(v instanceof FieldDeclaration) {
        FieldDeclaration f = (FieldDeclaration)v;
        if(f.isFinal()) {
          if(f.hasInit())
            error("initialized field " + f.name() + " can not be assigned");
          else {
            BodyDecl bodyDecl = enclosingBodyDecl();
            if(!(bodyDecl instanceof ConstructorDecl) && !(bodyDecl instanceof InstanceInitializer) && !(bodyDecl instanceof StaticInitializer) && !(bodyDecl instanceof FieldDeclaration))
              error("final field " + f.name() + " may only be assigned in constructors and initializers");
            else if(!isDUbefore(f))
              error("Final field " + f.name() + " may only be assigned once");
          }
        }
      }
      else if(v instanceof ParameterDeclaration) {
        ParameterDeclaration p = (ParameterDeclaration)v;

        // 8.4.1
        if(p.isFinal()) {
          error("Final parameter " + p.name() + " may not be assigned");
        }
      }
      
    }
  }

    // Declared in DefiniteAssignment.jrag at line 454


  protected boolean checkDUeverywhere(Variable v) {
    if(isDest() && decl() == v)
      return false;
    return super.checkDUeverywhere(v);
  }

    // Declared in NameCheck.jrag at line 165


  public void nameCheck() {
    if(decls().isEmpty() && (!isQualified() || !qualifier().type().isUnknown() || qualifier().isPackageAccess()))
      error("no field named " + name());
    if(decls().size() > 1) {
      StringBuffer s = new StringBuffer();
      s.append("several fields named " + name());
      for(Iterator iter = decls().iterator(); iter.hasNext(); ) {
        Variable v = (Variable)iter.next();
        s.append("\n    " + v.type().typeName() + "." + v.name() + " declared in " + v.hostType().typeName());
      }
      error(s.toString());
    }
      
    // 8.8.5.1
    if(inExplicitConstructorInvocation() && !isQualified() && decl().isInstanceVariable() && hostType() == decl().hostType())
      error("instance variable " + name() + " may not be accessed in an explicit constructor invocation");

    Variable v = decl();
    if(!v.isFinal() && !v.isClassVariable() && !v.isInstanceVariable() && v.hostType() != hostType())
      error("A parameter/variable used but not declared in an inner class must be declared final");

    // 8.3.2.3
    if((decl().isInstanceVariable() || decl().isClassVariable()) && !isQualified()) {
      if(hostType() != null && !hostType().declaredBeforeUse(decl(), this)) {
        if(inSameInitializer() && !simpleAssignment() && inDeclaringClass()) {
          BodyDecl b = closestBodyDecl(hostType());
          error("variable " + decl().name() + " is used in " + b + " before it is declared");
        }
      }
    }

  }

    // Declared in NameCheck.jrag at line 199


  // find the bodydecl declared in t in which this construct is nested
  public BodyDecl closestBodyDecl(TypeDecl t) {
    ASTNode node = this;
    while(!(node.getParent().getParent() instanceof Program) && node.getParent().getParent() != t) {
      node = node.getParent();
    }
    if(node instanceof BodyDecl)
      return (BodyDecl)node;
    return null;
  }

    // Declared in NodeConstructors.jrag at line 33

  public VarAccess(String name, int start, int end) {
    this(name);
    this.start = start;
    this.end = end;
  }

    // Declared in PrettyPrint.jadd at line 639


  public void toString(StringBuffer s) {
    s.append(name());
  }

    // Declared in Encapsulate.jrag at line 52


	public void encapsulate(java.util.List changes, String getter, String setter) {
		Context ctxt = new Context();
		ASTNode ch = this;
		for(ASTNode p=getParent();p!=null&&p instanceof Expr;ch=p,p=p.getParent()) {
			if(p instanceof AssignExpr) {
				Expr rhs = ((AssignExpr)p).getSource();
				Binary implicit_op = ((AssignExpr)p).getImplicitOperator();
				if(implicit_op == null) {
					List args = new List();	args.add(rhs);
					changes.add(new NodeReplace(p, 
							ctxt.plugIn(new MethodAccess(setter, args))));
				} else {
					/* TODO: this is dangerous; we copy a subtree in which there
					 *       might be pending adjustments */
					Context ctxt2 = ctxt.fullCopy();
					List args = new List();
					implicit_op.setLeftOperand((Expr)ctxt2.plugIn(new MethodAccess(getter, new List())));
					implicit_op.setRightOperand(rhs);
					args.add(implicit_op);
					changes.add(new NodeReplace(p,
							ctxt.plugIn(new MethodAccess(setter, args))));
				}
			} else if(p instanceof PostDecExpr) {
				// i-- becomes (setI(getI()-1) == 0 ? getI()+1 : getI()+1)
				encapsulate_postfix(changes, p, ctxt, getter, setter, 
						new SubExpr(), new AddExpr());
			} else if(p instanceof PostIncExpr) {
				encapsulate_postfix(changes, p, ctxt, getter, setter, 
						new AddExpr(), new SubExpr());
			} else if(p instanceof PreDecExpr) {
				// --i becomes setI(getI()-1)
				encapsulate_prefix(changes, p, ctxt, getter, setter, new SubExpr());
			} else if(p instanceof PreIncExpr) {
				encapsulate_prefix(changes, p, ctxt, getter, setter, new AddExpr());
			} else if(p instanceof ParExpr) {
				ctxt.wrapIn(p, 0);
				continue;
			} else if(p instanceof AbstractDot && ch == ((AbstractDot)p).getRight()) {
				ctxt.wrapIn(p, 1);
				continue;
			} else {
				changes.add(new NodeReplace(this, new MethodAccess(getter, new List())));
				return;
			}
		}
	}

    // Declared in Encapsulate.jrag at line 99

	
	static void encapsulate_postfix(java.util.List changes, ASTNode p, 
			Context ctxt, String getter, String setter, Binary rator, Binary undo) {
		List args = new List();
		Expr getacc = (Expr)ctxt.fullCopy().plugIn(new MethodAccess(getter, new List()));
		rator.setLeftOperand((Expr)getacc.fullCopy());
		rator.setRightOperand(new IntegerLiteral(1));
		args.add(rator);
		undo.setLeftOperand((Expr)getacc.fullCopy());
		undo.setRightOperand(new IntegerLiteral(1));
		Expr setacc = (Expr)ctxt.fullCopy().plugIn(new MethodAccess(setter, args));
		changes.add(new NodeReplace(p,
				new ParExpr(
						new ConditionalExpr(new EQExpr(setacc, new IntegerLiteral(0)),
								(Expr)undo.fullCopy(), (Expr)undo.fullCopy()))));
	}

    // Declared in Encapsulate.jrag at line 115

	
	static void encapsulate_prefix(java.util.List changes, ASTNode p,
			Context ctxt, String getter, String setter, Binary rator) {
		List args = new List();
		Expr getacc = (Expr)ctxt.fullCopy().plugIn(new MethodAccess(getter, new List()));
		rator.setLeftOperand(getacc);
		rator.setRightOperand(new IntegerLiteral(1));
		args.add(rator);
		changes.add(new NodeReplace(p, ctxt.plugIn(new MethodAccess(setter, args))));
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 16

    public VarAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 16
    public VarAccess(String p0) {
        setID(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 16
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in ConstantExpression.jrag at line 99
    public Constant constant() {
        Constant constant_value = constant_compute();
        return constant_value;
    }

    private Constant constant_compute() {  return  type().cast(decl().getInit().constant());  }

    protected boolean isConstant_visited = false;
    protected boolean isConstant_computed = false;
    protected boolean isConstant_initialized = false;
    protected boolean isConstant_value;
    public boolean isConstant() {
        if(isConstant_computed)
            return isConstant_value;
        if (!isConstant_initialized) {
            isConstant_initialized = true;
            isConstant_value = false;
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isConstant_visited = true;
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            do {
                CHANGE = false;
                boolean new_isConstant_value = isConstant_compute();
                if (new_isConstant_value!=isConstant_value)
                    CHANGE = true;
                isConstant_value = new_isConstant_value; 
            } while (CHANGE);
            isConstant_visited = false;
            if(isFinal && num == boundariesCrossed)
{
            isConstant_computed = true;
            }
            else {
            RESET_CYCLE = true;
            isConstant_compute();
            RESET_CYCLE = false;
              isConstant_computed = false;
              isConstant_initialized = false;
            }
            IN_CIRCLE = false; 
            return isConstant_value;
        }
        if(!isConstant_visited) {
            if (RESET_CYCLE) {
                isConstant_computed = false;
                isConstant_initialized = false;
                return isConstant_value;
            }
            isConstant_visited = true;
            boolean new_isConstant_value = isConstant_compute();
            if (new_isConstant_value!=isConstant_value)
                CHANGE = true;
            isConstant_value = new_isConstant_value; 
            isConstant_visited = false;
            return isConstant_value;
        }
        return isConstant_value;
    }

    private boolean isConstant_compute()  {
    Variable v = decl();
    if(v instanceof FieldDeclaration) {
      FieldDeclaration f = (FieldDeclaration)v;
      return f.isConstant() && (!isQualified() || (isQualified() && qualifier().isTypeAccess()));
    }
    boolean result = v.isFinal() && v.hasInit() && v.getInit().isConstant() && (v.type().isPrimitive() || v.type().isString());
    return result && (!isQualified() || (isQualified() && qualifier().isTypeAccess()));
  }

    // Declared in DefiniteAssignment.jrag at line 49
    public Variable varDecl() {
        Variable varDecl_value = varDecl_compute();
        return varDecl_value;
    }

    private Variable varDecl_compute() {  return  decl();  }

    // Declared in DefiniteAssignment.jrag at line 342
    public boolean isDAafter(Variable v) {
        boolean isDAafter_Variable_value = isDAafter_compute(v);
        return isDAafter_Variable_value;
    }

    private boolean isDAafter_compute(Variable v)  {
    return (isDest() && decl() == v) || isDAbefore(v);
  }

    // Declared in DefiniteAssignment.jrag at line 829
    public boolean isDUafter(Variable v) {
        boolean isDUafter_Variable_value = isDUafter_compute(v);
        return isDUafter_Variable_value;
    }

    private boolean isDUafter_compute(Variable v)  {
    if(isDest() && decl() == v)
      return false;
    return isDUbefore(v);
  }

    // Declared in DefiniteAssignment.jrag at line 1217
    public boolean unassignedEverywhere(Variable v, TryStmt stmt) {
        boolean unassignedEverywhere_Variable_TryStmt_value = unassignedEverywhere_compute(v, stmt);
        return unassignedEverywhere_Variable_TryStmt_value;
    }

    private boolean unassignedEverywhere_compute(Variable v, TryStmt stmt)  {
    if(isDest() && decl() == v && enclosingStmt().reachable()) {
      return false;
    }
    return super.unassignedEverywhere(v, stmt);
  }

    protected boolean decls_computed = false;
    protected SimpleSet decls_value;
    // Declared in LookupVariable.jrag at line 232
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
    SimpleSet set = lookupVariable(name());
    if(set.size() == 1) {
      Variable v = (Variable)set.iterator().next();
      if(!isQualified() && inStaticContext()) {
        if(v.isInstanceVariable() && !hostType().memberFields(v.name()).isEmpty())
          return SimpleSet.emptySet;
      }
      else if(isQualified() && qualifier().staticContextQualifier()) {
        if(v.isInstanceVariable())
          return SimpleSet.emptySet;
      }
    }
    return set;
  }

    protected boolean decl_computed = false;
    protected Variable decl_value;
    // Declared in LookupVariable.jrag at line 247
    public Variable decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private Variable decl_compute()  {
    SimpleSet decls = decls();
    if(decls.size() == 1)
      return (Variable)decls.iterator().next();
    return unknownField();
  }

    // Declared in NameCheck.jrag at line 209
    public boolean inSameInitializer() {
        boolean inSameInitializer_value = inSameInitializer_compute();
        return inSameInitializer_value;
    }

    private boolean inSameInitializer_compute()  {
    BodyDecl b = closestBodyDecl(decl().hostType());
    if(b == null) return false;
    if(b instanceof FieldDeclaration && ((FieldDeclaration)b).isStatic() == decl().isStatic())
      return true;
    if(b instanceof InstanceInitializer && !decl().isStatic())
      return true;
    if(b instanceof StaticInitializer && decl().isStatic())
      return true;
    return false;
  }

    // Declared in NameCheck.jrag at line 221
    public boolean simpleAssignment() {
        boolean simpleAssignment_value = simpleAssignment_compute();
        return simpleAssignment_value;
    }

    private boolean simpleAssignment_compute() {  return  isDest() && getParent() instanceof AssignSimpleExpr;  }

    // Declared in NameCheck.jrag at line 223
    public boolean inDeclaringClass() {
        boolean inDeclaringClass_value = inDeclaringClass_compute();
        return inDeclaringClass_value;
    }

    private boolean inDeclaringClass_compute() {  return  hostType() == decl().hostType();  }

    // Declared in PrettyPrint.jadd at line 937
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getID() + "]";  }

    // Declared in QualifiedNames.jrag at line 8
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    protected boolean isFieldAccess_computed = false;
    protected boolean isFieldAccess_value;
    // Declared in ResolveAmbiguousNames.jrag at line 14
    public boolean isFieldAccess() {
        if(isFieldAccess_computed)
            return isFieldAccess_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        isFieldAccess_value = isFieldAccess_compute();
        if(isFinal && num == boundariesCrossed)
            isFieldAccess_computed = true;
        return isFieldAccess_value;
    }

    private boolean isFieldAccess_compute() {  return  decl().isClassVariable() || decl().isInstanceVariable();  }

    // Declared in SyntacticClassification.jrag at line 101
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.AMBIGUOUS_NAME;  }

    // Declared in TypeAnalysis.jrag at line 285
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

    // Declared in TypeCheck.jrag at line 8
    public boolean isVariable() {
        boolean isVariable_value = isVariable_compute();
        return isVariable_value;
    }

    private boolean isVariable_compute() {  return  true;  }

    // Declared in VarDefUse.jrag at line 9
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

    private boolean mayDef_compute(Variable v) {  return  decl() == v && isDest();  }

    // Declared in VarDefUse.jrag at line 21
    public boolean mayUse(Variable v) {
        Object _parameters = v;
if(mayUse_Variable_values == null) mayUse_Variable_values = new java.util.HashMap(4);
        if(mayUse_Variable_values.containsKey(_parameters))
            return ((Boolean)mayUse_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayUse_Variable_value = mayUse_compute(v);
        if(isFinal && num == boundariesCrossed)
            mayUse_Variable_values.put(_parameters, Boolean.valueOf(mayUse_Variable_value));
        return mayUse_Variable_value;
    }

    private boolean mayUse_compute(Variable v) {  return  decl() == v && isSource();  }

    // Declared in TypeHierarchyCheck.jrag at line 113
    public boolean inExplicitConstructorInvocation() {
        boolean inExplicitConstructorInvocation_value = getParent().Define_boolean_inExplicitConstructorInvocation(this, null);
        return inExplicitConstructorInvocation_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected void collect_contributors_FieldDeclaration_uses() {
        // Declared in Uses.jrag at line 11
        if(decl() instanceof FieldDeclaration) {
        {
            FieldDeclaration ref = (FieldDeclaration)((FieldDeclaration)decl());
            if(ref != null)
                ref.FieldDeclaration_uses_contributors.add(this);
        }
        }
        super.collect_contributors_FieldDeclaration_uses();
    }
    protected void collect_contributors_ParameterDeclaration_uses() {
        // Declared in Uses.jrag at line 21
        if(decl() instanceof ParameterDeclaration) {
        {
            ParameterDeclaration ref = (ParameterDeclaration)((ParameterDeclaration)decl());
            if(ref != null)
                ref.ParameterDeclaration_uses_contributors.add(this);
        }
        }
        super.collect_contributors_ParameterDeclaration_uses();
    }
    protected void collect_contributors_VariableDeclaration_uses() {
        // Declared in Uses.jrag at line 16
        if(decl() instanceof VariableDeclaration) {
        {
            VariableDeclaration ref = (VariableDeclaration)((VariableDeclaration)decl());
            if(ref != null)
                ref.VariableDeclaration_uses_contributors.add(this);
        }
        }
        super.collect_contributors_VariableDeclaration_uses();
    }
    protected void contributeTo_VariableDeclaration_VariableDeclaration_uses(HashSet collection) {
        if(decl() instanceof VariableDeclaration)
            collection.add(this);
    }

    protected void contributeTo_FieldDeclaration_FieldDeclaration_uses(HashSet collection) {
        if(decl() instanceof FieldDeclaration)
            collection.add(this);
    }

    protected void contributeTo_ParameterDeclaration_ParameterDeclaration_uses(HashSet collection) {
        if(decl() instanceof ParameterDeclaration)
            collection.add(this);
    }

}
