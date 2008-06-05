
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


// Statements


public abstract class Stmt extends ASTNode<ASTNode> implements Cloneable, CFGNode {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        pred_computed = false;
        pred_value = null;
        succ_computed = false;
        succ_value = null;
        uniqueIndex_computed = false;
        following_computed = false;
        following_value = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public Stmt clone() throws CloneNotSupportedException {
        Stmt node = (Stmt)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.pred_computed = false;
        node.pred_value = null;
        node.succ_computed = false;
        node.succ_value = null;
        node.uniqueIndex_computed = false;
        node.following_computed = false;
        node.following_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in UnreachableStatements.jrag at line 14

  void checkUnreachableStmt() {
    if(!reachable() && reportUnreachable())
      error("statement is unreachable");
  }

    // Declared in MyAnalysis.jrag at line 7


  public static int uniqueIndex = 0;

    // Declared in java.ast at line 3
    // Declared in java.ast line 198

    public Stmt() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    protected java.util.Map isDAafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 327
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

    private boolean isDAafter_compute(Variable v) {  return isDAbefore(v);  }

    protected java.util.Map isDUafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 778
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
    throw new Error("isDUafter in " + getClass().getName());
  }

    // Declared in LookupVariable.jrag at line 127
 @SuppressWarnings({"unchecked", "cast"})     public boolean declaresVariable(String name) {
        boolean declaresVariable_String_value = declaresVariable_compute(name);
        return declaresVariable_String_value;
    }

    private boolean declaresVariable_compute(String name) {  return false;  }

    // Declared in NameCheck.jrag at line 396
 @SuppressWarnings({"unchecked", "cast"})     public boolean continueLabel() {
        boolean continueLabel_value = continueLabel_compute();
        return continueLabel_value;
    }

    private boolean continueLabel_compute() {  return false;  }

    protected boolean canCompleteNormally_computed = false;
    protected boolean canCompleteNormally_value;
    // Declared in UnreachableStatements.jrag at line 29
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

    private boolean canCompleteNormally_compute() {  return true;  }

    protected boolean pred_computed = false;
    protected SmallSet pred_value;
    // Declared in ControlFlowGraph.jrag at line 36
 @SuppressWarnings({"unchecked", "cast"})     public SmallSet pred() {
        if(pred_computed)
            return pred_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        pred_value = pred_compute();
        if(isFinal && num == boundariesCrossed)
            pred_computed = true;
        return pred_value;
    }

    private SmallSet pred_compute() {  return SmallSet.empty();  }

    protected boolean succ_computed = false;
    protected SmallSet succ_value;
    // Declared in ControlFlowGraph.jrag at line 57
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

    private SmallSet succ_compute() {  return following();  }

    // Declared in ControlFlowGraph.jrag at line 391
 @SuppressWarnings({"unchecked", "cast"})     public CFGNode targetForContinue() {
        CFGNode targetForContinue_value = targetForContinue_compute();
        return targetForContinue_value;
    }

    private CFGNode targetForContinue_compute() {  return this;  }

    // Declared in MyAnalysis.jrag at line 2
 @SuppressWarnings({"unchecked", "cast"})     public boolean isEmptyStmt() {
        boolean isEmptyStmt_value = isEmptyStmt_compute();
        return isEmptyStmt_value;
    }

    private boolean isEmptyStmt_compute() {  return false;  }

    protected boolean uniqueIndex_computed = false;
    protected int uniqueIndex_value;
    // Declared in MyAnalysis.jrag at line 8
 @SuppressWarnings({"unchecked", "cast"})     public int uniqueIndex() {
        if(uniqueIndex_computed)
            return uniqueIndex_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        uniqueIndex_value = uniqueIndex_compute();
        if(isFinal && num == boundariesCrossed)
            uniqueIndex_computed = true;
        return uniqueIndex_value;
    }

    private int uniqueIndex_compute() {  return uniqueIndex++;  }

    // Declared in MyAnalysis.jrag at line 11
 @SuppressWarnings({"unchecked", "cast"})     public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {
    StringBuffer buf = new StringBuffer();
    buf.append(super.dumpString());
    buf.append(" <** " + uniqueIndex() + " -> {");
    for(Iterator iter = succ().iterator(); iter.hasNext(); ) {
      CFGNode node = (CFGNode)iter.next();
      buf.append(node.uniqueIndex() + " ");
    }
    buf.append("} **>");
    return buf.toString();
  }

    // Declared in DefiniteAssignment.jrag at line 234
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDAbefore(Variable v) {
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        return isDAbefore_Variable_value;
    }

    // Declared in DefiniteAssignment.jrag at line 692
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDUbefore(Variable v) {
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        return isDUbefore_Variable_value;
    }

    // Declared in LookupMethod.jrag at line 24
 @SuppressWarnings({"unchecked", "cast"})     public Collection lookupMethod(String name) {
        Collection lookupMethod_String_value = getParent().Define_Collection_lookupMethod(this, null, name);
        return lookupMethod_String_value;
    }

    // Declared in LookupType.jrag at line 96
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    // Declared in LookupType.jrag at line 174
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet lookupType(String name) {
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        return lookupType_String_value;
    }

    // Declared in LookupVariable.jrag at line 16
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet lookupVariable(String name) {
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        return lookupVariable_String_value;
    }

    // Declared in TypeAnalysis.jrag at line 512
 @SuppressWarnings({"unchecked", "cast"})     public BodyDecl enclosingBodyDecl() {
        BodyDecl enclosingBodyDecl_value = getParent().Define_BodyDecl_enclosingBodyDecl(this, null);
        return enclosingBodyDecl_value;
    }

    // Declared in TypeAnalysis.jrag at line 584
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl hostType() {
        TypeDecl hostType_value = getParent().Define_TypeDecl_hostType(this, null);
        return hostType_value;
    }

    // Declared in UnreachableStatements.jrag at line 27
 @SuppressWarnings({"unchecked", "cast"})     public boolean reachable() {
        boolean reachable_value = getParent().Define_boolean_reachable(this, null);
        return reachable_value;
    }

    // Declared in UnreachableStatements.jrag at line 145
 @SuppressWarnings({"unchecked", "cast"})     public boolean reportUnreachable() {
        boolean reportUnreachable_value = getParent().Define_boolean_reportUnreachable(this, null);
        return reportUnreachable_value;
    }

    // Declared in ControlFlowGraph.jrag at line 22
 @SuppressWarnings({"unchecked", "cast"})     public CFGNode exit() {
        CFGNode exit_value = getParent().Define_CFGNode_exit(this, null);
        return exit_value;
    }

    // Declared in ControlFlowGraph.jrag at line 25
 @SuppressWarnings({"unchecked", "cast"})     public CFGNode entry() {
        CFGNode entry_value = getParent().Define_CFGNode_entry(this, null);
        return entry_value;
    }

    protected boolean following_computed = false;
    protected SmallSet following_value;
    // Declared in ControlFlowGraph.jrag at line 64
 @SuppressWarnings({"unchecked", "cast"})     public SmallSet following() {
        if(following_computed)
            return following_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        following_value = getParent().Define_SmallSet_following(this, null);
        if(isFinal && num == boundariesCrossed)
            following_computed = true;
        return following_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
