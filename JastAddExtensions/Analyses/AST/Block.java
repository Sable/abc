
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;
  // a statement that can be reached by break or continue
public class Block extends Stmt implements Cloneable,  VariableScope {
    public void flushCache() {
        super.flushCache();
        checkReturnDA_Variable_values = null;
        isDAafter_Variable_values = null;
        checkReturnDU_Variable_values = null;
        isDUafter_Variable_values = null;
        localVariableDeclaration_String_values = null;
        canCompleteNormally_computed = false;
        exitsAfter_Stmt_values = null;
        uncaughtThrowsBetween_Stmt_Stmt_values = null;
        lookupType_String_values = null;
        lookupVariable_String_values = null;
        accessField_FieldDeclaration_values = null;
        accessMethod_MethodDecl_List_values = null;
        accessType_TypeDecl_boolean_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        Block node = (Block)super.clone();
        node.checkReturnDA_Variable_values = null;
        node.isDAafter_Variable_values = null;
        node.checkReturnDU_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.localVariableDeclaration_String_values = null;
        node.canCompleteNormally_computed = false;
        node.exitsAfter_Stmt_values = null;
        node.uncaughtThrowsBetween_Stmt_Stmt_values = null;
        node.lookupType_String_values = null;
        node.lookupVariable_String_values = null;
        node.accessField_FieldDeclaration_values = null;
        node.accessMethod_MethodDecl_List_values = null;
        node.accessType_TypeDecl_boolean_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          Block node = (Block)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        Block res = (Block)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DeclareBeforeUse.jrag at line 12


  public boolean declaredBeforeUse(Variable decl, ASTNode use) {
    int indexDecl = ((ASTNode)decl).varChildIndex(this);
    int indexUse = use.varChildIndex(this);
    return indexDecl <= indexUse;
  }

    // Declared in DeclareBeforeUse.jrag at line 17

  public boolean declaredBeforeUse(Variable decl, int indexUse) {
    int indexDecl = ((ASTNode)decl).varChildIndex(this);
    return indexDecl <= indexUse;
  }

    // Declared in PrettyPrint.jadd at line 680


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("{\n");
    indent++;
    for(int i = 0; i < getNumStmt(); i++) {
      s.append(indent());
      getStmt(i).toString(s);
    }
    indent--;
    s.append(indent());
    s.append("}\n");
  }

    // Declared in ExtractMethod.jrag at line 50

	
	public void encapsulate(java.util.List changes, String name, 
				int begin, int end, boolean static_ctxt) 
			throws RefactoringException {
		Stmt begin_stmt = getStmt(begin);
		Stmt end_stmt = getStmt(end);
		Collection parms = new ArrayList();        // parameters of extracted method
		Collection localVars = new ArrayList();    // local variables of extracted method
		Opt ret = new Opt();                       // what extracted method returns
		Collection savedVars = new ArrayList();    // declarations that need to be inserted before
										           // call to extracted method
		Collection toBeRemoved = new ArrayList();  // declarations that should be removed after
										           // extracting the method
		Set exns = Set.empty();                    // the set of exceptions thrown by
												   // the selection
		Collection visibleDecls = begin_stmt.visibleLocalDecls();
		visibleDecls.addAll(localDeclsBetween(begin, end));
		analyseDeclarations(visibleDecls, begin_stmt, end_stmt, parms, localVars, ret, savedVars, toBeRemoved);
		exns = uncaughtThrowsBetween(begin_stmt, end_stmt);
		Collection bodystmts = new ArrayList();
		for(int i=begin;i<=end;++i)
			bodystmts.add(getStmt(i));
		// create declaration of method
		MethodDecl md = createMethod(static_ctxt, name, parms, ret, exns, localVars, bodystmts);
		// prepare new block body
		Collection stmts = new ArrayList();
		int i;
		for(i=0;i<begin;++i) {
			Stmt s = getStmt(i);
			if(!toBeRemoved.contains(s))
				stmts.add(s);
		}
		for(Iterator iter=savedVars.iterator();iter.hasNext();)
			stmts.add((Stmt)iter.next());
		List args = new List();
		for(Iterator iter=parms.iterator();iter.hasNext();)
			args.add(new VarAccess(((LocalDeclaration)iter.next()).getID()));
		MethodAccess ma = new MethodAccess(name, args);
		// add it as an expression statement, or with an assignment
		if(ret.isEmpty())
			stmts.add(new ExprStmt(ma));
		else {
			LocalDeclaration ld = (LocalDeclaration)ret.getChild(0);
			stmts.add(new ExprStmt(new AssignSimpleExpr(new VarAccess(ld.getID()), ma)));
		}
		// NB: no statements need to be removed after the selection
		for(i=end+1;i<getNumStmt();++i)
			stmts.add(getStmt(i));
		// now record changes to be made
		TypeDecl td = hostBodyDecl().hostType();
		// kludge: method decl needs a parent so we can compute its signature
		md.setParent(td);
		String sig = md.signature();
		md.setParent(null);
		td.addMethod(changes, md, sig, false);
		changes.add(new BlockBodyChange(this, stmts));
	}

