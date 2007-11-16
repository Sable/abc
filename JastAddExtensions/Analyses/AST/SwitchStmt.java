
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


public class SwitchStmt extends BranchTargetStmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        targetOf_ContinueStmt_values = null;
        targetOf_BreakStmt_values = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        typeInt_computed = false;
        typeInt_value = null;
        typeLong_computed = false;
        typeLong_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        SwitchStmt node = (SwitchStmt)super.clone();
        node.targetOf_ContinueStmt_values = null;
        node.targetOf_BreakStmt_values = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.typeInt_computed = false;
        node.typeInt_value = null;
        node.typeLong_computed = false;
        node.typeLong_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          SwitchStmt node = (SwitchStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        SwitchStmt res = (SwitchStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 711


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("switch (");
    getExpr().toString(s);
    s.append(")");
    getBlock().toString(s);
  }

    // Declared in TypeCheck.jrag at line 332


  public void typeCheck() {
    TypeDecl type = getExpr().type();
    if(!type.isIntegralType() || type.isLong())
      error("Switch expression must be of char, byte, short, or int");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 206

    public SwitchStmt() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 206
    public SwitchStmt(Expr p0, Block p1) {
        setChild(p0, 0);
        setChild(p1, 1);
    }

    // Declared in java.ast at line 17


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 20

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 206
    public void setExpr(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getExpr() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getExprNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 206
    public void setBlock(Block node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Block getBlock() {
        return (Block)getChild(1);
    }

    // Declared in java.ast at line 9


    public Block getBlockNoTransform() {
        return (Block)getChildNoTransform(1);
    }

    protected java.util.Map targetOf_ContinueStmt_values;
    // Declared in BranchTarget.jrag at line 64
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

    private boolean targetOf_compute(ContinueStmt stmt) {  return  false;  }

    protected java.util.Map targetOf_BreakStmt_values;
    // Declared in BranchTarget.jrag at line 68
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

    // Declared in DefiniteAssignment.jrag at line 528
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
    if(!(!noDefaultLabel() || getExpr().isDAafter(v))) {
      return false;
    }
    if(!(!switchLabelEndsBlock() || getExpr().isDAafter(v))) {
      return false;
    }
    if(!assignedAfterLastStmt(v)) {
      return false;
    }
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDAafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 546
    public boolean assignedAfterLastStmt(Variable v) {
        boolean assignedAfterLastStmt_Variable_value = assignedAfterLastStmt_compute(v);
        return assignedAfterLastStmt_Variable_value;
    }

    private boolean assignedAfterLastStmt_compute(Variable v) {  return 
    getBlock().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 1006
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
    if(!(!noDefaultLabel() || getExpr().isDUafter(v)))
      return false;
    if(!(!switchLabelEndsBlock() || getExpr().isDUafter(v)))
      return false;
    if(!unassignedAfterLastStmt(v))
      return false;
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDUafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 1021
    public boolean unassignedAfterLastStmt(Variable v) {
        boolean unassignedAfterLastStmt_Variable_value = unassignedAfterLastStmt_compute(v);
        return unassignedAfterLastStmt_Variable_value;
    }

    private boolean unassignedAfterLastStmt_compute(Variable v) {  return 
    getBlock().isDUafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 1024
    public boolean switchLabelEndsBlock() {
        boolean switchLabelEndsBlock_value = switchLabelEndsBlock_compute();
        return switchLabelEndsBlock_value;
    }

    private boolean switchLabelEndsBlock_compute() {  return 
    getBlock().getNumStmt() > 0 && getBlock().getStmt(getBlock().getNumStmt()-1) instanceof ConstCase;  }

    // Declared in UnreachableStatements.jrag at line 51
    public boolean lastStmtCanCompleteNormally() {
        boolean lastStmtCanCompleteNormally_value = lastStmtCanCompleteNormally_compute();
        return lastStmtCanCompleteNormally_value;
    }

    private boolean lastStmtCanCompleteNormally_compute() {  return  getBlock().canCompleteNormally();  }

    // Declared in UnreachableStatements.jrag at line 53
    public boolean noStmts() {
        boolean noStmts_value = noStmts_compute();
        return noStmts_value;
    }

    private boolean noStmts_compute()  {
    for(int i = 0; i < getBlock().getNumStmt(); i++)
      if(!(getBlock().getStmt(i) instanceof Case))
        return false;
    return true;
  }

    // Declared in UnreachableStatements.jrag at line 60
    public boolean noStmtsAfterLastLabel() {
        boolean noStmtsAfterLastLabel_value = noStmtsAfterLastLabel_compute();
        return noStmtsAfterLastLabel_value;
    }

    private boolean noStmtsAfterLastLabel_compute() {  return  
    getBlock().getNumStmt() > 0 && getBlock().getStmt(getBlock().getNumStmt()-1) instanceof Case;  }

    // Declared in UnreachableStatements.jrag at line 63
    public boolean noDefaultLabel() {
        boolean noDefaultLabel_value = noDefaultLabel_compute();
        return noDefaultLabel_value;
    }

    private boolean noDefaultLabel_compute()  {
    for(int i = 0; i < getBlock().getNumStmt(); i++)
      if(getBlock().getStmt(i) instanceof DefaultCase)
        return false;
    return true;
  }

    // Declared in UnreachableStatements.jrag at line 70
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

    private boolean canCompleteNormally_compute() {  return 
    lastStmtCanCompleteNormally() || noStmts() || noStmtsAfterLastLabel() || noDefaultLabel() || reachableBreak();  }

    // Declared in ControlFlowGraph.jrag at line 16
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  Set.empty().union(getBlock());  }

    protected boolean typeInt_computed = false;
    protected TypeDecl typeInt_value;
    // Declared in LookupType.jrag at line 52
    public TypeDecl typeInt() {
        if(typeInt_computed)
            return typeInt_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeInt_value = getParent().Define_TypeDecl_typeInt(this, null);
        if(isFinal && num == boundariesCrossed)
            typeInt_computed = true;
        return typeInt_value;
    }

    protected boolean typeLong_computed = false;
    protected TypeDecl typeLong_value;
    // Declared in LookupType.jrag at line 54
    public TypeDecl typeLong() {
        if(typeLong_computed)
            return typeLong_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeLong_value = getParent().Define_TypeDecl_typeLong(this, null);
        if(isFinal && num == boundariesCrossed)
            typeLong_computed = true;
        return typeLong_value;
    }

    // Declared in ControlFlowGraph.jrag at line 316
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  following();
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in NameCheck.jrag at line 408
    public Case Define_Case_bind(ASTNode caller, ASTNode child, Case c) {
        if(caller == getBlockNoTransform()) {
    Block b = getBlock();
    for(int i = 0; i < b.getNumStmt(); i++)
      if(b.getStmt(i) instanceof Case && ((Case)b.getStmt(i)).constValue(c))
        return (Case)b.getStmt(i);
    return null;
  }
        return getParent().Define_Case_bind(this, caller, c);
    }

    // Declared in UnreachableStatements.jrag at line 147
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 73
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in TypeCheck.jrag at line 348
    public TypeDecl Define_TypeDecl_switchType(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  getExpr().type();
        }
        return getParent().Define_TypeDecl_switchType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 565
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return  getExpr().isDAafter(v);
        }
        if(caller == getExprNoTransform()) {
    if(((ASTNode)v).isDescendantTo(this))
      return false;
    boolean result = isDAbefore(v);
    return result;
  }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in NameCheck.jrag at line 367
    public boolean Define_boolean_insideSwitch(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_insideSwitch(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 1029
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return  getExpr().isDUafter(v);
        }
        if(caller == getExprNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
