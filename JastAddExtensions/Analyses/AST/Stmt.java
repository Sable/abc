
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


// Statements

public abstract class Stmt extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        canCompleteNormally_computed = false;
        dominates_Stmt_Stmt_values = null;
        dominates_Stmt_values = null;
        post_dominates_Stmt_values = null;
        gsucc_Block_int_int_values = null;
        hostBlock_computed = false;
        hostBlock_value = null;
        Stmt_pred_visited = false;
        Stmt_pred_computed = false;
        Stmt_pred_value = null;
    Stmt_pred_contributors = new java.util.HashSet();
    }
    public Object clone() throws CloneNotSupportedException {
        Stmt node = (Stmt)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.canCompleteNormally_computed = false;
        node.dominates_Stmt_Stmt_values = null;
        node.dominates_Stmt_values = null;
        node.post_dominates_Stmt_values = null;
        node.gsucc_Block_int_int_values = null;
        node.hostBlock_computed = false;
        node.hostBlock_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in PrettyPrint.jadd at line 676



  // Stmts

  public void toString(StringBuffer s) {
    super.toString(s);
  }

    // Declared in UnreachableStatements.jrag at line 5

  void checkUnreachableStmt() {
    if(!reachable() && reportUnreachable())
      error("statement is unreachable");
  }

    // Declared in ExtractMethod.jrag at line 34

	
	public int indexInHostBlock() {
		return indexInBlock(hostBlock());
	}

    // Declared in ExtractMethod.jrag at line 38

	
	public int indexInBlock(Block blk) {
		return indexIn(blk.getStmtList());
	}

    // Declared in GuardedControlFlow.jrag at line 14

	
	public Set gsucc(Block blk) {
		return gsucc(blk, -1, Integer.MAX_VALUE);
	}

    // Declared in GuardedControlFlow.jrag at line 18

	
	public Set gsucc(Stmt begin, Stmt end) { 
		return gsucc(hostBlock(), 
					 begin.indexInBlock(hostBlock()), 
					 end.indexInBlock(hostBlock()));
	}

    // Declared in java.ast at line 3
    // Declared in java.ast line 199

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
    // Declared in DefiniteAssignment.jrag at line 316
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

    private boolean isDAafter_compute(Variable v) {  return  isDAbefore(v);  }

    protected java.util.Map isDUafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 774
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
    throw new Error("isDUafter in " + getClass().getName());
  }

    // Declared in LookupVariable.jrag at line 130
    public boolean declaresVariable(String name) {
        boolean declaresVariable_String_value = declaresVariable_compute(name);
        return declaresVariable_String_value;
    }

    private boolean declaresVariable_compute(String name) {  return  false;  }

    // Declared in NameCheck.jrag at line 391
    public boolean continueLabel() {
        boolean continueLabel_value = continueLabel_compute();
        return continueLabel_value;
    }

    private boolean continueLabel_compute() {  return  false;  }

    protected boolean canCompleteNormally_computed = false;
    protected boolean canCompleteNormally_value;
    // Declared in UnreachableStatements.jrag at line 20
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

    private boolean canCompleteNormally_compute() {  return  true;  }

    // Declared in ControlFlowGraph.jrag at line 6
    public Set succ() {
        Set succ_value = succ_compute();
        return succ_value;
    }

    private Set succ_compute() {  return  following();  }

    // Declared in ControlFlowGraph.jrag at line 78
    public boolean hasCondBranch() {
        boolean hasCondBranch_value = hasCondBranch_compute();
        return hasCondBranch_value;
    }

    private boolean hasCondBranch_compute() {  return  false;  }

    // Declared in ControlFlowGraph.jrag at line 89
    public boolean containsReturn() {
        boolean containsReturn_value = containsReturn_compute();
        return containsReturn_value;
    }

    private boolean containsReturn_compute() {  return  false;  }

    // Declared in ControlFlowGraph.jrag at line 163
    public boolean hasPathBranch() {
        boolean hasPathBranch_value = hasPathBranch_compute();
        return hasPathBranch_value;
    }

    private boolean hasPathBranch_compute() {  return  false;  }

    // Declared in ControlFlowGraph.jrag at line 322
    public Set first() {
        Set first_value = first_compute();
        return first_value;
    }

    private Set first_compute() {  return  Set.empty().union(this);  }

    // Declared in ControlFlowGraph.jrag at line 409
    public boolean isEntryBlock() {
        boolean isEntryBlock_value = isEntryBlock_compute();
        return isEntryBlock_value;
    }

    private boolean isEntryBlock_compute() {  return  this == entryBlock();  }

    // Declared in ControlFlowGraph.jrag at line 410
    public boolean isExitBlock() {
        boolean isExitBlock_value = isExitBlock_compute();
        return isExitBlock_value;
    }

    private boolean isExitBlock_compute() {  return  this == exitBlock();  }

    protected java.util.Set dominates_Stmt_Stmt_visited;
    protected java.util.Set dominates_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set dominates_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map dominates_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean dominates(Stmt halfway, Stmt goal) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(halfway);
        _parameters.add(goal);
