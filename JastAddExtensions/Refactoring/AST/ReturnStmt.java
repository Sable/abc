
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

public class ReturnStmt extends Stmt implements Cloneable {
    public void flushCache() {
        super.flushCache();
        finallyList_computed = false;
        finallyList_value = null;
        isDAafter_Variable_values = null;
        isDUafterReachedFinallyBlocks_Variable_values = null;
        isDAafterReachedFinallyBlocks_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
    }
    public Object clone() throws CloneNotSupportedException {
        ReturnStmt node = (ReturnStmt)super.clone();
        node.finallyList_computed = false;
        node.finallyList_value = null;
        node.isDAafter_Variable_values = null;
        node.isDUafterReachedFinallyBlocks_Variable_values = null;
        node.isDAafterReachedFinallyBlocks_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ReturnStmt node = (ReturnStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ReturnStmt res = (ReturnStmt)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in BranchTarget.jrag at line 46

  public void collectBranches(Collection c) {
    c.add(this);
  }

    // Declared in NodeConstructors.jrag at line 57


  public ReturnStmt(Expr expr) {
    this(new Opt(expr));
  }

    // Declared in PrettyPrint.jadd at line 826


  public void toString(StringBuffer s) {
    super.toString(s);
    s.append("return ");
    if(hasResult()) {
      getResult().toString(s);
    }
    s.append(";\n");
  }

    // Declared in TypeCheck.jrag at line 397


  public void typeCheck() {
    if(hasResult() && !returnType().isVoid()) {
      if(!getResult().type().assignConversionTo(returnType(), getResult()))
        error("return value must be an instance of " + returnType().typeName() + " which " + getResult().type().typeName() + " is not");
    }
    // 8.4.5 8.8.5
    if(returnType().isVoid() && hasResult())
      error("return stmt may not have an expression in void methods");
    // 8.4.5
    if(!returnType().isVoid() && !hasResult())
      error("return stmt must have an expression in non void methods");
    if(hostBodyDecl() instanceof InstanceInitializer || hostBodyDecl() instanceof StaticInitializer)
      error("Initializers may not return");

  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 218

    public ReturnStmt() {
        super();

        setChild(new Opt(), 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 218
    public ReturnStmt(Opt p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 218
    public void setResultOpt(Opt opt) {
        setChild(opt, 0);
    }

    // Declared in java.ast at line 6


    public boolean hasResult() {
        return getResultOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Expr getResult() {
        return (Expr)getResultOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setResult(Expr node) {
        getResultOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getResultOpt() {
        return (Opt)getChild(0);
    }

    // Declared in java.ast at line 21


    public Opt getResultOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }

    protected boolean finallyList_computed = false;
    protected ArrayList finallyList_value;
    // Declared in BranchTarget.jrag at line 177
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

    // Declared in DefiniteAssignment.jrag at line 646
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
    // Declared in DefiniteAssignment.jrag at line 948
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
    // Declared in DefiniteAssignment.jrag at line 984
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
    if(hasResult() ? getResult().isDAafter(v) : isDAbefore(v))
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

    // Declared in DefiniteAssignment.jrag at line 1187
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

    // Declared in UnreachableStatements.jrag at line 98
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

    // Declared in ControlFlowGraph.jrag at line 50
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute()  {
		// -- Old impl --
		// eq ReturnStmt.succ() = Set.empty().union(exitBlock());
		// 

		// Search for finally .. finally. 
		// When no enclosing try-catch-finally take the exit block
		TryStmt tryStmt = enclosingTryStmt();
		while (tryStmt != null) {
			if (tryStmt.hasFinally()) {
				return Set.empty().union(enclosingFinally());
			}
			tryStmt = tryStmt.enclosingTryStmt();
		}		

		return Set.empty().union(exitBlock());
	}

    // Declared in ControlFlowGraph.jrag at line 98
    public boolean containsReturn() {
        boolean containsReturn_value = containsReturn_compute();
        return containsReturn_value;
    }

    private boolean containsReturn_compute() {  return  true;  }

    // Declared in TypeCheck.jrag at line 392
    public TypeDecl returnType() {
        TypeDecl returnType_value = getParent().Define_TypeDecl_returnType(this, null);
        return returnType_value;
    }

    // Declared in ControlFlowGraph.jrag at line 146
    public TryStmt enclosingTryStmt() {
        TryStmt enclosingTryStmt_value = getParent().Define_TryStmt_enclosingTryStmt(this, null);
        return enclosingTryStmt_value;
    }

    // Declared in DefiniteAssignment.jrag at line 649
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getResultOptNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in DefiniteAssignment.jrag at line 1190
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getResultOptNoTransform()) {
            return  isDUbefore(v);
        }
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
