
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


public class LabeledStmt extends BranchTargetStmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        targetOf_ContinueStmt_values = null;
        targetOf_BreakStmt_values = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        succ_computed = false;
        succ_value = null;
        lookupLabel_String_values = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public LabeledStmt clone() throws CloneNotSupportedException {
        LabeledStmt node = (LabeledStmt)super.clone();
        node.targetOf_ContinueStmt_values = null;
        node.targetOf_BreakStmt_values = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.succ_computed = false;
        node.succ_value = null;
        node.lookupLabel_String_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
     @SuppressWarnings({"unchecked", "cast"})  public LabeledStmt copy() {
      try {
          LabeledStmt node = (LabeledStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public LabeledStmt fullCopy() {
        LabeledStmt res = (LabeledStmt)copy();
        for(int i = 0; i < getNumChildNoTransform(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NameCheck.jrag at line 351

  
  public void nameCheck() {
    LabeledStmt stmt = lookupLabel(getLabel());
    if(stmt != null) {
      if(stmt.enclosingBodyDecl() == enclosingBodyDecl()) {
        error("Labels can not shadow labels in the same member");
      }
    }
  }

    // Declared in PrettyPrint.jadd at line 558


  public void toString(StringBuffer s) {
    s.append(getLabel() + ":\n");
    s.append(indent());
    getStmt().toString(s);
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 202

    public LabeledStmt() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 202
    public LabeledStmt(String p0, Stmt p1) {
        setLabel(p0);
        setChild(p1, 0);
    }

    // Declared in java.ast at line 16


    // Declared in java.ast line 202
    public LabeledStmt(beaver.Symbol p0, Stmt p1) {
        setLabel(p0);
        setChild(p1, 0);
    }

    // Declared in java.ast at line 21


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 24

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 202
    protected String tokenString_Label;

    // Declared in java.ast at line 3

    public void setLabel(String value) {
        tokenString_Label = value;
    }

    // Declared in java.ast at line 6

    public int Labelstart;

    // Declared in java.ast at line 7

    public int Labelend;

    // Declared in java.ast at line 8

    public void setLabel(beaver.Symbol symbol) {
        if(symbol.value != null && !(symbol.value instanceof String))
          throw new UnsupportedOperationException("setLabel is only valid for String lexemes");
        tokenString_Label = (String)symbol.value;
        Labelstart = symbol.getStart();
        Labelend = symbol.getEnd();
    }

    // Declared in java.ast at line 15

    public String getLabel() {
        return tokenString_Label != null ? tokenString_Label : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 202
    public void setStmt(Stmt node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Stmt getStmt() {
        return (Stmt)getChild(0);
    }

    // Declared in java.ast at line 9


    public Stmt getStmtNoTransform() {
        return (Stmt)getChildNoTransform(0);
    }

    protected java.util.Map targetOf_ContinueStmt_values;
    // Declared in BranchTarget.jrag at line 68
 @SuppressWarnings({"unchecked", "cast"})     public boolean targetOf(ContinueStmt stmt) {
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

    private boolean targetOf_compute(ContinueStmt stmt) {  return stmt.hasLabel() && stmt.getLabel().equals(getLabel());  }

    protected java.util.Map targetOf_BreakStmt_values;
    // Declared in BranchTarget.jrag at line 75
 @SuppressWarnings({"unchecked", "cast"})     public boolean targetOf(BreakStmt stmt) {
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

    private boolean targetOf_compute(BreakStmt stmt) {  return stmt.hasLabel() && stmt.getLabel().equals(getLabel());  }

    // Declared in DefiniteAssignment.jrag at line 512
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDAafter(Variable v) {
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

    private boolean isDAafter_compute(Variable v) {
    if(!getStmt().isDAafter(v))
      return false;
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDAafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 898
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDUafter(Variable v) {
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

    private boolean isDUafter_compute(Variable v) {
    if(!getStmt().isDUafter(v))
      return false;
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(!stmt.isDUafterReachedFinallyBlocks(v))
        return false;
    }
    return true;
  }

    // Declared in UnreachableStatements.jrag at line 46
 @SuppressWarnings({"unchecked", "cast"})     public boolean canCompleteNormally() {
        if(canCompleteNormally_computed)
            return canCompleteNormally_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        canCompleteNormally_value = canCompleteNormally_compute();
        if(isFinal && num == boundariesCrossed)
            canCompleteNormally_computed = true;
        return canCompleteNormally_value;
    }

    private boolean canCompleteNormally_compute() {  return getStmt().canCompleteNormally() || reachableBreak();  }

    // Declared in ControlFlowGraph.jrag at line 350
 @SuppressWarnings({"unchecked", "cast"})     public SmallSet succ() {
        if(succ_computed)
            return succ_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        succ_value = succ_compute();
        if(isFinal && num == boundariesCrossed)
            succ_computed = true;
        return succ_value;
    }

    private SmallSet succ_compute() {  return SmallSet.empty().union(getStmt());  }

    // Declared in ControlFlowGraph.jrag at line 397
 @SuppressWarnings({"unchecked", "cast"})     public CFGNode targetForContinue() {
        CFGNode targetForContinue_value = targetForContinue_compute();
        return targetForContinue_value;
    }

    private CFGNode targetForContinue_compute() {  return getStmt().targetForContinue();  }

    protected java.util.Map lookupLabel_String_values;
    // Declared in BranchTarget.jrag at line 171
 @SuppressWarnings({"unchecked", "cast"})     public LabeledStmt lookupLabel(String name) {
        Object _parameters = name;
if(lookupLabel_String_values == null) lookupLabel_String_values = new java.util.HashMap(4);
        if(lookupLabel_String_values.containsKey(_parameters))
            return (LabeledStmt)lookupLabel_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        LabeledStmt lookupLabel_String_value = getParent().Define_LabeledStmt_lookupLabel(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupLabel_String_values.put(_parameters, lookupLabel_String_value);
        return lookupLabel_String_value;
    }

    // Declared in BranchTarget.jrag at line 172
    public LabeledStmt Define_LabeledStmt_lookupLabel(ASTNode caller, ASTNode child, String name) {
        if(caller == getStmtNoTransform()) {
            return name.equals(getLabel()) ? this : lookupLabel(name);
        }
        return getParent().Define_LabeledStmt_lookupLabel(this, caller, name);
    }

    // Declared in ControlFlowGraph.jrag at line 351
    public SmallSet Define_SmallSet_following(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return following();
        }
        return getParent().Define_SmallSet_following(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 47
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getStmtNoTransform()) {
            return reachable();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 511
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getStmtNoTransform()) {
            return isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 897
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getStmtNoTransform()) {
            return isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
