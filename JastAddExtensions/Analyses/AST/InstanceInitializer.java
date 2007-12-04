
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

public class InstanceInitializer extends BodyDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
        exceptions_computed = false;
        exceptions_value = null;
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        handlesException_TypeDecl_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        InstanceInitializer node = (InstanceInitializer)super.clone();
        node.exceptions_computed = false;
        node.exceptions_value = null;
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.handlesException_TypeDecl_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          InstanceInitializer node = (InstanceInitializer)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        InstanceInitializer res = (InstanceInitializer)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 115


  // Type body decl

  public void toString(StringBuffer s) {
    s.append(indent());
    getBlock().toString(s);
  }

    // Declared in UnreachableStatements.jrag at line 13

  void checkUnreachableStmt() {
    if(!getBlock().canCompleteNormally())
      error("instance initializer in " + hostType().fullName() + " can not complete normally");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 69

    public InstanceInitializer() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 69
    public InstanceInitializer(Block p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 69
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

    protected boolean exceptions_computed = false;
    protected Collection exceptions_value;
    // Declared in AnonymousClasses.jrag at line 116
    public Collection exceptions() {
        if(exceptions_computed)
            return exceptions_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        exceptions_value = exceptions_compute();
        if(isFinal && num == boundariesCrossed)
            exceptions_computed = true;
        return exceptions_value;
    }

    private Collection exceptions_compute()  {
    HashSet set = new HashSet();
    collectExceptions(set, this);
    for(Iterator iter = set.iterator(); iter.hasNext(); ) {
      TypeDecl typeDecl = (TypeDecl)iter.next();
      if(!getBlock().reachedException(typeDecl))
        iter.remove();
    }
    return set;
  }

    // Declared in DefiniteAssignment.jrag at line 283
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

    private boolean isDAafter_compute(Variable v) {  return  getBlock().isDAafter(v);  }

    // Declared in DefiniteAssignment.jrag at line 746
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

    private boolean isDUafter_compute(Variable v) {  return  getBlock().isDUafter(v);  }

    // Declared in ControlFlowGraph.jrag at line 332
    public boolean hasControlFlow() {
        boolean hasControlFlow_value = hasControlFlow_compute();
        return hasControlFlow_value;
    }

    private boolean hasControlFlow_compute() {  return  true;  }

    // Declared in ControlFlowGraph.jrag at line 340
    public Stmt exitBlock() {
        Stmt exitBlock_value = exitBlock_compute();
        return exitBlock_value;
    }

    private Stmt exitBlock_compute() {  return  getBlock();  }

    // Declared in ControlFlowGraph.jrag at line 388
    public Stmt entryBlock() {
        Stmt entryBlock_value = entryBlock_compute();
        return entryBlock_value;
    }

    private Stmt entryBlock_compute()  {
		Block block = getBlock();
		if (block.getNumStmt() > 0) {
			Stmt stmt = block.getStmt(0);
			assert(stmt.isEntryBlock());
			// Hacky solution to deal with first sets in the entryBlock 
			// e.g. ForStmt, DoStmt etc has a first node other than this
			Set first = stmt.first();
			for (Iterator itr = first.iterator(); itr.hasNext(); ) {
				return (Stmt)itr.next();
			}	
		}
		assert(block.isEntryBlock());
		return block;
	}

    protected java.util.Map handlesException_TypeDecl_values;
    // Declared in ExceptionHandling.jrag at line 23
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

    // Declared in TypeAnalysis.jrag at line 582
    public BodyDecl Define_BodyDecl_hostBodyDecl(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  this;
        }
        return getParent().Define_BodyDecl_hostBodyDecl(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 405
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  Set.empty().union(getBlock().exitBlock());
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in NameCheck.jrag at line 231
    public ASTNode Define_ASTNode_enclosingBlock(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  this;
        }
        return getParent().Define_ASTNode_enclosingBlock(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 148
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getBlockNoTransform()) {
    if(hostType().isAnonymous())
      return true;
    if(!exceptionType.isUncheckedException())
      return true;
    for(Iterator iter = hostType().constructors().iterator(); iter.hasNext(); ) {
      ConstructorDecl decl = (ConstructorDecl)iter.next();
      if(!decl.throwsException(exceptionType))
        return false;
    }
    return true;
  }
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in UnreachableStatements.jrag at line 26
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 435
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in TypeHierarchyCheck.jrag at line 131
    public boolean Define_boolean_inStaticContext(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  false;
        }
        return getParent().Define_boolean_inStaticContext(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