    // Declared in ExtractMethod.jrag at line 107

	
	private void analyseDeclarations(Collection decls, Stmt begin_stmt, Stmt end_stmt,
			Collection parms, Collection localVars, Opt ret, Collection savedVars, 
			Collection toBeRemoved) throws RefactoringException {
		for(Iterator i=decls.iterator();i.hasNext();) {
			LocalDeclaration decl = (LocalDeclaration)i.next();
			if(decl.isValueParmFor(begin_stmt, end_stmt))
				parms.add(decl.asParameterDeclaration());
			if(decl.isOutParmFor(begin_stmt, end_stmt)) {
				if(!ret.isEmpty())
					throw new RefactoringException("ambiguous return value");
				ret.setChild(((ASTNode)decl).fullCopy(), 0);
			}
			if(decl.shouldMoveInto(begin_stmt, end_stmt)) {
				localVars.add(decl.asVariableDeclaration());
				if(!decl.accessedOutside(begin_stmt, end_stmt) 
						&& decl instanceof VariableDeclaration)
					toBeRemoved.add((ASTNode)decl);
			}
			if(decl.shouldMoveOutOf(begin_stmt, end_stmt) || 
					(decl.shouldDuplicate(begin_stmt, end_stmt) && 
							((Stmt)decl).between(begin_stmt, end_stmt)))
				savedVars.add(decl.asVariableDeclaration());
		}
	}

