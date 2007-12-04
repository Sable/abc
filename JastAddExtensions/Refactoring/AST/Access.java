
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


public abstract class Access extends Expr implements Cloneable {
    public void flushCache() {
        super.flushCache();
        prevExpr_computed = false;
        prevExpr_value = null;
        hasPrevExpr_computed = false;
        type_computed = false;
        type_value = null;
        accessField_FieldDeclaration_values = null;
        accessMethod_MethodDecl_List_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        Access node = (Access)super.clone();
        node.prevExpr_computed = false;
        node.prevExpr_value = null;
        node.hasPrevExpr_computed = false;
        node.type_computed = false;
        node.type_value = null;
        node.accessField_FieldDeclaration_values = null;
        node.accessMethod_MethodDecl_List_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in AccessLocalVariable.jrag at line 9


	public Access accessLocalVariable(VariableDeclaration vd) {
		SimpleSet res = lookupVariable(vd.getID());
		if(!res.contains(vd))
			return null;
		return new VarAccess(vd.getID());
	}

    // Declared in AccessLocalVariable.jrag at line 16

	
	public Access accessParameter(ParameterDeclaration pd) {
		SimpleSet res = lookupVariable(pd.getID());
		if(!res.contains(pd))
			return null;
		return new VarAccess(pd.getID());
	}

    // Declared in AdjustAccess.jrag at line 58

	
	// more needed here...
	
	public void adjust(java.util.List changes, AdjustmentTable table) throws RefactoringException {
		if(isQualified())
			table.adjust(changes, qualifier());
		ASTNode target = table.getTarget(this);
		if(target == null) return;
		ASTNode oldacc = null;
		Access newacc = null;
        FileRange pos = new FileRange(getStart(), getEnd());
		if(target instanceof VariableDeclaration) {
			if(((VarAccess)this).decl() != target) {
				newacc = this.accessLocalVariable((VariableDeclaration)target);
				if(newacc == null)
					throw new RefactoringException("local variable would become shadowed at "+pos);
				oldacc = this;
			}
		} else if(target instanceof ParameterDeclaration) {
			if(((VarAccess)this).decl() != target) {
				newacc = this.accessParameter((ParameterDeclaration)target);
				if(newacc == null)
					throw new RefactoringException("parameter would become shadowed at "+pos);
				oldacc = this;
			}
		} else if(target instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration)target;
			if(((VarAccess)this).decl() != fd) {
				newacc = this.accessField(fd);
				if(newacc == null)
					throw new RefactoringException("couldn't consistently rename field access at "+pos);
				if(this.isQualified()) {
					newacc = this.qualifier().mergeWithAccess(newacc);
					if(newacc == null)
						throw new RefactoringException("couldn't consistently rename field access at "+pos);
					oldacc = this.getParent();
				} else {
					oldacc = this;
				}
			}
		} else if(target instanceof MethodDecl) {
			MethodDecl md = (MethodDecl)target;
			RefactoringException exc = new RefactoringException("couldn't consistently rename method access at "+pos);
			if(((MethodAccess)this).decl() != md) {
				newacc = this.accessMethod(md, (List)((MethodAccess)this).getArgList().fullCopy());
				if(newacc == null)
					throw exc;
				if(this.isQualified()) {
					if(newacc instanceof AbstractDot && !md.isStatic())
						throw exc;
					newacc = this.qualifier().mergeWithAccess(newacc);
					if(newacc == null)
						throw exc;
					oldacc = this.getParent();
				} else {
					oldacc = this;
				}
			}
		} else if(target instanceof TypeDecl) {
			TypeAccess tacc = (TypeAccess)this;
            if(tacc.decl() != target) {
                boolean ambiguous = tacc.nameType() == NameType.AMBIGUOUS_NAME;
                newacc = this.accessType((TypeDecl)target, ambiguous);
                if(newacc == null)
                    throw new RefactoringException("couldn't consistently rename type access at "+pos);
                if(this.isQualified()) {
                    newacc = this.qualifier().mergeWithAccess(newacc);
                    if(newacc == null)
                        throw new RefactoringException("couldn't consistently rename field access at "+pos);
                    oldacc = parent;
                } else {
                    oldacc = this;
                }
            } else {
                ASTNode parent = tacc.getParent();
                int idx = parent.getIndexOfChild(tacc);
                ParseName pn = new ParseName(tacc.getID());
                parent.setChild(pn, idx);
                try {
                	Access tmp = (Access)parent.getChild(idx);
                	if(tmp instanceof VarAccess) {
                		parent.setChild(tacc, idx);
                		boolean ambiguous = tacc.nameType() == NameType.AMBIGUOUS_NAME;
                		newacc = this.accessType((TypeDecl)target, ambiguous);
                		if(newacc == null)
                			throw new RefactoringException("couldn't consistently rename type access at "+pos);
                		if(this.isQualified()) {
                			newacc = this.qualifier().mergeWithAccess(newacc);
                			if(newacc == null)
                				throw new RefactoringException("couldn't consistently rename field access at "+pos);
                			oldacc = parent;
                		} else {
                			oldacc = this;
                		}
                	}
                } finally {
                	parent.setChild(tacc, idx);
                }
            }
		} else if(target instanceof PackageDecl) {
			Access acc = this;
			ASTNode parent = acc; int idx;
			do {
				acc = (Access)parent;
				parent = acc.getParent();
				idx = parent.getIndexOfChild(acc);
			} while(parent instanceof AbstractDot);
			ParseName pn = new ParseName(acc.packageName());
			parent.setChild(pn, idx);
			try {
				if(parent.getChild(idx) instanceof VarAccess)
					throw new RefactoringException("package access at "+pos+" is shadowed");
				// package accesses never have to be merged through mergeWithAccess()
			} finally {
				parent.setChild(acc, idx);
			}
		} else {
			throw new RefactoringException("don't know how to adjust access to "+target.getClass()+" "+target);
		}
		if(oldacc != newacc)
			changes.add(new NodeReplace(oldacc, newacc));
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 11

    public Access() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    // Declared in LookupMethod.jrag at line 8
    public Expr unqualifiedScope() {
        Expr unqualifiedScope_value = unqualifiedScope_compute();
        return unqualifiedScope_value;
    }

    private Expr unqualifiedScope_compute() {  return  isQualified() ? nestedScope() : this;  }

    // Declared in ResolveAmbiguousNames.jrag at line 49
    public boolean isQualified() {
        boolean isQualified_value = isQualified_compute();
        return isQualified_value;
    }

    private boolean isQualified_compute() {  return  hasPrevExpr();  }

    // Declared in ResolveAmbiguousNames.jrag at line 52
    public Expr qualifier() {
        Expr qualifier_value = qualifier_compute();
        return qualifier_value;
    }

    private Expr qualifier_compute() {  return  prevExpr();  }

    // Declared in ResolveAmbiguousNames.jrag at line 57
    public Access lastAccess() {
        Access lastAccess_value = lastAccess_compute();
        return lastAccess_value;
    }

    private Access lastAccess_compute() {  return  this;  }

    protected boolean prevExpr_computed = false;
    protected Expr prevExpr_value;
    // Declared in ResolveAmbiguousNames.jrag at line 69
    public Expr prevExpr() {
        if(prevExpr_computed)
            return prevExpr_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        prevExpr_value = prevExpr_compute();
        if(isFinal && num == boundariesCrossed)
            prevExpr_computed = true;
        return prevExpr_value;
    }

    private Expr prevExpr_compute()  {
    if(isLeftChildOfDot()) {
      if(parentDot().isRightChildOfDot())
        return parentDot().parentDot().leftSide();
    }
    else if(isRightChildOfDot())
      return parentDot().leftSide();
    throw new Error(this + " does not have a previous expression");
  }

    protected boolean hasPrevExpr_computed = false;
    protected boolean hasPrevExpr_value;
    // Declared in ResolveAmbiguousNames.jrag at line 80
    public boolean hasPrevExpr() {
        if(hasPrevExpr_computed)
            return hasPrevExpr_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        hasPrevExpr_value = hasPrevExpr_compute();
        if(isFinal && num == boundariesCrossed)
            hasPrevExpr_computed = true;
        return hasPrevExpr_value;
    }

    private boolean hasPrevExpr_compute()  {
    if(isLeftChildOfDot()) {
      if(parentDot().isRightChildOfDot())
        return true;
    }
    else if(isRightChildOfDot())
      return true;
    return false;
  }

    // Declared in SyntacticClassification.jrag at line 46
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.NO_NAME;  }

