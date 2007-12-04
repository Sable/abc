
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

public abstract class BranchTargetStmt extends Stmt implements Cloneable,  BranchPropagation {
    public void flushCache() {
        super.flushCache();
        reachableBreak_computed = false;
        reachableContinue_computed = false;
        targetBranches_computed = false;
        targetBranches_value = null;
        escapedBranches_computed = false;
        escapedBranches_value = null;
        branches_computed = false;
        branches_value = null;
        targetContinues_computed = false;
        targetContinues_value = null;
        targetBreaks_computed = false;
        targetBreaks_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        BranchTargetStmt node = (BranchTargetStmt)super.clone();
        node.reachableBreak_computed = false;
        node.reachableContinue_computed = false;
        node.targetBranches_computed = false;
        node.targetBranches_value = null;
        node.escapedBranches_computed = false;
        node.escapedBranches_value = null;
        node.branches_computed = false;
        node.branches_value = null;
        node.targetContinues_computed = false;
        node.targetContinues_value = null;
        node.targetBreaks_computed = false;
        node.targetBreaks_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 200

    public BranchTargetStmt() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    // Declared in BranchTarget.jrag at line 49

  public void collectBranches(Collection c) {
    c.addAll(escapedBranches());
  }

    // Declared in BranchTarget.jrag at line 148

  public Stmt branchTarget(Stmt branchStmt) {
    if(targetBranches().contains(branchStmt))
      return this;
    return super.branchTarget(branchStmt);
  }

    // Declared in BranchTarget.jrag at line 186

  public void collectFinally(Stmt branchStmt, ArrayList list) {
    if(targetBranches().contains(branchStmt))
      return;
    super.collectFinally(branchStmt, list);
  }

    // Declared in BranchTarget.jrag at line 31
    public abstract boolean targetOf(ContinueStmt stmt);
    // Declared in BranchTarget.jrag at line 32
    public abstract boolean targetOf(BreakStmt stmt);
    protected boolean reachableBreak_computed = false;
    protected boolean reachableBreak_value;
    // Declared in UnreachableStatements.jrag at line 40
    public boolean reachableBreak() {
        if(reachableBreak_computed)
            return reachableBreak_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        reachableBreak_value = reachableBreak_compute();
        if(isFinal && num == boundariesCrossed)
            reachableBreak_computed = true;
        return reachableBreak_value;
    }

    private boolean reachableBreak_compute()  {
    for(Iterator iter = targetBreaks().iterator(); iter.hasNext(); ) {
      BreakStmt stmt = (BreakStmt)iter.next();
      if(stmt.reachable())
        return true;
    }
    return false;
  }

    protected boolean reachableContinue_computed = false;
    protected boolean reachableContinue_value;
    // Declared in UnreachableStatements.jrag at line 82
    public boolean reachableContinue() {
        if(reachableContinue_computed)
            return reachableContinue_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        reachableContinue_value = reachableContinue_compute();
        if(isFinal && num == boundariesCrossed)
            reachableContinue_computed = true;
        return reachableContinue_value;
    }

    private boolean reachableContinue_compute()  {
    for(Iterator iter = targetContinues().iterator(); iter.hasNext(); ) {
      Stmt stmt = (Stmt)iter.next();
      if(stmt.reachable())
        return true;
    }
    return false;
  }

    protected boolean targetBranches_computed = false;
    protected Collection targetBranches_value;
    // Declared in BranchTarget.jrag at line 74
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
    for(Iterator iter = branches().iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof ContinueStmt && targetOf((ContinueStmt)o))
        set.add(o);
      if(o instanceof BreakStmt && targetOf((BreakStmt)o))
        set.add(o);
    }
    return set;
  }

    protected boolean escapedBranches_computed = false;
    protected Collection escapedBranches_value;
    // Declared in BranchTarget.jrag at line 86
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
    for(Iterator iter = branches().iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof ContinueStmt && !targetOf((ContinueStmt)o))
        set.add(o);
      if(o instanceof BreakStmt && !targetOf((BreakStmt)o))
        set.add(o);
      if(o instanceof ReturnStmt)
        set.add(o);
    }
    return set;
  }

    protected boolean branches_computed = false;
    protected Collection branches_value;
    // Declared in BranchTarget.jrag at line 100
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
    super.collectBranches(set);
    return set;
  }

    protected boolean targetContinues_computed = false;
    protected Collection targetContinues_value;
    // Declared in BranchTarget.jrag at line 207
    public Collection targetContinues() {
        if(targetContinues_computed)
            return targetContinues_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        targetContinues_value = targetContinues_compute();
        if(isFinal && num == boundariesCrossed)
            targetContinues_computed = true;
        return targetContinues_value;
    }

    private Collection targetContinues_compute()  {
    HashSet set = new HashSet();
    for(Iterator iter = targetBranches().iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof ContinueStmt)
        set.add(o);
    }
    if(getParent() instanceof LabeledStmt) {
      for(Iterator iter = ((LabeledStmt)getParent()).targetBranches().iterator(); iter.hasNext(); ) {
        Object o = iter.next();
        if(o instanceof ContinueStmt)
          set.add(o);
      }
    }
    return set;
  }

    protected boolean targetBreaks_computed = false;
    protected Collection targetBreaks_value;
    // Declared in BranchTarget.jrag at line 224
    public Collection targetBreaks() {
        if(targetBreaks_computed)
            return targetBreaks_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        targetBreaks_value = targetBreaks_compute();
        if(isFinal && num == boundariesCrossed)
            targetBreaks_computed = true;
        return targetBreaks_value;
    }

    private Collection targetBreaks_compute()  {
    HashSet set = new HashSet();
    for(Iterator iter = targetBranches().iterator(); iter.hasNext(); ) {
      Object o = iter.next();
      if(o instanceof BreakStmt)
        set.add(o);
    }
    return set;
  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