if(dominates_Stmt_Stmt_visited == null) dominates_Stmt_Stmt_visited = new java.util.HashSet(4);
if(dominates_Stmt_Stmt_values == null) dominates_Stmt_Stmt_values = new java.util.HashMap(4);
        if(dominates_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)dominates_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!dominates_Stmt_Stmt_initialized.contains(_parameters)) {
            dominates_Stmt_Stmt_initialized.add(_parameters);
            dominates_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(true));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            dominates_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_dominates_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_dominates_Stmt_Stmt_value = dominates_compute(halfway, goal);
                if (new_dominates_Stmt_Stmt_value!=((Boolean)dominates_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                dominates_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_dominates_Stmt_Stmt_value));
            } while (CHANGE);
            dominates_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            dominates_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            dominates_compute(halfway, goal);
            RESET_CYCLE = false;
            dominates_Stmt_Stmt_computed.remove(_parameters);
            dominates_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_dominates_Stmt_Stmt_value;
        }
        if(!dominates_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                dominates_Stmt_Stmt_computed.remove(_parameters);
                dominates_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)dominates_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            dominates_Stmt_Stmt_visited.add(_parameters);
            boolean new_dominates_Stmt_Stmt_value = dominates_compute(halfway, goal);
            if (new_dominates_Stmt_Stmt_value!=((Boolean)dominates_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            dominates_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_dominates_Stmt_Stmt_value));
            dominates_Stmt_Stmt_visited.remove(_parameters);
            return new_dominates_Stmt_Stmt_value;
        }
        return ((Boolean)dominates_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean dominates_compute(Stmt halfway, Stmt goal)  {
		if(this == halfway)
			return true;
		if(this == goal)
			return false;
		for(Iterator i=gsucc(hostBlock()).iterator();i.hasNext();) {
			Stmt next = (Stmt)i.next();
			if(!next.dominates(halfway, goal))
				return false;
		}
		return true;
	}

    protected java.util.Map dominates_Stmt_values;
    // Declared in Domination.jrag at line 17
    public boolean dominates(Stmt goal) {
        Object _parameters = goal;
if(dominates_Stmt_values == null) dominates_Stmt_values = new java.util.HashMap(4);
        if(dominates_Stmt_values.containsKey(_parameters))
            return ((Boolean)dominates_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean dominates_Stmt_value = dominates_compute(goal);
        if(isFinal && num == boundariesCrossed)
            dominates_Stmt_values.put(_parameters, Boolean.valueOf(dominates_Stmt_value));
        return dominates_Stmt_value;
    }

    private boolean dominates_compute(Stmt goal)  {
		Block host = hostBlock();
		if(host == null) return false;
		// the first() set is always a singleton, but we don't rely on this
		for(Iterator i=host.first().iterator();i.hasNext();)
			if(!((Stmt)i.next()).dominates(this, goal))
				return false;
		return true;
	}

    protected java.util.Map post_dominates_Stmt_values;
    // Declared in Domination.jrag at line 27
    public boolean post_dominates(Stmt origin) {
        Object _parameters = origin;
if(post_dominates_Stmt_values == null) post_dominates_Stmt_values = new java.util.HashMap(4);
        if(post_dominates_Stmt_values.containsKey(_parameters))
            return ((Boolean)post_dominates_Stmt_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean post_dominates_Stmt_value = post_dominates_compute(origin);
        if(isFinal && num == boundariesCrossed)
            post_dominates_Stmt_values.put(_parameters, Boolean.valueOf(post_dominates_Stmt_value));
        return post_dominates_Stmt_value;
    }

    private boolean post_dominates_compute(Stmt origin)  {
		Block host = hostBlock();
		if(host == null) return false;
		if(host.getNumStmt() == 0) return false;  // shouldn't happen!
		for(Iterator i=host.exitsAfter(host.getStmt(0)).iterator();i.hasNext();)
			if(!origin.dominates(this, (Stmt)i.next()))
				return false;
		return true;
	}

    protected java.util.Map gsucc_Block_int_int_values;
    // Declared in GuardedControlFlow.jrag at line 5
    public Set gsucc(Block blk, int start, int end) {
        java.util.List _parameters = new java.util.ArrayList(3);
        _parameters.add(blk);
        _parameters.add(new Integer(start));
        _parameters.add(new Integer(end));
if(gsucc_Block_int_int_values == null) gsucc_Block_int_int_values = new java.util.HashMap(4);
        if(gsucc_Block_int_int_values.containsKey(_parameters))
            return (Set)gsucc_Block_int_int_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Set gsucc_Block_int_int_value = gsucc_compute(blk, start, end);
        if(isFinal && num == boundariesCrossed)
            gsucc_Block_int_int_values.put(_parameters, gsucc_Block_int_int_value);
        return gsucc_Block_int_int_value;
    }

    private Set gsucc_compute(Block blk, int start, int end) {  return  succ();  }

    protected java.util.Set between_Stmt_Stmt_visited;
    protected java.util.Set between_Stmt_Stmt_computed = new java.util.HashSet(4);
    protected java.util.Set between_Stmt_Stmt_initialized = new java.util.HashSet(4);
    protected java.util.Map between_Stmt_Stmt_values = new java.util.HashMap(4);
    public boolean between(Stmt begin, Stmt end) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(begin);
        _parameters.add(end);
if(between_Stmt_Stmt_visited == null) between_Stmt_Stmt_visited = new java.util.HashSet(4);
if(between_Stmt_Stmt_values == null) between_Stmt_Stmt_values = new java.util.HashMap(4);
        if(between_Stmt_Stmt_computed.contains(_parameters))
            return ((Boolean)between_Stmt_Stmt_values.get(_parameters)).booleanValue();
        if (!between_Stmt_Stmt_initialized.contains(_parameters)) {
            between_Stmt_Stmt_initialized.add(_parameters);
            between_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(false));
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            between_Stmt_Stmt_visited.add(_parameters);
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            boolean new_between_Stmt_Stmt_value;
            do {
                CHANGE = false;
                new_between_Stmt_Stmt_value = between_compute(begin, end);
                if (new_between_Stmt_Stmt_value!=((Boolean)between_Stmt_Stmt_values.get(_parameters)).booleanValue())
                    CHANGE = true;
                between_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_between_Stmt_Stmt_value));
            } while (CHANGE);
            between_Stmt_Stmt_visited.remove(_parameters);
            if(isFinal && num == boundariesCrossed)
{
            between_Stmt_Stmt_computed.add(_parameters);
            }
            else {
            RESET_CYCLE = true;
            between_compute(begin, end);
            RESET_CYCLE = false;
            between_Stmt_Stmt_computed.remove(_parameters);
            between_Stmt_Stmt_initialized.remove(_parameters);
            }
            IN_CIRCLE = false; 
            return new_between_Stmt_Stmt_value;
        }
        if(!between_Stmt_Stmt_visited.contains(_parameters)) {
            if (RESET_CYCLE) {
                between_Stmt_Stmt_computed.remove(_parameters);
                between_Stmt_Stmt_initialized.remove(_parameters);
                return ((Boolean)between_Stmt_Stmt_values.get(_parameters)).booleanValue();
            }
            between_Stmt_Stmt_visited.add(_parameters);
            boolean new_between_Stmt_Stmt_value = between_compute(begin, end);
            if (new_between_Stmt_Stmt_value!=((Boolean)between_Stmt_Stmt_values.get(_parameters)).booleanValue())
                CHANGE = true;
            between_Stmt_Stmt_values.put(_parameters, Boolean.valueOf(new_between_Stmt_Stmt_value));
            between_Stmt_Stmt_visited.remove(_parameters);
            return new_between_Stmt_Stmt_value;
        }
        return ((Boolean)between_Stmt_Stmt_values.get(_parameters)).booleanValue();
    }

    private boolean between_compute(Stmt begin, Stmt end)  {
		Block blk = begin.hostBlock();
		return between(blk, 
					   begin.indexInBlock(blk), end.indexInBlock(blk));
	}

    // Declared in DefiniteAssignment.jrag at line 223
    public boolean isDAbefore(Variable v) {
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        return isDAbefore_Variable_value;
    }

    // Declared in DefiniteAssignment.jrag at line 688
    public boolean isDUbefore(Variable v) {
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        return isDUbefore_Variable_value;
    }

    // Declared in LookupType.jrag at line 86
    public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    // Declared in LookupVariable.jrag at line 7
    public SimpleSet lookupVariable(String name) {
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        return lookupVariable_String_value;
    }

    // Declared in TypeAnalysis.jrag at line 580
    public BodyDecl hostBodyDecl() {
        BodyDecl hostBodyDecl_value = getParent().Define_BodyDecl_hostBodyDecl(this, null);
        return hostBodyDecl_value;
    }

    // Declared in UnreachableStatements.jrag at line 18
    public boolean reachable() {
        boolean reachable_value = getParent().Define_boolean_reachable(this, null);
        return reachable_value;
    }

    // Declared in UnreachableStatements.jrag at line 136
    public boolean reportUnreachable() {
        boolean reportUnreachable_value = getParent().Define_boolean_reportUnreachable(this, null);
        return reportUnreachable_value;
    }

    // Declared in ControlFlowGraph.jrag at line 153
    public boolean hasEnclosingTryStmt() {
        boolean hasEnclosingTryStmt_value = getParent().Define_boolean_hasEnclosingTryStmt(this, null);
        return hasEnclosingTryStmt_value;
    }

    // Declared in ControlFlowGraph.jrag at line 157
    public boolean isFinallyBlock() {
        boolean isFinallyBlock_value = getParent().Define_boolean_isFinallyBlock(this, null);
        return isFinallyBlock_value;
    }

    // Declared in ControlFlowGraph.jrag at line 166
    public Set enclosingFinally() {
        Set enclosingFinally_value = getParent().Define_Set_enclosingFinally(this, null);
        return enclosingFinally_value;
    }

    // Declared in ControlFlowGraph.jrag at line 174
    public boolean withInCatchClause() {
        boolean withInCatchClause_value = getParent().Define_boolean_withInCatchClause(this, null);
        return withInCatchClause_value;
    }

    // Declared in ControlFlowGraph.jrag at line 205
    public ConstCase followingConstCase() {
        ConstCase followingConstCase_value = getParent().Define_ConstCase_followingConstCase(this, null);
        return followingConstCase_value;
    }

    // Declared in ControlFlowGraph.jrag at line 220
    public DefaultCase followingDefaultCase() {
        DefaultCase followingDefaultCase_value = getParent().Define_DefaultCase_followingDefaultCase(this, null);
        return followingDefaultCase_value;
    }

    // Declared in ControlFlowGraph.jrag at line 237
    public Set following() {
        Set following_value = getParent().Define_Set_following(this, null);
        return following_value;
    }

    // Declared in ControlFlowGraph.jrag at line 412
    public Stmt exitBlock() {
        Stmt exitBlock_value = getParent().Define_Stmt_exitBlock(this, null);
        return exitBlock_value;
    }

    // Declared in ControlFlowGraph.jrag at line 415
    public Stmt entryBlock() {
        Stmt entryBlock_value = getParent().Define_Stmt_entryBlock(this, null);
        return entryBlock_value;
    }

    protected boolean hostBlock_computed = false;
    protected Block hostBlock_value;
    // Declared in Domination.jrag at line 50
    public Block hostBlock() {
        if(hostBlock_computed)
            return hostBlock_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        hostBlock_value = getParent().Define_Block_hostBlock(this, null);
        if(isFinal && num == boundariesCrossed)
            hostBlock_computed = true;
        return hostBlock_value;
    }

    // Declared in Domination.jrag at line 69
    public boolean isInitOrUpdateStmt() {
        boolean isInitOrUpdateStmt_value = getParent().Define_boolean_isInitOrUpdateStmt(this, null);
        return isInitOrUpdateStmt_value;
    }

    // Declared in ExtractMethod.jrag at line 164
    public Collection visibleLocalDecls() {
        Collection visibleLocalDecls_value = getParent().Define_Collection_visibleLocalDecls(this, null);
        return visibleLocalDecls_value;
    }

    // Declared in GuardedControlFlow.jrag at line 24
    public boolean between(Block blk, int start, int end) {
        boolean between_Block_int_int_value = getParent().Define_boolean_between(this, null, blk, start, end);
        return between_Block_int_int_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected boolean Stmt_pred_visited = false;
    protected boolean Stmt_pred_computed = false;
    protected Collection Stmt_pred_value;
    // Declared in ControlFlowGraph.jrag at line 74
    public Collection pred() {
        if(Stmt_pred_computed)
            return Stmt_pred_value;
        if(Stmt_pred_visited)
            throw new RuntimeException("Circular definition of attr: pred in class: ");
        Stmt_pred_visited = true;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Stmt_pred_value = pred_compute();
        if(isFinal && num == boundariesCrossed)
            Stmt_pred_computed = true;
        Stmt_pred_visited = false;
        return Stmt_pred_value;
    }

    java.util.HashSet Stmt_pred_contributors = new java.util.HashSet();
    private Collection pred_compute() {
        ASTNode node = this;
        while(node.getParent() != null)
            node = node.getParent();
        Program root = (Program)node;
        root.collect_contributors_Stmt_pred();
        Stmt_pred_value = new ArrayList();
        for(java.util.Iterator iter = Stmt_pred_contributors.iterator(); iter.hasNext(); ) {
            ASTNode contributor = (ASTNode)iter.next();
            contributor.contributeTo_Stmt_Stmt_pred(Stmt_pred_value);
        }
        return Stmt_pred_value;
    }

    protected void collect_contributors_Stmt_pred() {
        // Declared in ControlFlowGraph.jrag at line 75
        for(Iterator iter = (succ()).iterator(); iter.hasNext(); ) {
            Stmt ref = (Stmt)iter.next();
            if(ref != null)
            ref.Stmt_pred_contributors.add(this);
        }
        super.collect_contributors_Stmt_pred();
    }
    protected void contributeTo_Stmt_Stmt_pred(Collection collection) {
        collection.add(this);
    }

}