    // Declared in ExtractMethod.jrag at line 132

	
	private MethodDecl createMethod(boolean static_ctxt, String name, 
			Collection parms, Opt ret, 
			Set exns, Collection localVariables, Collection stmts) {
		// modifiers: "private", perhaps with a "static"
		Modifiers mod = new Modifiers();
		mod.addModifier(new Modifier("private"));
		if(static_ctxt)
			mod.addModifier(new Modifier("static"));
		// type access: either "void" or the type of the variable to be assigned to
		Access acc;
		if(ret.isEmpty()) {
			acc = new TypeAccess("void");
		} else {
			LocalDeclaration decl = (LocalDeclaration)ret.getChild(0);
			acc = (Access)decl.getTypeAccess().fullCopy(); 
		}
		// parameter declarations
		List parmdecls = new List();
		for(Iterator i=parms.iterator();i.hasNext();)
			parmdecls.add((ASTNode)i.next());
		// brackets
		// TODO: not implemented
		List brackets = new List();
		// thrown exceptions
		List throwdecls = new List();
		for(Iterator i=exns.iterator();i.hasNext();)
			// TODO: it's not that simple; we need to use accessType here
			throwdecls.add(new TypeAccess(((ThrowStmt)i.next()).getExpr().type().name()));
		// body
		List bodystmts = new List();
		for(Iterator i=localVariables.iterator();i.hasNext();)
			bodystmts.add((ASTNode)i.next());
		for(Iterator i=stmts.iterator();i.hasNext();)
			bodystmts.add((ASTNode)i.next());
		if(!ret.isEmpty()) {
			LocalDeclaration decl = (LocalDeclaration)ret.getChild(0);
			String varname = decl.getID();
			ReturnStmt stmt = new ReturnStmt(new VarAccess(varname));
			bodystmts.add(stmt);
		}
		Block body = new Block(bodystmts);
		return new MethodDecl(mod, acc, name, parmdecls, brackets, throwdecls, new Opt(body));
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 201

    public Block() {
        super();

        setChild(new List(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 201
    public Block(List p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 201
    public void setStmtList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumStmt = 0;

    // Declared in java.ast at line 7

    public int getNumStmt() {
        return getStmtList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Stmt getStmt(int i) {
        return (Stmt)getStmtList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addStmt(Stmt node) {
        List list = getStmtList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setStmt(Stmt node, int i) {
        List list = getStmtList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getStmtList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getStmtListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    protected java.util.Map checkReturnDA_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 291
    public boolean checkReturnDA(Variable v) {
        Object _parameters = v;
if(checkReturnDA_Variable_values == null) checkReturnDA_Variable_values = new java.util.HashMap(4);
        if(checkReturnDA_Variable_values.containsKey(_parameters))
            return ((Boolean)checkReturnDA_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean checkReturnDA_Variable_value = checkReturnDA_compute(v);
        if(isFinal && num == boundariesCrossed)
            checkReturnDA_Variable_values.put(_parameters, Boolean.valueOf(checkReturnDA_Variable_value));
        return checkReturnDA_Variable_value;
    }

    private boolean checkReturnDA_compute(Variable v)  {
    HashSet set = new HashSet();
    collectBranches(set);
    for(Iterator iter = set.iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof ReturnStmt) {
        ReturnStmt stmt = (ReturnStmt)o;
        if(!stmt.isDAafterReachedFinallyBlocks(v))
          return false;
      }
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 438
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

    private boolean isDAafter_compute(Variable v) {  return  getNumStmt() == 0 ? isDAbefore(v) : getStmt(getNumStmt()-1).isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 444
    public boolean isDUeverywhere(Variable v) {
        boolean isDUeverywhere_Variable_value = isDUeverywhere_compute(v);
        return isDUeverywhere_Variable_value;
    }

    private boolean isDUeverywhere_compute(Variable v) {  return  isDUbefore(v) && checkDUeverywhere(v);  }

    protected java.util.Map checkReturnDU_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 754
    public boolean checkReturnDU(Variable v) {
        Object _parameters = v;
if(checkReturnDU_Variable_values == null) checkReturnDU_Variable_values = new java.util.HashMap(4);
        if(checkReturnDU_Variable_values.containsKey(_parameters))
            return ((Boolean)checkReturnDU_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean checkReturnDU_Variable_value = checkReturnDU_compute(v);
        if(isFinal && num == boundariesCrossed)
            checkReturnDU_Variable_values.put(_parameters, Boolean.valueOf(checkReturnDU_Variable_value));
        return checkReturnDU_Variable_value;
    }

    private boolean checkReturnDU_compute(Variable v)  {
    HashSet set = new HashSet();
    collectBranches(set);
    for(Iterator iter = set.iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof ReturnStmt) {
        ReturnStmt stmt = (ReturnStmt)o;
        if(!stmt.isDUafterReachedFinallyBlocks(v))
          return false;
      }
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 876
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

    private boolean isDUafter_compute(Variable v) {  return  getNumStmt() == 0 ? isDUbefore(v) : getStmt(getNumStmt()-1).isDUafter(v);  }

    protected java.util.Map localVariableDeclaration_String_values;
    // Declared in LookupVariable.jrag at line 117
    public VariableDeclaration localVariableDeclaration(String name) {
        Object _parameters = name;
if(localVariableDeclaration_String_values == null) localVariableDeclaration_String_values = new java.util.HashMap(4);
        if(localVariableDeclaration_String_values.containsKey(_parameters))
            return (VariableDeclaration)localVariableDeclaration_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        VariableDeclaration localVariableDeclaration_String_value = localVariableDeclaration_compute(name);
        if(isFinal && num == boundariesCrossed)
            localVariableDeclaration_String_values.put(_parameters, localVariableDeclaration_String_value);
        return localVariableDeclaration_String_value;
    }

    private VariableDeclaration localVariableDeclaration_compute(String name)  {
    for(int i = 0; i < getNumStmt(); i++)
      if(getStmt(i).declaresVariable(name))
        return (VariableDeclaration)getStmt(i);
    return null;
  }

    // Declared in UnreachableStatements.jrag at line 28
    public boolean canCompleteNormally() {
        if(canCompleteNormally_computed)
            return canCompleteNormally_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        canCompleteNormally_value = canCompleteNormally_compute();
        if(isFinal && num == boundariesCrossed)
            canCompleteNormally_computed = true;
        return canCompleteNormally_value;
    }

    private boolean canCompleteNormally_compute() {  return  getNumStmt() == 0 ? reachable() : getStmt(getNumStmt() - 1).canCompleteNormally();  }

    // Declared in LocalVarNesting.jrag at line 5
    public RefactoringException acceptLocal(String name) {
        RefactoringException acceptLocal_String_value = acceptLocal_compute(name);
        return acceptLocal_String_value;
    }

    private RefactoringException acceptLocal_compute(String name)  {
		RefactoringException e;
		for(int i=0;i<getNumStmt();++i) {
			e = getStmt(i).acceptLocal(name);
			if(e != null) return e;
		}
		return null;
	}

    // Declared in ControlFlowGraph.jrag at line 7
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  getNumStmt() == 0 ? following() : Set.empty().union(getStmt(0).first());  }

    // Declared in ControlFlowGraph.jrag at line 90
    public boolean containsReturn() {
        boolean containsReturn_value = containsReturn_compute();
        return containsReturn_value;
    }

    private boolean containsReturn_compute()  {
		for (int i = 0; i < getNumStmt(); i++) {
			if (getStmt(i).containsReturn()) {
				return true;
			}
		}
		return false;
	}

    // Declared in ControlFlowGraph.jrag at line 164
    public boolean hasPathBranch() {
        boolean hasPathBranch_value = hasPathBranch_compute();
        return hasPathBranch_value;
    }

    private boolean hasPathBranch_compute() {  return  isFinallyBlock() && !((TryStmt)getParent()).getBlock().canCompleteNormally();  }

    // Declared in ControlFlowGraph.jrag at line 323
    public Set first() {
        Set first_value = first_compute();
        return first_value;
    }

    private Set first_compute() {  return  getNumStmt() > 0 ? Set.empty().union(getStmt(0).first()) : following();  }

    protected java.util.Set exitsAfter_Stmt_visited;
    protected java.util.Set exitsAfter_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set exitsAfter_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map exitsAfter_Stmt_values = new java.util.HashMap(4);
    public Set exitsAfter(Stmt stmt) {
        Object _parameters = stmt;
if(exitsAfter_Stmt_visited == null) exitsAfter_Stmt_visited = new java.util.HashSet(4);
if(exitsAfter_Stmt_values == null) exitsAfter_Stmt_values = new java.util.HashMap(4);
        if(exitsAfter_Stmt_computed.contains(_parameters))
            return (Set)exitsAfter_Stmt_values.get(_parameters);
        if (!exitsAfter_Stmt_initialized.contains(_parameters)) {
            exitsAfter_Stmt_initialized.add(_parameters);
            exitsAfter_Stmt_values.put(_parameters, Set.empty());
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            exitsAfter_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            Set new_exitsAfter_Stmt_value;
            do {
                CHANGE = false;
                new_exitsAfter_Stmt_value = exitsAfter_compute(stmt);
                if ((new_exitsAfter_Stmt_value==null && (Set)exitsAfter_Stmt_values.get(_parameters)!=null) || (new_exitsAfter_Stmt_value!=null && !new_exitsAfter_Stmt_value.equals((Set)exitsAfter_Stmt_values.get(_parameters))))
                    CHANGE = true;
                exitsAfter_Stmt_values.put(_parameters, new_exitsAfter_Stmt_value);
            } while (CHANGE);
            exitsAfter_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            exitsAfter_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            exitsAfter_compute(stmt);
            RESET_CYCLE = false;
            exitsAfter_Stmt_computed.remove(_parameters);
            exitsAfter_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_exitsAfter_Stmt_value;
        }
        if(!exitsAfter_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                exitsAfter_Stmt_computed.remove(_parameters);
                exitsAfter_Stmt_initialized.remove(_parameters);
                return (Set)exitsAfter_Stmt_values.get(_parameters);
            }
            exitsAfter_Stmt_visited.add(_parameters);
            Set new_exitsAfter_Stmt_value = exitsAfter_compute(stmt);
            if ((new_exitsAfter_Stmt_value==null && (Set)exitsAfter_Stmt_values.get(_parameters)!=null) || (new_exitsAfter_Stmt_value!=null && !new_exitsAfter_Stmt_value.equals((Set)exitsAfter_Stmt_values.get(_parameters))))
                CHANGE = true;
            exitsAfter_Stmt_values.put(_parameters, new_exitsAfter_Stmt_value);
            exitsAfter_Stmt_visited.remove(_parameters);
            return new_exitsAfter_Stmt_value;
        }
        return (Set)exitsAfter_Stmt_values.get(_parameters);
    }

    private Set exitsAfter_compute(Stmt stmt)  {
		Set set = Set.empty();
		for(Iterator i=stmt.gsucc(this).iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.between(this, -1, Integer.MAX_VALUE))
				set = set.union(stmt);
			set = set.union(exitsAfter(next));
		}
		return set;
	}

    // Declared in ExtractMethod.jrag at line 208
    public Collection localDeclsBetween(int start, int end) {
        Collection localDeclsBetween_int_int_value = localDeclsBetween_compute(start, end);
        return localDeclsBetween_int_int_value;
    }

    private Collection localDeclsBetween_compute(int start, int end)  {
		ArrayList decls = new ArrayList();
		for(int i=start;i<=end;++i)
			if(getStmt(i) instanceof VariableDeclaration)
				decls.add(getStmt(i));
		return decls;
	}

    protected java.util.Map uncaughtThrowsBetween_Stmt_Stmt_values;
    // Declared in ParameterClassification.jrag at line 26
    public Set uncaughtThrowsBetween(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(uncaughtThrowsBetween_Stmt_Stmt_values == null) uncaughtThrowsBetween_Stmt_Stmt_values = new java.util.HashMap(4);
        if(uncaughtThrowsBetween_Stmt_Stmt_values.containsKey(_parameters))
            return (Set)uncaughtThrowsBetween_Stmt_Stmt_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Set uncaughtThrowsBetween_Stmt_Stmt_value = uncaughtThrowsBetween_compute(begin, end);
        if(isFinal && num == boundariesCrossed)
            uncaughtThrowsBetween_Stmt_Stmt_values.put(_parameters, uncaughtThrowsBetween_Stmt_Stmt_value);
        return uncaughtThrowsBetween_Stmt_Stmt_value;
    }

    private Set uncaughtThrowsBetween_compute(Stmt begin, Stmt end)  {
		Set uncaughtThrows = Set.empty();
		int begin_idx = begin.indexInBlock(this);
		int end_idx = end.indexInBlock(this);
		for (int i = begin_idx; 0 <= i && i <= end_idx; ++i) {
			Stmt child = getStmt(i);
			uncaughtThrows = uncaughtThrows.union(getStmt(i).uncaughtThrows());
		}
		return uncaughtThrows;
	}

    protected java.util.Map lookupType_String_values;
    // Declared in LookupType.jrag at line 175
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
    // Declared in LookupVariable.jrag at line 8
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

    // Declared in UnreachableStatements.jrag at line 19
    public boolean reachable() {
        boolean reachable_value = getParent().Define_boolean_reachable(this, null);
        return reachable_value;
    }

    // Declared in ASTUtil.jrag at line 5
    public Program programRoot() {
        Program programRoot_value = getParent().Define_Program_programRoot(this, null);
        return programRoot_value;
    }

    protected java.util.Map accessField_FieldDeclaration_values;
    // Declared in AccessField.jrag at line 10
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
    // Declared in AccessMethod.jrag at line 7
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

    // Declared in AccessPackage.jrag at line 5
    public Access accessPackage(String pkg) {
        Access accessPackage_String_value = getParent().Define_Access_accessPackage(this, null, pkg);
        return accessPackage_String_value;
    }

    // Declared in AccessPackage.jrag at line 8
    public boolean hasPackage(String packageName) {
        boolean hasPackage_String_value = getParent().Define_boolean_hasPackage(this, null, packageName);
        return hasPackage_String_value;
    }

    protected java.util.Map accessType_TypeDecl_boolean_values;
    // Declared in AccessType.jrag at line 7
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

    // Declared in ExtractMethod.jrag at line 190
    public Collection Define_Collection_visibleLocalDecls(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) { 
   int k = caller.getIndexOfChild(child);
 {
		Collection decls = visibleLocalDecls();
		decls.addAll(localDeclsBetween(0,k-1));
		return decls;
	}
}
        return getParent().Define_Collection_visibleLocalDecls(this, caller);
    }

    // Declared in GuardedControlFlow.jrag at line 27
    public boolean Define_boolean_between(ASTNode caller, ASTNode child, Block blk, int start, int end) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		return blk == this ? start <= i && i <= end : between(blk, start, end);  
	}
}
        return getParent().Define_boolean_between(this, caller, blk, start, end);
    }

    // Declared in AccessType.jrag at line 114
    public Access Define_Access_accessType(ASTNode caller, ASTNode child, TypeDecl td, boolean ambiguous) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		Access acc = accessType(td, ambiguous);
		if(acc != null && localVariableDeclaration(td.getID()) != null) {
			if(acc instanceof AbstractDot) {
				Expr left = ((AbstractDot)acc).getLeft();
				if(left.isPackageAccess()) {
					Access pkgacc = accessPackage(((PackageAccess)left).getPackage());
					if(pkgacc == null) return null;
					return pkgacc.qualifiesAccess(((AbstractDot)acc).getRight());
				} else if(left.isTypeAccess()) {
					Access tacc = accessType(((TypeAccess)left).decl(), ambiguous);
					if(tacc == null) return null;
					return tacc.qualifiesAccess(((AbstractDot)acc).getRight());
				} else {
					assert(false);
				}
			} else {
				if(td.isNestedType()) {
					TypeDecl enc = td.enclosingType();
					Access encacc = getStmt(i).accessType(enc, ambiguous);
					if(encacc == null) return null;
					Access innacc = enc.getBodyDecl(0).accessType(td, ambiguous);
					if(acc == null) return null;
					return encacc.qualifiesAccess(acc);
				} else if(!td.packageName().equals("") && accessPackage(td.packageName()) != null) {
					return accessPackage(td.packageName()).qualifiesAccess(acc);
				} else {
					return null;
				}
			}
		} else {
			return acc;
		}
	}
}
        return getParent().Define_Access_accessType(this, caller, td, ambiguous);
    }

    // Declared in ControlFlowGraph.jrag at line 160
    public boolean Define_boolean_isFinallyBlock(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  isFinallyBlock() ? false : isFinallyBlock();
        }
        return getParent().Define_boolean_isFinallyBlock(this, caller);
    }

    // Declared in LookupVariable.jrag at line 71
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getStmtListNoTransform()) { 
   int index = caller.getIndexOfChild(child);
 {
    VariableDeclaration v = localVariableDeclaration(name);
    // declare before use and shadowing
    if(v != null && declaredBeforeUse(v, index))
      return v;
    return lookupVariable(name);
  }
}
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in ControlFlowGraph.jrag at line 222
    public DefaultCase Define_DefaultCase_followingDefaultCase(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		if (!(getParent() instanceof SwitchStmt) || i == getNumStmt() - 1) {
			return followingDefaultCase();
		} 
		for (int index = 0; index < getNumStmt(); index++) {
			Stmt stmt = getStmt(index);
			if (stmt instanceof DefaultCase) {
				return (DefaultCase)stmt;
			}
		}
		return followingDefaultCase();
	}
}
        return getParent().Define_DefaultCase_followingDefaultCase(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 29
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  childIndex == 0 ? reachable() : getStmt(childIndex-1).canCompleteNormally();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in AccessField.jrag at line 191
    public Access Define_Access_accessField(ASTNode caller, ASTNode child, FieldDeclaration fd) {
        if(caller == getStmtListNoTransform()) { 
   int index = caller.getIndexOfChild(child);
 {
		Access acc = accessField(fd);
		if(acc != null) {
			if(acc instanceof AbstractDot)
				return acc;
			VariableDeclaration v = localVariableDeclaration(fd.getID());
			if(v != null && declaredBeforeUse(v, index)) {
				return new ThisAccess("this").qualifiesAccess(acc);
			}
			return acc;
		}
		return null;
	}
}
        return getParent().Define_Access_accessField(this, caller, fd);
    }

    // Declared in DefiniteAssignment.jrag at line 441
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getStmtListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  index == 0 ? isDAbefore(v) : getStmt(index - 1).isDAafter(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in ControlFlowGraph.jrag at line 239
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 { 
		if (i == getNumStmt() - 1) {
			return following();
		}
		Stmt thisStmt = getStmt(i);
		Stmt nextStmt = getStmt(i + 1);

		if (thisStmt instanceof ConstCase) {
			ConstCase nextCase = thisStmt.followingConstCase();
			if (nextCase == null) {
				DefaultCase defaultCase = thisStmt.followingDefaultCase();
				if (defaultCase == null) {
					return following().union(nextStmt);
				} 
				return Set.empty().union(nextStmt.first()).union(defaultCase);
			}
			return Set.empty().union(nextStmt.first()).union(nextCase);
		} else if (nextStmt instanceof Case) {
			if (i + 2 < getNumStmt()) {
				return Set.empty().union(getStmt(i + 2).first());
			}  
			return following();
		} 

		return Set.empty().union(nextStmt.first());
	}
}
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 207
    public ConstCase Define_ConstCase_followingConstCase(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		if (!(getParent() instanceof SwitchStmt) || i == getNumStmt() - 1) {
			return followingConstCase();
		} 
		for (int index = i + 1; index < getNumStmt(); index++) {
			Stmt stmt = getStmt(index);
			if (stmt instanceof ConstCase) {
				return (ConstCase)stmt;
			}
		}
		return followingConstCase();
	}
}
        return getParent().Define_ConstCase_followingConstCase(this, caller);
    }

