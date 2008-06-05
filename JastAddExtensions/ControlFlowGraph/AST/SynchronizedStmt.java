
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;



public class SynchronizedStmt extends Stmt implements Cloneable, FinallyHost {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        succ_computed = false;
        succ_value = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public SynchronizedStmt clone() throws CloneNotSupportedException {
        SynchronizedStmt node = (SynchronizedStmt)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.succ_computed = false;
        node.succ_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
     @SuppressWarnings({"unchecked", "cast"})  public SynchronizedStmt copy() {
      try {
          SynchronizedStmt node = (SynchronizedStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public SynchronizedStmt fullCopy() {
        SynchronizedStmt res = (SynchronizedStmt)copy();
        for(int i = 0; i < getNumChildNoTransform(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BranchTarget.jrag at line 207

  public void collectFinally(Stmt branchStmt, ArrayList list) {
    list.add(this);
    super.collectFinally(branchStmt, list);
  }

    // Declared in PrettyPrint.jadd at line 705


  public void toString(StringBuffer s) {
    s.append("synchronized(");
    getExpr().toString(s);
    s.append(") ");
    getBlock().toString(s);
  }

    // Declared in TypeCheck.jrag at line 362


  public void typeCheck() {
    TypeDecl type = getExpr().type();
    if(!type.isReferenceType() || type.isNull())
      error("*** The type of the expression must be a reference");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 220

    public SynchronizedStmt() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 220
    public SynchronizedStmt(Expr p0, Block p1) {
        setChild(p0, 0);
        setChild(p1, 1);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 220
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
    // Declared in java.ast line 220
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

    // Declared in DefiniteAssignment.jrag at line 656
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

    private boolean isDAafter_compute(Variable v) {  return getBlock().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 919
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDUafterFinally(Variable v) {
        boolean isDUafterFinally_Variable_value = isDUafterFinally_compute(v);
        return isDUafterFinally_Variable_value;
    }

    private boolean isDUafterFinally_compute(Variable v) {  return true;  }

    // Declared in DefiniteAssignment.jrag at line 922
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDAafterFinally(Variable v) {
        boolean isDAafterFinally_Variable_value = isDAafterFinally_compute(v);
        return isDAafterFinally_Variable_value;
    }

    private boolean isDAafterFinally_compute(Variable v) {  return false;  }

    // Declared in DefiniteAssignment.jrag at line 1182
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

    private boolean isDUafter_compute(Variable v) {  return getBlock().isDUafter(v);  }

    // Declared in UnreachableStatements.jrag at line 110
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

    private boolean canCompleteNormally_compute() {  return getBlock().canCompleteNormally();  }

    // Declared in ControlFlowGraph.jrag at line 354
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

    private SmallSet succ_compute() {  return SmallSet.empty().union(getExpr().first());  }

    // Declared in ControlFlowGraph.jrag at line 356
    public SmallSet Define_SmallSet_following(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return following();
        }
        if(caller == getExprNoTransform()) {
            return SmallSet.empty().union(getBlock());
        }
        return getParent().Define_SmallSet_following(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 155
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 111
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return reachable();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 658
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return getExpr().isDAafter(v);
        }
        if(caller == getExprNoTransform()) {
            return isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 1184
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return getExpr().isDUafter(v);
        }
        if(caller == getExprNoTransform()) {
            return isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
