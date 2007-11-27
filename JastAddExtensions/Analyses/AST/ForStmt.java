
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class ForStmt extends BranchTargetStmt implements Cloneable,  VariableScope {
    public void flushCache() {
        super.flushCache();
        targetOf_ContinueStmt_values = null;
        targetOf_BreakStmt_values = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        localLookup_String_values = null;
        localVariableDeclaration_String_values = null;
        canCompleteNormally_computed = false;
        lookupVariable_String_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ForStmt node = (ForStmt)super.clone();
        node.targetOf_ContinueStmt_values = null;
        node.targetOf_BreakStmt_values = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.localLookup_String_values = null;
        node.localVariableDeclaration_String_values = null;
        node.canCompleteNormally_computed = false;
        node.lookupVariable_String_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ForStmt node = (ForStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ForStmt res = (ForStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in DefiniteAssignment.jrag at line 1131

  // 16.2.11 4th bullet
  private boolean conditionVisited;

    // Declared in DefiniteAssignment.jrag at line 1132

  private boolean isDUbeforeCondition(Variable v) {
    if(conditionVisited)
      return true;
    conditionVisited = true;
    boolean result = true;
    if(!isDUafterInit(v))
      result = false;
    else if(!isDUafterUpdate(v))
      result = false;
    conditionVisited = false;
    return result;
  }

    // Declared in PrettyPrint.jadd at line 761


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("for(");
    
    if(getNumInitStmt() > 0) {
      if(getInitStmt(0) instanceof VariableDeclaration) {
        VariableDeclaration var = (VariableDeclaration)getInitStmt(0);
        var.getModifiers().toString(s);
        var.getTypeAccess().toString(s);
        s.append(" " + var.name());
        for(int i = 1; i < getNumInitStmt(); i++) {
          s.append(", ");
          s.append(((VariableDeclaration)getInitStmt(i)).name());
        }
      }
      else {
        if(!(getInitStmt(0) instanceof ExprStmt)) {
          System.err.println("Found unexpected type: " + getInitStmt(0).getClass().getName());
        }
        ExprStmt stmt = (ExprStmt)getInitStmt(0);
        stmt.getExpr().toString(s);
        for(int i = 1; i < getNumInitStmt(); i++) {
          s.append(", ");
          stmt = (ExprStmt)getInitStmt(i);
          stmt.getExpr().toString(s);
        }
      }
    }
    
    s.append("; ");
    if(hasCondition()) {
      getCondition().toString(s);
    }
    s.append("; ");

    if(getNumUpdateStmt() > 0) {
      ExprStmt stmt = (ExprStmt)getUpdateStmt(0);
      stmt.getExpr().toString(s);
      for(int i = 1; i < getNumUpdateStmt(); i++) {
        s.append(", ");
        stmt = (ExprStmt)getUpdateStmt(i);
        stmt.getExpr().toString(s);
      }
    }
    
    s.append(") ");
    getStmt().toString(s);
  }

    // Declared in TypeCheck.jrag at line 323

  public void typeCheck() {
    if(hasCondition()) {
      TypeDecl cond = getCondition().type();
      if(!cond.isBoolean()) {
        error("the type of \"" + getCondition() + "\" is " + cond.name() + " which is not boolean");
      }
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 214

    public ForStmt() {
        super();

        setChild(new List(), 0);
        setChild(new Opt(), 1);
        setChild(new List(), 2);
        setChild(null, 3);

    }

    // Declared in java.ast at line 14


    // Declared in java.ast line 214
    public ForStmt(List p0, Opt p1, List p2, Stmt p3) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
        setChild(p3, 3);
    }

    // Declared in java.ast at line 21


  protected int numChildren() {
    return 4;
  }

    // Declared in java.ast at line 24

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 214
    public void setInitStmtList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumInitStmt = 0;

    // Declared in java.ast at line 7

    public int getNumInitStmt() {
        return getInitStmtList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Stmt getInitStmt(int i) {
        return (Stmt)getInitStmtList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addInitStmt(Stmt node) {
        List list = getInitStmtList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setInitStmt(Stmt node, int i) {
        List list = getInitStmtList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getInitStmtList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getInitStmtListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 214
    public void setConditionOpt(Opt opt) {
        setChild(opt, 1);
    }

    // Declared in java.ast at line 6


    public boolean hasCondition() {
        return getConditionOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Expr getCondition() {
        return (Expr)getConditionOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setCondition(Expr node) {
        getConditionOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getConditionOpt() {
        return (Opt)getChild(1);
    }

    // Declared in java.ast at line 21


    public Opt getConditionOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 214
    public void setUpdateStmtList(List list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumUpdateStmt = 0;

    // Declared in java.ast at line 7

    public int getNumUpdateStmt() {
        return getUpdateStmtList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Stmt getUpdateStmt(int i) {
        return (Stmt)getUpdateStmtList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addUpdateStmt(Stmt node) {
        List list = getUpdateStmtList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setUpdateStmt(Stmt node, int i) {
        List list = getUpdateStmtList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getUpdateStmtList() {
        return (List)getChild(2);
    }

    // Declared in java.ast at line 28


    public List getUpdateStmtListNoTransform() {
        return (List)getChildNoTransform(2);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 214
    public void setStmt(Stmt node) {
        setChild(node, 3);
    }

    // Declared in java.ast at line 5

    public Stmt getStmt() {
        return (Stmt)getChild(3);
    }

    // Declared in java.ast at line 9


    public Stmt getStmtNoTransform() {
        return (Stmt)getChildNoTransform(3);
    }

    protected java.util.Map targetOf_ContinueStmt_values;
    // Declared in BranchTarget.jrag at line 63
    public boolean targetOf(ContinueStmt stmt) {
        Object _parameters = stmt;
if(targetOf_ContinueStmt_values == null) targetOf_ContinueStmt_values = new java.util.HashMap(4);
        if(targetOf_ContinueStmt_values.containsKey(_parameters))
            return ((Boolean)targetOf_ContinueStmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean targetOf_ContinueStmt_value = targetOf_compute(stmt);
        if(isFinal && num == boundariesCrossed)
            targetOf_ContinueStmt_values.put(_parameters, Boolean.valueOf(targetOf_ContinueStmt_value));
        return targetOf_ContinueStmt_value;
    }

    private boolean targetOf_compute(ContinueStmt stmt) {  return  !stmt.hasLabel();  }

    protected java.util.Map targetOf_BreakStmt_values;
    // Declared in BranchTarget.jrag at line 71
    public boolean targetOf(BreakStmt stmt) {
        Object _parameters = stmt;
if(targetOf_BreakStmt_values == null) targetOf_BreakStmt_values = new java.util.HashMap(4);
        if(targetOf_BreakStmt_values.containsKey(_parameters))
            return ((Boolean)targetOf_BreakStmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean targetOf_BreakStmt_value = targetOf_compute(stmt);
        if(isFinal && num == boundariesCrossed)
            targetOf_BreakStmt_values.put(_parameters, Boolean.valueOf(targetOf_BreakStmt_value));
        return targetOf_BreakStmt_value;
    }

    private boolean targetOf_compute(BreakStmt stmt) {  return  !stmt.hasLabel();  }

    // Declared in DefiniteAssignment.jrag at line 609
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
    if(!(!hasCondition() || getCondition().isDAafterFalse(v)))
      return false;
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDAafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 622
    public boolean isDAafterInitialization(Variable v) {
        boolean isDAafterInitialization_Variable_value = isDAafterInitialization_compute(v);
        return isDAafterInitialization_Variable_value;
    }

    private boolean isDAafterInitialization_compute(Variable v) {  return  getNumInitStmt() == 0 ? isDAbefore(v) : getInitStmt(getNumInitStmt()-1).isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 1111
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
    if(!(!hasCondition() || getCondition().isDUafterFalse(v))) {
      return false;
    }
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDUafterReachedFinallyBlocks(v))
        return false;
    }
    //if(!isDUafterUpdate(v))
    //  return false;
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 1129
    public boolean isDUafterInit(Variable v) {
        boolean isDUafterInit_Variable_value = isDUafterInit_compute(v);
        return isDUafterInit_Variable_value;
    }

    private boolean isDUafterInit_compute(Variable v) {  return  getNumInitStmt() == 0 ? isDUbefore(v) : getInitStmt(getNumInitStmt()-1).isDUafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 1148
    public boolean isDUafterUpdate(Variable v) {
        boolean isDUafterUpdate_Variable_value = isDUafterUpdate_compute(v);
        return isDUafterUpdate_Variable_value;
    }

    private boolean isDUafterUpdate_compute(Variable v)  {
    if(getNumUpdateStmt() > 0)
      return getUpdateStmt(getNumUpdateStmt()-1).isDUafter(v);
    if(!getStmt().isDUafter(v))
      return false;
    for(Iterator iter = targetContinues().iterator(); iter.hasNext(); ) {
      ContinueStmt stmt = (ContinueStmt)iter.next();
      if(!stmt.isDUafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    protected java.util.Map localLookup_String_values;
    // Declared in LookupVariable.jrag at line 94
    public SimpleSet localLookup(String name) {
        Object _parameters = name;
if(localLookup_String_values == null) localLookup_String_values = new java.util.HashMap(4);
        if(localLookup_String_values.containsKey(_parameters))
            return (SimpleSet)localLookup_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet localLookup_String_value = localLookup_compute(name);
        if(isFinal && num == boundariesCrossed)
            localLookup_String_values.put(_parameters, localLookup_String_value);
        return localLookup_String_value;
    }

    private SimpleSet localLookup_compute(String name)  {
    VariableDeclaration v = localVariableDeclaration(name);
    if(v != null) return v;
    return lookupVariable(name);
  }

    protected java.util.Map localVariableDeclaration_String_values;
    // Declared in LookupVariable.jrag at line 124
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
    for(int i = 0; i < getNumInitStmt(); i++)
      if(getInitStmt(i).declaresVariable(name))
        return (VariableDeclaration)getInitStmt(i);
    return null;
  }

    // Declared in NameCheck.jrag at line 392
    public boolean continueLabel() {
        boolean continueLabel_value = continueLabel_compute();
        return continueLabel_value;
    }

    private boolean continueLabel_compute() {  return  true;  }

    // Declared in UnreachableStatements.jrag at line 93
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

    private boolean canCompleteNormally_compute() {  return  reachable() && hasCondition() && (!getCondition().isConstant() || !getCondition().isTrue()) || reachableBreak();  }

    // Declared in LocalVarNesting.jrag at line 19
    public RefactoringException acceptLocal(String name) {
        RefactoringException acceptLocal_String_value = acceptLocal_compute(name);
        return acceptLocal_String_value;
    }

    private RefactoringException acceptLocal_compute(String name)  {
		RefactoringException e;
		int i;
		for(i=0;i<getNumInitStmt();++i) {
			e = getInitStmt(i).acceptLocal(name);
			if(e != null) return e;
		}
		// the update statement cannot declare variables, so we can ignore it
        e = this.getStmt().acceptLocal(name);
        return e;
	}

    // Declared in ControlFlowGraph.jrag at line 11
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  following().union(getStmt());  }

    // Declared in ControlFlowGraph.jrag at line 82
    public boolean hasCondBranch() {
        boolean hasCondBranch_value = hasCondBranch_compute();
        return hasCondBranch_value;
    }

    private boolean hasCondBranch_compute() {  return  true;  }

    // Declared in ControlFlowGraph.jrag at line 324
    public Set first() {
        Set first_value = first_compute();
        return first_value;
    }

    private Set first_compute() {  return  getNumInitStmt() > 0 ? Set.empty().union(getInitStmt(0)) : super.first();  }

    protected java.util.Map lookupVariable_String_values;
    // Declared in LookupVariable.jrag at line 9
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

    // Declared in ExtractMethod.jrag at line 200
    public Collection Define_Collection_visibleLocalDecls(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
		Collection decls = visibleLocalDecls();
		for(int i=0;i<getNumInitStmt();++i)
			if(getInitStmt(i) instanceof VariableDeclaration)
				decls.add(getInitStmt(i));
		return decls;
	}
        return getParent().Define_Collection_visibleLocalDecls(this, caller);
    }

    // Declared in NameCheck.jrag at line 360
    public boolean Define_boolean_insideLoop(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_insideLoop(this, caller);
    }

    // Declared in LookupVariable.jrag at line 93
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getStmtNoTransform()) {
            return  localLookup(name);
        }
        if(caller == getUpdateStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  localLookup(name);
        }
        if(caller == getConditionOptNoTransform()) {
            return  localLookup(name);
        }
        if(caller == getInitStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  localLookup(name);
        }
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in ControlFlowGraph.jrag at line 269
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return  getNumUpdateStmt() > 0 ? Set.empty().union(getUpdateStmt(0)) : Set.empty().union(this);
        }
        if(caller == getUpdateStmtListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  Set.empty().union(this);
        }
        if(caller == getInitStmtListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  Set.empty().union(this);
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in Domination.jrag at line 57
    public Block Define_Block_hostBlock(ASTNode caller, ASTNode child) {
        if(caller == getUpdateStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  hostBlock();
        }
        if(caller == getInitStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  hostBlock();
        }
        if(caller == getStmtNoTransform()) {
            return  hostBlock();
        }
        return getParent().Define_Block_hostBlock(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 140
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in Domination.jrag at line 73
    public boolean Define_boolean_isInitOrUpdateStmt(ASTNode caller, ASTNode child) {
        if(caller == getUpdateStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        if(caller == getInitStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  true;
        }
        return getParent().Define_boolean_isInitOrUpdateStmt(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 94
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return  reachable() && (!hasCondition() || (!getCondition().isConstant() || !getCondition().isFalse()));
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in NameCheck.jrag at line 282
    public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return  this;
        }
        if(caller == getInitStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  this;
        }
        return getParent().Define_VariableScope_outerScope(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 633
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getUpdateStmtListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    if(!getStmt().isDAafter(v))
      return false;
    for(Iterator iter = targetContinues().iterator(); iter.hasNext(); ) {
      ContinueStmt stmt = (ContinueStmt)iter.next();
      if(!stmt.isDAafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }
}
        if(caller == getStmtNoTransform()) {
    if(hasCondition() && getCondition().isDAafterTrue(v))
      return true;
    if(!hasCondition() && isDAafterInitialization(v))
      return true;
    return false;
  }
        if(caller == getConditionOptNoTransform()) {
            return  isDAafterInitialization(v);
        }
        if(caller == getInitStmtListNoTransform()) {
      int i = caller.getIndexOfChild(child);
            return  i == 0 ? isDAbefore(v) : getInitStmt(i-1).isDAafter(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 1162
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getUpdateStmtListNoTransform()) { 
   int i = caller.getIndexOfChild(child);
 {
    if(i == 0) {
      if(!getStmt().isDUafter(v))
        return false;
      for(Iterator iter = targetContinues().iterator(); iter.hasNext(); ) {
        ContinueStmt stmt = (ContinueStmt)iter.next();
        if(!stmt.isDUafterReachedFinallyBlocks(v))
          return false;
      }
      return true;
    }
    else
      return getUpdateStmt(i-1).isDUafter(v);
  }
}
        if(caller == getStmtNoTransform()) {
            return  hasCondition() ?
    getCondition().isDUafterTrue(v) : isDUafterInit(v);
        }
        if(caller == getConditionOptNoTransform()) {
            return  isDUbeforeCondition(v);
        }
        if(caller == getInitStmtListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  childIndex == 0 ? isDUbefore(v) : getInitStmt(childIndex-1).isDUafter(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    // Declared in DefiniteAssignment.jrag at line 1178
    if(!hasCondition()) {
        duringDefiniteAssignment++;
        ASTNode result = rewriteRule0();
        duringDefiniteAssignment--;
        return result;
    }

    return super.rewriteTo();
}

    // Declared in DefiniteAssignment.jrag at line 1178
    private ForStmt rewriteRule0() {
      setCondition(new BooleanLiteral("true"));
      return this;
    }
}
