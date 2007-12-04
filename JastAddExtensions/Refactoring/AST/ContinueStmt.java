
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class ContinueStmt extends Stmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        targetStmt_computed = false;
        targetStmt_value = null;
        finallyList_computed = false;
        finallyList_value = null;
        isDAafter_Variable_values = null;
        isDUafterReachedFinallyBlocks_Variable_values = null;
        isDAafterReachedFinallyBlocks_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        lookupLabel_String_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ContinueStmt node = (ContinueStmt)super.clone();
        node.targetStmt_computed = false;
        node.targetStmt_value = null;
        node.finallyList_computed = false;
        node.finallyList_value = null;
        node.isDAafter_Variable_values = null;
        node.isDUafterReachedFinallyBlocks_Variable_values = null;
        node.isDAafterReachedFinallyBlocks_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.lookupLabel_String_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ContinueStmt node = (ContinueStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ContinueStmt res = (ContinueStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BranchTarget.jrag at line 40

  public void collectBranches(Collection c) {
    c.add(this);
  }

    // Declared in NameCheck.jrag at line 379

  
  public void nameCheck() {
    if(!insideLoop())
      error("continue outside loop");
    else if(hasLabel()) {
      LabeledStmt label = lookupLabel(getLabel());
      if(label == null)
        error("labeled continue must have visible matching label");
      else if(!label.getStmt().continueLabel())
        error(getLabel() + " is not a loop label");
    }
  }

    // Declared in PrettyPrint.jadd at line 818


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("continue ");
    if(hasLabel())
      s.append(getLabel());
    s.append(";\n");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 217

    public ContinueStmt() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 217
    public ContinueStmt(String p0) {
        setLabel(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 217
    private String tokenString_Label;

    // Declared in java.ast at line 3

    public void setLabel(String value) {
        tokenString_Label = value;
    }

    // Declared in java.ast at line 6

    public String getLabel() {
        return tokenString_Label != null ? tokenString_Label : "";
    }

    // Declared in BranchTarget.jrag at line 56
    public boolean hasLabel() {
        boolean hasLabel_value = hasLabel_compute();
        return hasLabel_value;
    }

    private boolean hasLabel_compute() {  return  !getLabel().equals("");  }

    protected boolean targetStmt_computed = false;
    protected Stmt targetStmt_value;
    // Declared in BranchTarget.jrag at line 141
    public Stmt targetStmt() {
        if(targetStmt_computed)
            return targetStmt_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        targetStmt_value = targetStmt_compute();
        if(isFinal && num == boundariesCrossed)
            targetStmt_computed = true;
        return targetStmt_value;
    }

    private Stmt targetStmt_compute() {  return  branchTarget(this);  }

    protected boolean finallyList_computed = false;
    protected ArrayList finallyList_value;
    // Declared in BranchTarget.jrag at line 172
    public ArrayList finallyList() {
        if(finallyList_computed)
            return finallyList_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        finallyList_value = finallyList_compute();
        if(isFinal && num == boundariesCrossed)
            finallyList_computed = true;
        return finallyList_value;
    }

    private ArrayList finallyList_compute()  {
    ArrayList list = new ArrayList();
    collectFinally(this, list);
    return list;
  }

    // Declared in DefiniteAssignment.jrag at line 645
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

    private boolean isDAafter_compute(Variable v) {  return  true;  }

    protected java.util.Map isDUafterReachedFinallyBlocks_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 938
    public boolean isDUafterReachedFinallyBlocks(Variable v) {
        Object _parameters = v;
if(isDUafterReachedFinallyBlocks_Variable_values == null) isDUafterReachedFinallyBlocks_Variable_values = new java.util.HashMap(4);
        if(isDUafterReachedFinallyBlocks_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUafterReachedFinallyBlocks_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUafterReachedFinallyBlocks_Variable_value = isDUafterReachedFinallyBlocks_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDUafterReachedFinallyBlocks_Variable_values.put(_parameters, Boolean.valueOf(isDUafterReachedFinallyBlocks_Variable_value));
        return isDUafterReachedFinallyBlocks_Variable_value;
    }

    private boolean isDUafterReachedFinallyBlocks_compute(Variable v)  {
    if(!isDUbefore(v) && finallyList().isEmpty())
      return false;
    for(Iterator iter = finallyList().iterator(); iter.hasNext(); ) {
      FinallyHost f = (FinallyHost)iter.next();
      if(!f.isDUafterFinally(v))
        return false;
    }
    return true;
  }

    protected java.util.Map isDAafterReachedFinallyBlocks_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 972
    public boolean isDAafterReachedFinallyBlocks(Variable v) {
        Object _parameters = v;
if(isDAafterReachedFinallyBlocks_Variable_values == null) isDAafterReachedFinallyBlocks_Variable_values = new java.util.HashMap(4);
        if(isDAafterReachedFinallyBlocks_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAafterReachedFinallyBlocks_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAafterReachedFinallyBlocks_Variable_value = isDAafterReachedFinallyBlocks_compute(v);
        if(isFinal && num == boundariesCrossed)
            isDAafterReachedFinallyBlocks_Variable_values.put(_parameters, Boolean.valueOf(isDAafterReachedFinallyBlocks_Variable_value));
        return isDAafterReachedFinallyBlocks_Variable_value;
    }

    private boolean isDAafterReachedFinallyBlocks_compute(Variable v)  {
    if(isDAbefore(v))
      return true;
    if(finallyList().isEmpty())
      return false;
    for(Iterator iter = finallyList().iterator(); iter.hasNext(); ) {
      FinallyHost f = (FinallyHost)iter.next();
      if(!f.isDAafterFinally(v))
        return false;
    }
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 1186
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

    private boolean isDUafter_compute(Variable v) {  return  true;  }

    // Declared in UnreachableStatements.jrag at line 97
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

    private boolean canCompleteNormally_compute() {  return  false;  }

    // Declared in ControlFlowGraph.jrag at line 14
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  Set.empty().union(targetStmt());  }

    protected java.util.Map lookupLabel_String_values;
    // Declared in BranchTarget.jrag at line 161
    public LabeledStmt lookupLabel(String name) {
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

    // Declared in NameCheck.jrag at line 356
    public boolean insideLoop() {
        boolean insideLoop_value = getParent().Define_boolean_insideLoop(this, null);
        return insideLoop_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
