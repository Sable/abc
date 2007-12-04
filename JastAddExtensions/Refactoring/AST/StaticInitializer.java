
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class StaticInitializer extends BodyDecl implements Cloneable,  Initializer {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        handlesException_TypeDecl_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        StaticInitializer node = (StaticInitializer)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.handlesException_TypeDecl_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          StaticInitializer node = (StaticInitializer)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        StaticInitializer res = (StaticInitializer)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in Modifiers.jrag at line 168


  // 8.1.2
  public void checkModifiers() {
    super.checkModifiers();
    if(hostType().isInnerClass())
      error("*** Inner classes may not declare static initializers");
  }

    // Declared in PrettyPrint.jadd at line 120


  public void toString(StringBuffer s) {
    s.append(indent());
    s.append("static ");
    getBlock().toString(s);
  }

    // Declared in UnreachableStatements.jrag at line 9

  void checkUnreachableStmt() {
    if(!getBlock().canCompleteNormally())
      error("static initializer in " + hostType().fullName() + " can not complete normally");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 70

    public StaticInitializer() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 70
    public StaticInitializer(Block p0) {
        setChild(p0, 0);
    }

    // Declared in java.ast at line 15


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 18

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 70
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

    // Declared in DefiniteAssignment.jrag at line 284
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

    // Declared in DefiniteAssignment.jrag at line 747
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

    // Declared in ControlFlowGraph.jrag at line 333
    public boolean hasControlFlow() {
        boolean hasControlFlow_value = hasControlFlow_compute();
        return hasControlFlow_value;
    }

    private boolean hasControlFlow_compute() {  return  true;  }

    // Declared in ControlFlowGraph.jrag at line 339
    public Stmt exitBlock() {
        Stmt exitBlock_value = exitBlock_compute();
        return exitBlock_value;
    }

    private Stmt exitBlock_compute() {  return  getBlock();  }

    // Declared in ControlFlowGraph.jrag at line 373
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
    // Declared in ExceptionHandling.jrag at line 24
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

    // Declared in TypeAnalysis.jrag at line 583
    public BodyDecl Define_BodyDecl_hostBodyDecl(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  this;
        }
        return getParent().Define_BodyDecl_hostBodyDecl(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 406
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  Set.empty().union(getBlock().exitBlock());
        }
        return getParent().Define_Set_following(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 161
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getBlockNoTransform()) {
            return 
    hostType().isAnonymous() ? handlesException(exceptionType) : !exceptionType.isUncheckedException();
        }
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in UnreachableStatements.jrag at line 25
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in TypeCheck.jrag at line 427
    public TypeDecl Define_TypeDecl_enclosingInstance(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  null;
        }
        return getParent().Define_TypeDecl_enclosingInstance(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 436
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        if(caller == getBlockNoTransform()) {
            return  isDAbefore(v);
        }
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }

    // Declared in TypeHierarchyCheck.jrag at line 130
    public boolean Define_boolean_inStaticContext(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_inStaticContext(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
