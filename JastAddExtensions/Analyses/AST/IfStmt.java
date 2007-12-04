
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;


public class IfStmt extends Stmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
    }
    public Object clone() throws CloneNotSupportedException {
        IfStmt node = (IfStmt)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          IfStmt node = (IfStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        IfStmt res = (IfStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NodeConstructors.jrag at line 61


  public IfStmt(Expr cond, Stmt thenBranch) {
    this(cond, thenBranch, new Opt());
  }

    // Declared in NodeConstructors.jrag at line 65


  public IfStmt(Expr cond, Stmt thenBranch, Stmt elseBranch) {
    this(cond, thenBranch, new Opt(elseBranch));
  }

    // Declared in PrettyPrint.jadd at line 731


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("if(");
    getCondition().toString(s);
    s.append(") ");
    getThen().toString(s);
    if(hasElse()) {
      s.append(indent());
      s.append("else ");
      getElse().toString(s);
    }
  }

    // Declared in TypeCheck.jrag at line 305


  public void typeCheck() {
    TypeDecl cond = getCondition().type();
    if(!cond.isBoolean()) {
      error("the type of \"" + getCondition() + "\" is " + cond.name() + " which is not boolean");
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 211

    public IfStmt() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(new Opt(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 211
    public IfStmt(Expr p0, Stmt p1, Opt p2) {
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
    // Declared in java.ast line 211
    public void setCondition(Expr node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Expr getCondition() {
        return (Expr)getChild(0);
    }

    // Declared in java.ast at line 9


    public Expr getConditionNoTransform() {
        return (Expr)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 211
    public void setThen(Stmt node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Stmt getThen() {
        return (Stmt)getChild(1);
    }

    // Declared in java.ast at line 9


    public Stmt getThenNoTransform() {
        return (Stmt)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 211
    public void setElseOpt(Opt opt) {
        setChild(opt, 2);
    }

    // Declared in java.ast at line 6


    public boolean hasElse() {
        return getElseOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Stmt getElse() {
        return (Stmt)getElseOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setElse(Stmt node) {
        getElseOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getElseOpt() {
        return (Opt)getChild(2);
    }

    // Declared in java.ast at line 21


    public Opt getElseOptNoTransform() {
        return (Opt)getChildNoTransform(2);
    }

    // Declared in DefiniteAssignment.jrag at line 522
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

    private boolean isDAafter_compute(Variable v) {  return  hasElse() ? getThen().isDAafter(v) && getElse().isDAafter(v) : getThen().isDAafter(v) && getCondition().isDAafterFalse(v);  }

    // Declared in DefiniteAssignment.jrag at line 1000
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

    private boolean isDUafter_compute(Variable v) {  return  hasElse() ? getThen().isDUafter(v) && getElse().isDUafter(v) : getThen().isDUafter(v) && getCondition().isDUafterFalse(v);  }

    // Declared in UnreachableStatements.jrag at line 130
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

    private boolean canCompleteNormally_compute() {  return  (reachable() && !hasElse()) || (getThen().canCompleteNormally() ||
    (hasElse() && getElse().canCompleteNormally()));  }

    // Declared in ControlFlowGraph.jrag at line 8
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  hasElse() ?	Set.empty().union(getThen()).union(getElse()) :
						following().union(getThen());  }

    // Declared in ControlFlowGraph.jrag at line 79
    public boolean hasCondBranch() {
        boolean hasCondBranch_value = hasCondBranch_compute();
        return hasCondBranch_value;
    }

    private boolean hasCondBranch_compute() {  return  true;  }

    // Declared in UnreachableStatements.jrag at line 139
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        if(caller == getElseOptNoTransform()) {
            return  reachable();
        }
        if(caller == getThenNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 133
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getElseOptNoTransform()) {
            return  reachable();
        }
        if(caller == getThenNoTransform()) {
            return  reachable();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 525
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getElseOptNoTransform()) {
            return  getCondition().isDAafterFalse(v);
        }
        if(caller == getThenNoTransform()) {
            return  getCondition().isDAafterTrue(v);
        }
        if(caller == getConditionNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 1003
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getElseOptNoTransform()) {
            return  getCondition().isDUafterFalse(v);
        }
        if(caller == getThenNoTransform()) {
            return  getCondition().isDUafterTrue(v);
        }
        if(caller == getConditionNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
