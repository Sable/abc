
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


public class TryStmt extends Stmt implements Cloneable,  FinallyHost {
    public void flushCache() {
        super.flushCache();
        branches_computed = false;
        branches_value = null;
        branchesFromFinally_computed = false;
        branchesFromFinally_value = null;
        targetBranches_computed = false;
        targetBranches_value = null;
        escapedBranches_computed = false;
        escapedBranches_value = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        reachableThrow_CatchClause_values = null;
        canCompleteNormally_computed = false;
        handlesException_TypeDecl_values = null;
        typeError_computed = false;
        typeError_value = null;
        typeRuntimeException_computed = false;
        typeRuntimeException_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        TryStmt node = (TryStmt)super.clone();
        node.branches_computed = false;
        node.branches_value = null;
        node.branchesFromFinally_computed = false;
        node.branchesFromFinally_value = null;
        node.targetBranches_computed = false;
        node.targetBranches_value = null;
        node.escapedBranches_computed = false;
        node.escapedBranches_value = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.reachableThrow_CatchClause_values = null;
        node.canCompleteNormally_computed = false;
        node.handlesException_TypeDecl_values = null;
        node.typeError_computed = false;
        node.typeError_value = null;
        node.typeRuntimeException_computed = false;
        node.typeRuntimeException_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          TryStmt node = (TryStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        TryStmt res = (TryStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BranchTarget.jrag at line 52

  public void collectBranches(Collection c) {
    c.addAll(escapedBranches());
  }

    // Declared in BranchTarget.jrag at line 153

  public Stmt branchTarget(Stmt branchStmt) {
    if(targetBranches().contains(branchStmt))
      return this;
    return super.branchTarget(branchStmt);
  }

    // Declared in BranchTarget.jrag at line 191

  public void collectFinally(Stmt branchStmt, ArrayList list) {
    if(hasFinally() && !branchesFromFinally().contains(branchStmt))
      list.add(this);
    if(targetBranches().contains(branchStmt))
      return;
    super.collectFinally(branchStmt, list);
  }

    // Declared in ExceptionHandling.jrag at line 193


  protected boolean reachedException(TypeDecl type) {
    boolean found = false;
    // found is true if the exception type is caught by a catch clause
    for(int i = 0; i < getNumCatchClause() && !found; i++)
      if(getCatchClause(i).handles(type))
        found = true;
    // if an exception is thrown in the block and the exception is not caught and
    // either there is no finally block or the finally block can complete normally
    if(!found && (!hasFinally() || getFinally().canCompleteNormally()) )
      if(getBlock().reachedException(type))
        return true;
    // even if the exception is caught by the catch clauses they may 
    // throw new exceptions
    for(int i = 0; i < getNumCatchClause() && found; i++)
      if(getCatchClause(i).reachedException(type))
        return true;
    return hasFinally() && getFinally().reachedException(type);
  }

    // Declared in PrettyPrint.jadd at line 850


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("try ");
    getBlock().toString(s);
    for(int i = 0; i < getNumCatchClause(); i++) {
      s.append(indent());
      getCatchClause(i).toString(s);
    }
    if(hasFinally()) {
      s.append(indent());
      s.append("finally ");
      getFinally().toString(s);
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 223

    public TryStmt() {
        super();

        setChild(null, 0);
        setChild(new List(), 1);
        setChild(new Opt(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 223
    public TryStmt(Block p0, List p1, Opt p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
    }

    // Declared in java.ast at line 19


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 22

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 223
    public void setBlock(Block node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Block getBlock() {
        return (Block)getChild(0);
    }

    // Declared in java.ast at line 9


    public Block getBlockNoTransform() {
        return (Block)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 223
    public void setCatchClauseList(List list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumCatchClause = 0;

    // Declared in java.ast at line 7

    public int getNumCatchClause() {
        return getCatchClauseList().getNumChild();
    }

    // Declared in java.ast at line 11


    public CatchClause getCatchClause(int i) {
        return (CatchClause)getCatchClauseList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addCatchClause(CatchClause node) {
        List list = getCatchClauseList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setCatchClause(CatchClause node, int i) {
        List list = getCatchClauseList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getCatchClauseList() {
        return (List)getChild(1);
    }

    // Declared in java.ast at line 28


    public List getCatchClauseListNoTransform() {
        return (List)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 223
    public void setFinallyOpt(Opt opt) {
        setChild(opt, 2);
    }

    // Declared in java.ast at line 6


    public boolean hasFinally() {
        return getFinallyOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Block getFinally() {
        return (Block)getFinallyOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setFinally(Block node) {
        getFinallyOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getFinallyOpt() {
        return (Opt)getChild(2);
    }

    // Declared in java.ast at line 21


    public Opt getFinallyOptNoTransform() {
        return (Opt)getChildNoTransform(2);
    }

    protected boolean branches_computed = false;
    protected Collection branches_value;
    // Declared in BranchTarget.jrag at line 107
    public Collection branches() {
        if(branches_computed)
            return branches_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        branches_value = branches_compute();
        if(isFinal && num == boundariesCrossed)
            branches_computed = true;
        return branches_value;
    }

    private Collection branches_compute()  {
    HashSet set = new HashSet();
    getBlock().collectBranches(set);
    for(int i = 0; i < getNumCatchClause(); i++)
      getCatchClause(i).collectBranches(set);
    return set;
  }

    protected boolean branchesFromFinally_computed = false;
    protected Collection branchesFromFinally_value;
    // Declared in BranchTarget.jrag at line 115
    public Collection branchesFromFinally() {
        if(branchesFromFinally_computed)
            return branchesFromFinally_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        branchesFromFinally_value = branchesFromFinally_compute();
        if(isFinal && num == boundariesCrossed)
            branchesFromFinally_computed = true;
        return branchesFromFinally_value;
    }

    private Collection branchesFromFinally_compute()  {
    HashSet set = new HashSet();
    if(hasFinally())
      getFinally().collectBranches(set);
    return set;
  }

    protected boolean targetBranches_computed = false;
    protected Collection targetBranches_value;
    // Declared in BranchTarget.jrag at line 123
    public Collection targetBranches() {
        if(targetBranches_computed)
            return targetBranches_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        targetBranches_value = targetBranches_compute();
        if(isFinal && num == boundariesCrossed)
            targetBranches_computed = true;
        return targetBranches_value;
    }

    private Collection targetBranches_compute()  {
    HashSet set = new HashSet();
    if(hasFinally() && !getFinally().canCompleteNormally())
      set.addAll(branches());
    return set;
  }

    protected boolean escapedBranches_computed = false;
    protected Collection escapedBranches_value;
    // Declared in BranchTarget.jrag at line 131
    public Collection escapedBranches() {
        if(escapedBranches_computed)
            return escapedBranches_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        escapedBranches_value = escapedBranches_compute();
        if(isFinal && num == boundariesCrossed)
            escapedBranches_computed = true;
        return escapedBranches_value;
    }

    private Collection escapedBranches_compute()  {
    HashSet set = new HashSet();
    if(hasFinally())
      set.addAll(branchesFromFinally());
    if(!hasFinally() || getFinally().canCompleteNormally())
      set.addAll(branches());
    return set;
  }

    // Declared in DefiniteAssignment.jrag at line 663
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
    // 16.2.15 4th bullet
    if(!hasFinally()) {
      if(!getBlock().isDAafter(v))
        return false;
      for(int i = 0; i < getNumCatchClause(); i++)
        if(!getCatchClause(i).getBlock().isDAafter(v))
          return false;
      return true;
    }
    else {
      // 16.2.15 5th bullet
      if(getFinally().isDAafter(v))
        return true;
      if(!getBlock().isDAafter(v))
        return false;
      for(int i = 0; i < getNumCatchClause(); i++)
        if(!getCatchClause(i).getBlock().isDAafter(v))
          return false;
      return true;
    }
  }

    // Declared in DefiniteAssignment.jrag at line 920
    public boolean isDUafterFinally(Variable v) {
        boolean isDUafterFinally_Variable_value = isDUafterFinally_compute(v);
        return isDUafterFinally_Variable_value;
    }

    private boolean isDUafterFinally_compute(Variable v) {  return  getFinally().isDUafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 923
    public boolean isDAafterFinally(Variable v) {
        boolean isDAafterFinally_Variable_value = isDAafterFinally_compute(v);
        return isDAafterFinally_Variable_value;
    }

    private boolean isDAafterFinally_compute(Variable v) {  return  getFinally().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 1234
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
    // 16.2.14 4th bullet
    if(!hasFinally()) {
      if(!getBlock().isDUafter(v))
        return false;
      for(int i = 0; i < getNumCatchClause(); i++)
        if(!getCatchClause(i).getBlock().isDUafter(v))
          return false;
      return true;
    }
    else
      return getFinally().isDUafter(v);
  }

    protected java.util.Map reachableThrow_CatchClause_values;
    // Declared in ExceptionHandling.jrag at line 183
    public boolean reachableThrow(CatchClause c) {
        Object _parameters = c;
if(reachableThrow_CatchClause_values == null) reachableThrow_CatchClause_values = new java.util.HashMap(4);
        if(reachableThrow_CatchClause_values.containsKey(_parameters))
            return ((Boolean)reachableThrow_CatchClause_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean reachableThrow_CatchClause_value = reachableThrow_compute(c);
        if(isFinal && num == boundariesCrossed)
            reachableThrow_CatchClause_values.put(_parameters, Boolean.valueOf(reachableThrow_CatchClause_value));
        return reachableThrow_CatchClause_value;
    }

    private boolean reachableThrow_compute(CatchClause c) {  return  
    getBlock().reachedException(c.getParameter().type());  }

    // Declared in UnreachableStatements.jrag at line 104
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

    private boolean canCompleteNormally_compute()  {
     boolean anyCatchClauseCompleteNormally = false;
     for(int i = 0; i < getNumCatchClause() && !anyCatchClauseCompleteNormally; i++)
       anyCatchClauseCompleteNormally = getCatchClause(i).getBlock().canCompleteNormally();
     return (getBlock().canCompleteNormally() || anyCatchClauseCompleteNormally) &&
       (!hasFinally() || getFinally().canCompleteNormally());
  }

    // Declared in ControlFlowGraph.jrag at line 15
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  Set.empty().union(getBlock());  }

    // Declared in ControlFlowGraph.jrag at line 99
    public boolean containsReturn() {
        boolean containsReturn_value = containsReturn_compute();
        return containsReturn_value;
    }

    private boolean containsReturn_compute()  {
		if (getBlock().containsReturn()) {
			return true;
		}
		for (int i = 0; i < getNumCatchClause(); i++) {
			if (getCatchClause(i).getBlock().containsReturn()) {
				return true;
			}
		}
		if (hasFinally() && getFinally().containsReturn()) {
			return true;
		} 
		return false;
	}

    // Declared in ControlFlowGraph.jrag at line 121
    public Set uncaughtThrows() {
        Set uncaughtThrows_value = uncaughtThrows_compute();
        return uncaughtThrows_value;
    }

    private Set uncaughtThrows_compute()  {
		// Create a set containing the remaining throws after a throw-catch match
		Set uncaughtThrows = getBlock().uncaughtThrows();
		Set remainingThrows = Set.empty();
		for (Iterator itr = uncaughtThrows.iterator(); itr.hasNext();) {
			ThrowStmt throwStmt = (ThrowStmt)itr.next();
			boolean caught = false;
			for (int i = 0; i < getNumCatchClause() && !caught; i++) {
				caught = getCatchClause(i).handles(throwStmt.getExpr().type());
			}
			if (!caught) {
				remainingThrows = remainingThrows.union(throwStmt);
			}
		}
		// Add throws from catch blocks and finally
		for (int i = 0; i < getNumCatchClause(); i++) {
			remainingThrows = remainingThrows.union(getCatchClause(i).getBlock().uncaughtThrows());
		}
		if (hasFinally()) {
			remainingThrows = remainingThrows.union(getFinally().uncaughtThrows());
		}

		return remainingThrows;
	}

    protected java.util.Map handlesException_TypeDecl_values;
    // Declared in ExceptionHandling.jrag at line 26
    public boolean handlesException(TypeDecl exceptionType) {
        Object _parameters = exceptionType;
if(handlesException_TypeDecl_values == null) handlesException_TypeDecl_values = new java.util.HashMap(4);
        if(handlesException_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)handlesException_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean handlesException_TypeDecl_value = getParent().Define_boolean_handlesException(this, null, exceptionType);
        if(isFinal && num == boundariesCrossed)
            handlesException_TypeDecl_values.put(_parameters, Boolean.valueOf(handlesException_TypeDecl_value));
        return handlesException_TypeDecl_value;
    }

    protected boolean typeError_computed = false;
    protected TypeDecl typeError_value;
    // Declared in UnreachableStatements.jrag at line 127
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

    protected boolean typeRuntimeException_computed = false;
    protected TypeDecl typeRuntimeException_value;
    // Declared in UnreachableStatements.jrag at line 128
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

    // Declared in ControlFlowGraph.jrag at line 148
    public TryStmt enclosingTryStmt() {
        TryStmt enclosingTryStmt_value = getParent().Define_TryStmt_enclosingTryStmt(this, null);
        return enclosingTryStmt_value;
    }

    // Declared in ControlFlowGraph.jrag at line 155
    public boolean Define_boolean_hasEnclosingTryStmt(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_hasEnclosingTryStmt(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 159
    public boolean Define_boolean_isFinallyBlock(ASTNode caller, ASTNode child) {
        if(caller == getFinallyOptNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isFinallyBlock(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 274
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getFinallyOptNoTransform()) { 

		Set succSet = following();
		boolean addedExit = false;
		Set uncaughtThrows = uncaughtThrows();
		if (!uncaughtThrows.isEmpty()) {
			if (!hasEnclosingTryStmt()) {
				succSet.union(exitBlock());
				addedExit = true;
			} else {
				// For each uncaught throw search for the next step ( catch or finally eventually exit)
				for (Iterator itr = uncaughtThrows.iterator(); itr.hasNext(); ) {
					ThrowStmt throwStmt = (ThrowStmt)itr.next();
					TryStmt tryStmt = enclosingTryStmt();
					boolean foundNextStep = false;
					while (tryStmt != null && !foundNextStep) {
						TypeDecl throwType = throwStmt.getExpr().type();
						for (int i = 0; i < tryStmt.getNumCatchClause() && !foundNextStep; i++) {
							CatchClause catchClause = tryStmt.getCatchClause(i);
							if (catchClause.handles(throwType)) {
								succSet = succSet.union(catchClause.getBlock());
								foundNextStep = true;
							}  
						}
						if (tryStmt.hasFinally()) {
							succSet = succSet.union(getFinally());
							foundNextStep = true;
						}
						tryStmt = tryStmt.enclosingTryStmt();
					}		
					if (!foundNextStep && !addedExit) {
						succSet = succSet.union(exitBlock());
						addedExit = true;
					}
				}
			}
		} 
		if (containsReturn() && !hasEnclosingTryStmt() && !addedExit) {
			succSet = succSet.union(exitBlock());  
		}
		return succSet;
	}
        if(caller == getCatchClauseListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  hasFinally() ? Set.empty().union(getFinally()) : following();
        }
        if(caller == getBlockNoTransform()) {
            return  hasFinally() ? Set.empty().union(getFinally()) : following();
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 170
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getBlockNoTransform()) {
    for(int i = 0; i < getNumCatchClause(); i++)
      if(getCatchClause(i).handles(exceptionType))
        return true;
    if(hasFinally() && !getFinally().canCompleteNormally())
      return true;
    return handlesException(exceptionType);
  }
        if(caller == getCatchClauseListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    if(hasFinally() && !getFinally().canCompleteNormally())
      return true;
    return handlesException(exceptionType);
  }
}
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in UnreachableStatements.jrag at line 145
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getFinallyOptNoTransform()) {
            return  reachable();
        }
        if(caller == getCatchClauseListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  reachable();
        }
        if(caller == getBlockNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 151
    public TryStmt Define_TryStmt_enclosingTryStmt(ASTNode caller, ASTNode child) {
        if(caller == getCatchClauseListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  this;
        }
        if(caller == getBlockNoTransform()) {
            return  this;
        }
        return getParent().Define_TryStmt_enclosingTryStmt(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 112
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getFinallyOptNoTransform()) {
            return  reachable();
        }
        if(caller == getBlockNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 662
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getFinallyOptNoTransform()) {
            return  isDAbefore(v);
        }
        if(caller == getCatchClauseListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  getBlock().isDAbefore(v);
        }
        if(caller == getBlockNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in ControlFlowGraph.jrag at line 170
    public Set Define_Set_enclosingFinally(ASTNode caller, ASTNode child) {
        if(caller == getCatchClauseListNoTransform()) {
      int index = caller.getIndexOfChild(child);
            return  hasFinally() ? Set.empty().union(getFinally()) : Set.empty();
        }
        if(caller == getBlockNoTransform()) {
            return  hasFinally() ? Set.empty().union(getFinally()) : Set.empty();
        }
        return getParent().Define_Set_enclosingFinally(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 116
    public boolean Define_boolean_reachableCatchClause(ASTNode caller, ASTNode child) {
        if(caller == getCatchClauseListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    TypeDecl type = getCatchClause(childIndex).getParameter().type();
    for(int i = 0; i < childIndex; i++)
      if(getCatchClause(i).handles(type))
        return false;
    if(reachableThrow(getCatchClause(childIndex)))
      return true;
    if(type.mayCatch(typeError()) || type.mayCatch(typeRuntimeException()))
      return true;
    return false;
  }
}
        return getParent().Define_boolean_reachableCatchClause(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 1225
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getFinallyOptNoTransform()) {
    if(!getBlock().isDUeverywhere(v))
      return false;
    for(int i = 0; i < getNumCatchClause(); i++)
      if(!getCatchClause(i).getBlock().unassignedEverywhere(v, this))
        return false;
    return true;
  }
        if(caller == getCatchClauseListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    if(!getBlock().isDUafter(v))
      return false;
    if(!getBlock().isDUeverywhere(v))
      return false;
    return true;
  }
}
        if(caller == getBlockNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