    // Declared in Domination.jrag at line 52
    public Block Define_Block_hostBlock(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  this;
        }
        return getParent().Define_Block_hostBlock(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 41
    public boolean Define_boolean_isIncOrDec(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isIncOrDec(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 137
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  i == 0 ? reachable() : getStmt(i-1).reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in AccessPackage.jrag at line 31
    public Access Define_Access_accessPackage(ASTNode caller, ASTNode child, String pkg) {
        if(caller == getStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
		String[] path = pkg.split("\\.");
		if(getStmt(i).lookupType(path[0]).isEmpty() && 
				getStmt(i).lookupVariable(path[0]).isEmpty() && hasPackage(pkg))
			return new PackageAccess(pkg);
		return null;
	}
}
        return getParent().Define_Access_accessPackage(this, caller, pkg);
    }

    // Declared in NameCheck.jrag at line 279
    public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  this;
        }
        return getParent().Define_VariableScope_outerScope(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 106
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.EXPRESSION_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in LookupType.jrag at line 234
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getStmtListNoTransform()) { 
   int index = caller.getIndexOfChild(child);
 {
    SimpleSet c = SimpleSet.emptySet;
    for(int i = index; i >= 0 && !(getStmt(i) instanceof Case); i--) {
      if(getStmt(i) instanceof LocalClassDeclStmt) {
        TypeDecl t = ((LocalClassDeclStmt)getStmt(i)).getClassDecl();
        if(t.name().equals(name)) {
          c = c.add(t);
        }
      }
    }
    if(!c.isEmpty())
      return c;
    return lookupType(name);
  }
}
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

    // Declared in DefiniteAssignment.jrag at line 877
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getStmtListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  index == 0 ? isDUbefore(v) : getStmt(index - 1).isDUafter(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
