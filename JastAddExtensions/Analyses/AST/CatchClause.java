
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;

public class CatchClause extends ASTNode implements Cloneable,  VariableScope {
    public void flushCache() {
        super.flushCache();
        parameterDeclaration_String_values = null;
        typeThrowable_computed = false;
        typeThrowable_value = null;
        lookupVariable_String_values = null;
        reachableCatchClause_computed = false;
    }
    public Object clone() throws CloneNotSupportedException {
        CatchClause node = (CatchClause)super.clone();
        node.parameterDeclaration_String_values = null;
        node.typeThrowable_computed = false;
        node.typeThrowable_value = null;
        node.lookupVariable_String_values = null;
        node.reachableCatchClause_computed = false;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          CatchClause node = (CatchClause)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        CatchClause res = (CatchClause)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 865


  public void toString(StringBuffer s) {
    s.append("catch (");
    getParameter().toString(s);
    s.append(") ");
    getBlock().toString(s);
  }

    // Declared in TypeCheck.jrag at line 357


  public void typeCheck() {
    if(!getParameter().type().instanceOf(typeThrowable()))
      error("*** The catch variable must extend Throwable");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 224

    public CatchClause() {
        super();

        setChild(null, 0);
        setChild(null, 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 224
    public CatchClause(ParameterDeclaration p0, Block p1) {
        setChild(p0, 0);
        setChild(p1, 1);
    }

    // Declared in java.ast at line 17


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 20

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 224
    public void setParameter(ParameterDeclaration node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public ParameterDeclaration getParameter() {
        return (ParameterDeclaration)getChild(0);
    }

    // Declared in java.ast at line 9


    public ParameterDeclaration getParameterNoTransform() {
        return (ParameterDeclaration)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 224
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

    // Declared in ExceptionHandling.jrag at line 180
    public boolean handles(TypeDecl exceptionType) {
        boolean handles_TypeDecl_value = handles_compute(exceptionType);
        return handles_TypeDecl_value;
    }

    private boolean handles_compute(TypeDecl exceptionType) {  return 
    exceptionType.instanceOf(getParameter().type());  }

    protected java.util.Map parameterDeclaration_String_values;
    // Declared in LookupVariable.jrag at line 114
    public SimpleSet parameterDeclaration(String name) {
        Object _parameters = name;
if(parameterDeclaration_String_values == null) parameterDeclaration_String_values = new java.util.HashMap(4);
        if(parameterDeclaration_String_values.containsKey(_parameters))
            return (SimpleSet)parameterDeclaration_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet parameterDeclaration_String_value = parameterDeclaration_compute(name);
        if(isFinal && num == boundariesCrossed)
            parameterDeclaration_String_values.put(_parameters, parameterDeclaration_String_value);
        return parameterDeclaration_String_value;
    }

    private SimpleSet parameterDeclaration_compute(String name) {  return  
    getParameter().name().equals(name) ? (ParameterDeclaration)getParameter() : SimpleSet.emptySet;  }

    protected boolean typeThrowable_computed = false;
    protected TypeDecl typeThrowable_value;
    // Declared in LookupType.jrag at line 59
    public TypeDecl typeThrowable() {
        if(typeThrowable_computed)
            return typeThrowable_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeThrowable_value = getParent().Define_TypeDecl_typeThrowable(this, null);
        if(isFinal && num == boundariesCrossed)
            typeThrowable_computed = true;
        return typeThrowable_value;
    }

    protected java.util.Map lookupVariable_String_values;
    // Declared in LookupVariable.jrag at line 11
    public SimpleSet lookupVariable(String name) {
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

    protected boolean reachableCatchClause_computed = false;
    protected boolean reachableCatchClause_value;
    // Declared in UnreachableStatements.jrag at line 115
    public boolean reachableCatchClause() {
        if(reachableCatchClause_computed)
            return reachableCatchClause_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        reachableCatchClause_value = getParent().Define_boolean_reachableCatchClause(this, null);
        if(isFinal && num == boundariesCrossed)
            reachableCatchClause_computed = true;
        return reachableCatchClause_value;
    }

    // Declared in LocalDeclaration.jrag at line 33
    public Collection visibleLocalDecls() {
        Collection visibleLocalDecls_value = getParent().Define_Collection_visibleLocalDecls(this, null);
        return visibleLocalDecls_value;
    }

    // Declared in ControlFlowGraph.jrag at line 167
    public Set enclosingFinally() {
        Set enclosingFinally_value = getParent().Define_Set_enclosingFinally(this, null);
        return enclosingFinally_value;
    }

    // Declared in LocalDeclaration.jrag at line 47
    public Collection Define_Collection_visibleLocalDecls(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
		Collection decls = visibleLocalDecls();
		decls.add(getParameter());
		return decls;
	}
        return getParent().Define_Collection_visibleLocalDecls(this, caller);
    }

    // Declared in VariableDeclaration.jrag at line 74
    public boolean Define_boolean_isConstructorParameter(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  false;
        }
        return getParent().Define_boolean_isConstructorParameter(this, caller);
    }

    // Declared in VariableDeclaration.jrag at line 73
    public boolean Define_boolean_isMethodParameter(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  false;
        }
        return getParent().Define_boolean_isMethodParameter(this, caller);
    }

    // Declared in Domination.jrag at line 63
    public Block Define_Block_getBlock(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  getBlock();
        }
        return getParent().Define_Block_getBlock(this, caller);
    }

    // Declared in LookupVariable.jrag at line 86
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        if(caller == getParameterNoTransform()) {
            return  parameterDeclaration(name);
        }
        if(caller == getBlockNoTransform()) {
    SimpleSet set = parameterDeclaration(name);
    if(!set.isEmpty()) return set;
    return lookupVariable(name);
  }
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }

    // Declared in ControlFlowGraph.jrag at line 176
    public boolean Define_boolean_withInCatchClause(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_withInCatchClause(this, caller);
    }

    // Declared in VariableDeclaration.jrag at line 75
    public boolean Define_boolean_isExceptionHandlerParameter(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isExceptionHandlerParameter(this, caller);
    }

    // Declared in UnreachableStatements.jrag at line 113
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  reachableCatchClause();
        }
        return getParent().Define_boolean_reachable(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 76
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in NameCheck.jrag at line 278
    public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
        if(caller == getParameterNoTransform()) {
            return  this;
        }
        return getParent().Define_VariableScope_outerScope(this, caller);
    }

    // Declared in ControlFlowGraph.jrag at line 171
    public Set Define_Set_enclosingFinally(ASTNode caller, ASTNode child) {
        if(caller == getBlockNoTransform()) {
            return  enclosingFinally();
        }
        return getParent().Define_Set_enclosingFinally(this, caller);
    }

    // Declared in RenameParameter.jrag at line 18
    public RefactoringException Define_RefactoringException_canRenameTo(ASTNode caller, ASTNode child, String new_name) {
        if(caller == getParameterNoTransform()) {
		return getBlock().acceptLocal(new_name);
	}
        return getParent().Define_RefactoringException_canRenameTo(this, caller, new_name);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