    protected boolean type_computed = false;
    protected TypeDecl type_value;
    // Declared in TypeAnalysis.jrag at line 280
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

    private TypeDecl type_compute() {  return  unknownType();  }

    // Declared in LookupMethod.jrag at line 9
    public Expr nestedScope() {
        Expr nestedScope_value = getParent().Define_Expr_nestedScope(this, null);
        return nestedScope_value;
    }

    // Declared in LookupType.jrag at line 123
    public TypeDecl unknownType() {
        TypeDecl unknownType_value = getParent().Define_TypeDecl_unknownType(this, null);
        return unknownType_value;
    }

    // Declared in LookupVariable.jrag at line 230
    public Variable unknownField() {
        Variable unknownField_value = getParent().Define_Variable_unknownField(this, null);
        return unknownField_value;
    }

    // Declared in NameCheck.jrag at line 291
    public BodyDecl enclosingBodyDecl() {
        BodyDecl enclosingBodyDecl_value = getParent().Define_BodyDecl_enclosingBodyDecl(this, null);
        return enclosingBodyDecl_value;
    }

    protected java.util.Map accessField_FieldDeclaration_values;
    // Declared in AccessField.jrag at line 12
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
    // Declared in AccessMethod.jrag at line 8
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

    // Declared in Uses.jrag at line 81
    public PackageDecl findPackageDecl(String name) {
        PackageDecl findPackageDecl_String_value = getParent().Define_PackageDecl_findPackageDecl(this, null, name);
        return findPackageDecl_String_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
