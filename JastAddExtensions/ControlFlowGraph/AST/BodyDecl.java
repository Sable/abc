
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;



public abstract class BodyDecl extends ASTNode<ASTNode> implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isDAafter_Variable_values = null;
        isDUafter_Variable_values = null;
        entry_computed = false;
        entry_value = null;
        exit_computed = false;
        exit_value = null;
        isDAbefore_Variable_values = null;
        isDUbefore_Variable_values = null;
        typeThrowable_computed = false;
        typeThrowable_value = null;
        lookupVariable_String_values = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public BodyDecl clone() throws CloneNotSupportedException {
        BodyDecl node = (BodyDecl)super.clone();
        node.isDAafter_Variable_values = null;
        node.isDUafter_Variable_values = null;
        node.entry_computed = false;
        node.entry_value = null;
        node.exit_computed = false;
        node.exit_value = null;
        node.isDAbefore_Variable_values = null;
        node.isDUbefore_Variable_values = null;
        node.typeThrowable_computed = false;
        node.typeThrowable_value = null;
        node.lookupVariable_String_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in BranchTarget.jrag at line 211

  public void collectFinally(Stmt branchStmt, ArrayList list) {
    // terminate search if body declaration is reached
  }

    // Declared in ControlFlowGraph.jrag at line 760

    
    // The end for throw/return
    public SmallSet followingTarget(Stmt branch, ASTNode child) {
    	return SmallSet.empty().union(exit());  	
    }

    // Declared in java.ast at line 3
    // Declared in java.ast line 69

    public BodyDecl() {
        super();


    }

    // Declared in java.ast at line 9


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 12

  public boolean mayHaveRewrite() { return false; }

    protected java.util.Map isDAafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 245
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

    private boolean isDAafter_compute(Variable v) {  return true;  }

    protected java.util.Map isDUafter_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 709
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

    private boolean isDUafter_compute(Variable v) {  return true;  }

    // Declared in LookupType.jrag at line 391
 @SuppressWarnings({"unchecked", "cast"})     public boolean declaresType(String name) {
        boolean declaresType_String_value = declaresType_compute(name);
        return declaresType_String_value;
    }

    private boolean declaresType_compute(String name) {  return false;  }

    // Declared in LookupType.jrag at line 393
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl type(String name) {
        TypeDecl type_String_value = type_compute(name);
        return type_String_value;
    }

    private TypeDecl type_compute(String name) {  return null;  }

    // Declared in TypeAnalysis.jrag at line 271
 @SuppressWarnings({"unchecked", "cast"})     public boolean isVoid() {
        boolean isVoid_value = isVoid_compute();
        return isVoid_value;
    }

    private boolean isVoid_compute() {  return false;  }

    protected boolean entry_computed = false;
    protected Stmt entry_value;
    // Declared in ControlFlowGraph.jrag at line 11
 @SuppressWarnings({"unchecked", "cast"})     public Stmt entry() {
        if(entry_computed)
            return entry_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        entry_value = entry_compute();
        entry_value.setParent(this);
        entry_value.is$Final = true;
        if(true)
            entry_computed = true;
        return entry_value;
    }

    private Stmt entry_compute() {  return new EmptyStmt();  }

    protected boolean exit_computed = false;
    protected Stmt exit_value;
    // Declared in ControlFlowGraph.jrag at line 12
 @SuppressWarnings({"unchecked", "cast"})     public Stmt exit() {
        if(exit_computed)
            return exit_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        exit_value = exit_compute();
        exit_value.setParent(this);
        exit_value.is$Final = true;
        if(true)
            exit_computed = true;
        return exit_value;
    }

    private Stmt exit_compute() {  return new EmptyStmt();  }

    protected java.util.Map isDAbefore_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 244
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDAbefore(Variable v) {
        Object _parameters = v;
if(isDAbefore_Variable_values == null) isDAbefore_Variable_values = new java.util.HashMap(4);
        if(isDAbefore_Variable_values.containsKey(_parameters))
            return ((Boolean)isDAbefore_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDAbefore_Variable_value = getParent().Define_boolean_isDAbefore(this, null, v);
        if(isFinal && num == boundariesCrossed)
            isDAbefore_Variable_values.put(_parameters, Boolean.valueOf(isDAbefore_Variable_value));
        return isDAbefore_Variable_value;
    }

    protected java.util.Map isDUbefore_Variable_values;
    // Declared in DefiniteAssignment.jrag at line 708
 @SuppressWarnings({"unchecked", "cast"})     public boolean isDUbefore(Variable v) {
        Object _parameters = v;
if(isDUbefore_Variable_values == null) isDUbefore_Variable_values = new java.util.HashMap(4);
        if(isDUbefore_Variable_values.containsKey(_parameters))
            return ((Boolean)isDUbefore_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean isDUbefore_Variable_value = getParent().Define_boolean_isDUbefore(this, null, v);
        if(isFinal && num == boundariesCrossed)
            isDUbefore_Variable_values.put(_parameters, Boolean.valueOf(isDUbefore_Variable_value));
        return isDUbefore_Variable_value;
    }

    protected boolean typeThrowable_computed = false;
    protected TypeDecl typeThrowable_value;
    // Declared in ExceptionHandling.jrag at line 22
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl typeThrowable() {
        if(typeThrowable_computed)
            return typeThrowable_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeThrowable_value = getParent().Define_TypeDecl_typeThrowable(this, null);
        if(isFinal && num == boundariesCrossed)
            typeThrowable_computed = true;
        return typeThrowable_value;
    }

    // Declared in LookupMethod.jrag at line 25
 @SuppressWarnings({"unchecked", "cast"})     public Collection lookupMethod(String name) {
        Collection lookupMethod_String_value = getParent().Define_Collection_lookupMethod(this, null, name);
        return lookupMethod_String_value;
    }

    // Declared in LookupType.jrag at line 97
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    // Declared in LookupType.jrag at line 173
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet lookupType(String name) {
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        return lookupType_String_value;
    }

    protected java.util.Map lookupVariable_String_values;
    // Declared in LookupVariable.jrag at line 15
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet lookupVariable(String name) {
        Object _parameters = name;
if(lookupVariable_String_values == null) lookupVariable_String_values = new java.util.HashMap(4);
        if(lookupVariable_String_values.containsKey(_parameters))
            return (SimpleSet)lookupVariable_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet lookupVariable_String_value = getParent().Define_SimpleSet_lookupVariable(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupVariable_String_values.put(_parameters, lookupVariable_String_value);
        return lookupVariable_String_value;
    }

    // Declared in SyntacticClassification.jrag at line 21
 @SuppressWarnings({"unchecked", "cast"})     public NameType nameType() {
        NameType nameType_value = getParent().Define_NameType_nameType(this, null);
        return nameType_value;
    }

    // Declared in TypeAnalysis.jrag at line 567
 @SuppressWarnings({"unchecked", "cast"})     public String hostPackage() {
        String hostPackage_value = getParent().Define_String_hostPackage(this, null);
        return hostPackage_value;
    }

    // Declared in TypeAnalysis.jrag at line 582
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl hostType() {
        TypeDecl hostType_value = getParent().Define_TypeDecl_hostType(this, null);
        return hostType_value;
    }

    // Declared in ControlFlowGraph.jrag at line 21
    public CFGNode Define_CFGNode_exit(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return exit();
        }
        return getParent().Define_CFGNode_exit(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 515
    public BodyDecl Define_BodyDecl_enclosingBodyDecl(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return this;
        }
        return getParent().Define_BodyDecl_enclosingBodyDecl(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 17
    public SmallSet Define_SmallSet_following(ASTNode caller, ASTNode child) {
        if(caller == exit_value) {
            return SmallSet.empty();
        }
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return SmallSet.empty().union(exit());
        }
        return getParent().Define_SmallSet_following(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 24
    public CFGNode Define_CFGNode_entry(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return entry();
        }
        return getParent().Define_CFGNode_entry(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
