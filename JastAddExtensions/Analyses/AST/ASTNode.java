
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;

// Generated with JastAdd II (http://jastadd.cs.lth.se) version R20071114

public class ASTNode extends beaver.Symbol  implements Cloneable {
    public void flushCache() {
        mayDef_Variable_values = null;
        mayUse_Variable_values = null;
        mayAccess_Variable_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ASTNode node = (ASTNode)super.clone();
        node.mayDef_Variable_values = null;
        node.mayUse_Variable_values = null;
        node.mayAccess_Variable_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ASTNode node = (ASTNode)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ASTNode res = (ASTNode)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in AccessControl.jrag at line 116

    
  public void accessControl() {
  }

    // Declared in AnonymousClasses.jrag at line 127


  protected void collectExceptions(Collection c, ASTNode target) {
    for(int i = 0; i < getNumChild(); i++)
      getChild(i).collectExceptions(c, target);
  }

    // Declared in BranchTarget.jrag at line 36

  
  public void collectBranches(Collection c) {
    for(int i = 0; i < getNumChild(); i++)
      getChild(i).collectBranches(c);
  }

    // Declared in BranchTarget.jrag at line 142

  public Stmt branchTarget(Stmt branchStmt) {
    if(getParent() != null)
      return getParent().branchTarget(branchStmt);
    else
      return null;
  }

    // Declared in BranchTarget.jrag at line 182

  public void collectFinally(Stmt branchStmt, ArrayList list) {
    if(getParent() != null)
      getParent().collectFinally(branchStmt, list);
  }

    // Declared in DeclareBeforeUse.jrag at line 4

  public int varChildIndex(Block b) {
    ASTNode node = this;
    while(node.getParent().getParent() != b) {
      node = node.getParent();
    }
    return b.getStmtListNoTransform().getIndexOfChild(node);
  }

    // Declared in DeclareBeforeUse.jrag at line 22


  public int varChildIndex(TypeDecl t) {
    ASTNode node = this;
    while(node != null && node.getParent() != null && node.getParent().getParent() != t) {
      node = node.getParent();
    }
    if(node == null)
      return -1;
    return t.getBodyDeclListNoTransform().getIndexOfChild(node);
  }

    // Declared in DefiniteAssignment.jrag at line 3


  public void definiteAssignment() {
  }

    // Declared in DefiniteAssignment.jrag at line 447


  // 16.2.2 9th, 10th bullet
  protected boolean checkDUeverywhere(Variable v) {
    for(int i = 0; i < getNumChild(); i++)
      if(!getChild(i).checkDUeverywhere(v))
        return false;
    return true;
  }

    // Declared in DefiniteAssignment.jrag at line 557


  protected boolean isDescendantTo(ASTNode node) {
    if(this == node)
      return true;
    if(getParent() == null)
      return false;
    return getParent().isDescendantTo(node);
  }

    // Declared in ErrorCheck.jrag at line 3


  protected String sourceFile() {
    ASTNode node = this;
    while(node != null && !(node instanceof CompilationUnit))
      node = node.getParent();
    if(node == null)
      return "Unknown file";
    CompilationUnit u = (CompilationUnit)node;
    return u.relativeName();
  }

    // Declared in ErrorCheck.jrag at line 30


  // set start and end position to the same as the argument and return self
  public ASTNode setLocation(ASTNode node) {
    setStart(node.getStart());
    setEnd(node.getEnd());
    return this;
  }

    // Declared in ErrorCheck.jrag at line 36


  public ASTNode setStart(int i) {
    start = i;
    return this;
  }

    // Declared in ErrorCheck.jrag at line 40

  public int start() {
    return start;
  }

    // Declared in ErrorCheck.jrag at line 43

  public ASTNode setEnd(int i) {
    end = i;
    return this;
  }

    // Declared in ErrorCheck.jrag at line 47

  public int end() {
    return end;
  }

    // Declared in ErrorCheck.jrag at line 53




  public String location() {
    return "" + lineNumber();
  }

    // Declared in ErrorCheck.jrag at line 56

  public String errorPrefix() {
    return sourceFile() + ":" + location() + ":\n" + "  *** Semantic Error: ";
  }

    // Declared in ErrorCheck.jrag at line 59

  public String warningPrefix() {
    return sourceFile() + ":" + location() + ":\n" + "  *** WARNING: ";
  }

    // Declared in ErrorCheck.jrag at line 169


  protected void error(String s) {
    ASTNode node = this;
    while(node != null && !(node instanceof CompilationUnit))
      node = node.getParent();
    CompilationUnit cu = (CompilationUnit)node;
    if(getNumChild() == 0 && getStart() != 0 && getEnd() != 0) {  
      int line = getLine(getStart());
      int column = getColumn(getStart());
      int endLine = getLine(getEnd());
      int endColumn = getColumn(getEnd());
      cu.errors.add(new Problem(sourceFile(), s, line, column, endLine, endColumn, Problem.Severity.ERROR, Problem.Kind.SEMANTIC));
    }
    else
      cu.errors.add(new Problem(sourceFile(), s, lineNumber(), Problem.Severity.ERROR, Problem.Kind.SEMANTIC));
  }

    // Declared in ErrorCheck.jrag at line 185


  protected void warning(String s) {
    ASTNode node = this;
    while(node != null && !(node instanceof CompilationUnit))
      node = node.getParent();
    CompilationUnit cu = (CompilationUnit)node;
    cu.warnings.add(new Problem(sourceFile(), "WARNING: " + s, lineNumber(), Problem.Severity.WARNING));
  }

    // Declared in ErrorCheck.jrag at line 193

  
  public void collectErrors() {
    nameCheck();
    typeCheck();
    accessControl();
    exceptionHandling();
    checkUnreachableStmt();
    definiteAssignment();
    checkModifiers();
    for(int i = 0; i < getNumChild(); i++) {
      getChild(i).collectErrors();
    }
  }

    // Declared in ExceptionHandling.jrag at line 31

  
  public void exceptionHandling() {
  }

    // Declared in ExceptionHandling.jrag at line 186


  protected boolean reachedException(TypeDecl type) {
    for(int i = 0; i < getNumChild(); i++)
      if(getChild(i).reachedException(type))
        return true;
    return false;
  }

    // Declared in LookupMethod.jrag at line 40

  public static Collection removeInstanceMethods(Collection c) {
    c = new LinkedList(c);
    for(Iterator iter = c.iterator(); iter.hasNext(); ) {
      MethodDecl m = (MethodDecl)iter.next();
      if(!m.isStatic())
        iter.remove();
    }
    return c;
  }

    // Declared in LookupMethod.jrag at line 328

  protected static void putSimpleSetElement(HashMap map, Object key, Object value) {
    SimpleSet set = (SimpleSet)map.get(key);
    if(set == null) set = SimpleSet.emptySet;
    map.put(key, set.add(value));
  }

    // Declared in LookupVariable.jrag at line 179


  public SimpleSet removeInstanceVariables(SimpleSet oldSet) {
    SimpleSet newSet = SimpleSet.emptySet;
    for(Iterator iter = oldSet.iterator(); iter.hasNext(); ) {
      Variable v = (Variable)iter.next();
      if(!v.isInstanceVariable())
        newSet = newSet.add(v);
    }
    return newSet;
  }

    // Declared in Modifiers.jrag at line 2

  void checkModifiers() {
  }

    // Declared in NameCheck.jrag at line 2

  public void nameCheck() {
  }

    // Declared in NameCheck.jrag at line 5


  public TypeDecl extractSingleType(SimpleSet c) {
    if(c.size() != 1)
      return null;
    return (TypeDecl)c.iterator().next();
  }

    // Declared in PrettyPrint.jadd at line 4

  // Helper for indentation
  
  protected static int indent = 0;

    // Declared in PrettyPrint.jadd at line 6

  
  public static String indent() {
    StringBuffer s = new StringBuffer();
    for(int i = 0; i < indent; i++) {
      s.append("  ");
    }
    return s.toString();
  }

    // Declared in PrettyPrint.jadd at line 16


  // Default output
  
  public String toString() {
    StringBuffer s = new StringBuffer();
    toString(s);
    return s.toString().trim();
  }

    // Declared in PrettyPrint.jadd at line 22

  
  public void toString(StringBuffer s) {
  }

    // Declared in PrettyPrint.jadd at line 905


  // dump the AST to standard output

  public String dumpTree() {
    StringBuffer s = new StringBuffer();
    dumpTree(s, 0);
    return s.toString();
  }

    // Declared in PrettyPrint.jadd at line 911


  public void dumpTree(StringBuffer s, int j) {
    for(int i = 0; i < j; i++) {
      s.append("  ");
    }
    s.append(dumpString() + "\n");
    for(int i = 0; i < getNumChild(); i++)
      getChild(i).dumpTree(s, j + 1);
  }

    // Declared in PrettyPrint.jadd at line 920


  public String dumpTreeNoRewrite() {
    StringBuffer s = new StringBuffer();
    dumpTreeNoRewrite(s, 0);
    return s.toString();
  }

    // Declared in PrettyPrint.jadd at line 925

  protected void dumpTreeNoRewrite(StringBuffer s, int indent) {
    for(int i = 0; i < indent; i++)
      s.append("  ");
    s.append(dumpString());
    s.append("\n");
    for(int i = 0; i < getNumChild(); i++) {
      getChildNoTransform(i).dumpTreeNoRewrite(s, indent+1);
    }
  }

    // Declared in PrimitiveTypes.jrag at line 2

  protected static final String PRIMITIVE_PACKAGE_NAME = "@primitive";

    // Declared in TypeCheck.jrag at line 3

  public void typeCheck() {
  }

    // Declared in UnreachableStatements.jrag at line 3


  void checkUnreachableStmt() {
  }

    // Declared in ASTUtil.jrag at line 18

	
	// clear all attribute values in this subtree to force reevaluation of attributes after destructive updates
	public void clear() {
		flushCache();
		for(int i = 0; i < getNumChild(); i++)
			getChild(i).clear();
	}

    // Declared in ASTUtil.jrag at line 27


	// imperative transformation of the AST
    //	syntax ASTNode.replace(sourcenode).with(destnode)

	protected static ASTNode replace(ASTNode node) {
		replacePos = node.getParent().getIndexOfChild(node);
		return node.getParent();
	}

    // Declared in ASTUtil.jrag at line 31

	protected ASTNode with(ASTNode node) {
		setChild(node, replacePos);
		return node;
	}

    // Declared in ASTUtil.jrag at line 36


	private static int replacePos = 0;

    // Declared in ExtractMethod.jrag at line 24

	
	public int indexIn(ASTNode n) {
		if(getParent() == n)
			return getParent().getIndexOfChild(this);
		if(getParent() == null)
			return -1;
		return getParent().indexIn(n);
	}

    // Declared in ASTNode.ast at line 3
    // Declared in ASTNode.ast line 0

    public ASTNode() {
        super();


    }

    // Declared in ASTNode.ast at line 9


   static public boolean generatedWithCircularEnabled = true;

    // Declared in ASTNode.ast at line 10

   static public boolean generatedWithCacheCycle = false;

    // Declared in ASTNode.ast at line 11

   static public boolean generatedWithComponentCheck = false;

    // Declared in ASTNode.ast at line 12

  static public boolean IN_CIRCLE = false;

    // Declared in ASTNode.ast at line 13

  static public boolean CHANGE = false;

    // Declared in ASTNode.ast at line 14

  static public boolean RESET_CYCLE = false;

    // Declared in ASTNode.ast at line 15

  public static int boundariesCrossed = 0;

    // Declared in ASTNode.ast at line 42

  protected static State state = new State();

    // Declared in ASTNode.ast at line 43

  public boolean in$Circle = false;

    // Declared in ASTNode.ast at line 44

  public boolean in$Circle() { return in$Circle; }

    // Declared in ASTNode.ast at line 45

  public void in$Circle(boolean b) { in$Circle = b; }

    // Declared in ASTNode.ast at line 46

  public boolean is$Final = false;

    // Declared in ASTNode.ast at line 47

  public boolean is$Final() { return is$Final; }

    // Declared in ASTNode.ast at line 48

  public void is$Final(boolean b) { is$Final = b; }

    // Declared in ASTNode.ast at line 49

  protected static final int REWRITE_CHANGE = 1;

    // Declared in ASTNode.ast at line 50

  protected static final int REWRITE_NOCHANGE = 2;

    // Declared in ASTNode.ast at line 51

  protected static final int REWRITE_INTERRUPT = 3;

    // Declared in ASTNode.ast at line 52

  public ASTNode getChild(int i) {
    return ASTNode.getChild(this, i);
  }

    // Declared in ASTNode.ast at line 55

  public static ASTNode getChild(ASTNode that, int i) {
    ASTNode node = that.getChildNoTransform(i);
    if(node.is$Final()) return node;
    if(!node.mayHaveRewrite()) {
      node.is$Final(that.is$Final());
      return node;
    }
    if(!node.in$Circle()) {
      int rewriteState;
      int num = ASTNode.boundariesCrossed;
      do {
        ASTNode.state.push(ASTNode.REWRITE_CHANGE);
        ASTNode oldNode = node;
        oldNode.in$Circle(true);
        node = node.rewriteTo();
        oldNode.in$Circle(false);
        that.setChild(node, i);
        rewriteState = state.pop();
      } while(rewriteState == ASTNode.REWRITE_CHANGE);
      if(rewriteState == ASTNode.REWRITE_NOCHANGE && that.is$Final()) {
        node.is$Final(true);
        ASTNode.boundariesCrossed = num;
      }
    }
    else if(that.is$Final() != node.is$Final()) boundariesCrossed++;
    return node;
  }

    // Declared in ASTNode.ast at line 82

  private int childIndex;

    // Declared in ASTNode.ast at line 83

  public int getIndexOfChild(ASTNode node) {
    if(node != null && node.childIndex < numChildren() && node == getChildNoTransform(node.childIndex))
      return node.childIndex;
    for(int i = 0; i < numChildren(); i++)
      if(getChildNoTransform(i) == node) {
        node.childIndex = i;
        return i;
      }
    return -1;
  }

    // Declared in ASTNode.ast at line 94


  public void addChild(ASTNode node) {
    setChild(node, numChildren());
  }

    // Declared in ASTNode.ast at line 97

  public ASTNode getChildNoTransform(int i) {
    return children[i];
  }

    // Declared in ASTNode.ast at line 100

  protected ASTNode parent;

    // Declared in ASTNode.ast at line 101

  protected ASTNode[] children;

    // Declared in ASTNode.ast at line 102

  protected int numChildren;

    // Declared in ASTNode.ast at line 103

  protected int numChildren() {
    return numChildren;
  }

    // Declared in ASTNode.ast at line 106

  public int getNumChild() {
    return numChildren();
  }

    // Declared in ASTNode.ast at line 109

  public void setChild(ASTNode node, int i) {
    if(children == null) {
      children = new ASTNode[i + 1];
    } else if (i >= children.length) {
      ASTNode c[] = new ASTNode[i << 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = node;
    if(i >= numChildren) numChildren = i+1;
    if(node != null) { node.setParent(this); node.childIndex = i; }
  }

    // Declared in ASTNode.ast at line 121

  public void insertChild(ASTNode node, int i) {
    if(children == null) {
      children = new ASTNode[i + 1];
      children[i] = node;
    } else {
      ASTNode c[] = new ASTNode[children.length + 1];
      System.arraycopy(children, 0, c, 0, i);
      c[i] = node;
      if(i < children.length)
        System.arraycopy(children, i, c, i+1, children.length-i);
      children = c;
    }
    numChildren++;
    if(node != null) { node.setParent(this); node.childIndex = i; }
  }

    // Declared in ASTNode.ast at line 136

  public void removeChild(int i) {
    if(children != null) {
      ASTNode child = children[i];
      if(child != null) {
        child.setParent(null);
        child.childIndex = -1;
      }
      System.arraycopy(children, i+1, children, i, children.length-i-1);
      numChildren--;
    }
  }

    // Declared in ASTNode.ast at line 147

  public ASTNode getParent() {
    if(parent != null && parent.is$Final() != is$Final()) {
      boundariesCrossed++;
    }
    return parent;
  }

    // Declared in ASTNode.ast at line 153

  public void setParent(ASTNode node) {
    parent = node;
  }

    // Declared in ASTNode.ast at line 156

    protected static int duringDefiniteAssignment = 0;

    // Declared in ASTNode.ast at line 157

    protected static boolean duringDefiniteAssignment() {
        if(duringDefiniteAssignment == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 167

    protected static int duringVariableDeclaration = 0;

    // Declared in ASTNode.ast at line 168

    protected static boolean duringVariableDeclaration() {
        if(duringVariableDeclaration == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 178

    protected static int duringLookupConstructor = 0;

    // Declared in ASTNode.ast at line 179

    protected static boolean duringLookupConstructor() {
        if(duringLookupConstructor == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 189

    protected static int duringConstantExpression = 0;

    // Declared in ASTNode.ast at line 190

    protected static boolean duringConstantExpression() {
        if(duringConstantExpression == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 200

    protected static int duringAnonymousClasses = 0;

    // Declared in ASTNode.ast at line 201

    protected static boolean duringAnonymousClasses() {
        if(duringAnonymousClasses == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 211

    protected static int duringSyntacticClassification = 0;

    // Declared in ASTNode.ast at line 212

    protected static boolean duringSyntacticClassification() {
        if(duringSyntacticClassification == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 222

    protected static int duringResolveAmbiguousNames = 0;

    // Declared in ASTNode.ast at line 223

    protected static boolean duringResolveAmbiguousNames() {
        if(duringResolveAmbiguousNames == 0) {
            return false;
        }
        else {
            state.pop();
            state.push(ASTNode.REWRITE_INTERRUPT);
            return true;
        }
    }

    // Declared in ASTNode.ast at line 233

    public static void reset() {
        IN_CIRCLE = false;
        CHANGE = false;
        boundariesCrossed = 0;
        state = new State();
        if(duringDefiniteAssignment != 0) {
            System.out.println("Warning: resetting duringDefiniteAssignment");
            duringDefiniteAssignment = 0;
        }
        if(duringVariableDeclaration != 0) {
            System.out.println("Warning: resetting duringVariableDeclaration");
            duringVariableDeclaration = 0;
        }
        if(duringLookupConstructor != 0) {
            System.out.println("Warning: resetting duringLookupConstructor");
            duringLookupConstructor = 0;
        }
        if(duringConstantExpression != 0) {
            System.out.println("Warning: resetting duringConstantExpression");
            duringConstantExpression = 0;
        }
        if(duringAnonymousClasses != 0) {
            System.out.println("Warning: resetting duringAnonymousClasses");
            duringAnonymousClasses = 0;
        }
        if(duringSyntacticClassification != 0) {
            System.out.println("Warning: resetting duringSyntacticClassification");
            duringSyntacticClassification = 0;
        }
        if(duringResolveAmbiguousNames != 0) {
            System.out.println("Warning: resetting duringResolveAmbiguousNames");
            duringResolveAmbiguousNames = 0;
        }
    }

    // Declared in ASTNode.ast at line 267

  public boolean mayHaveRewrite() { return false; }

    // Declared in ControlFlowGraph.jrag at line 74
    protected void collect_contributors_Stmt_pred() {
        for(int i = 0; i < getNumChild(); i++)
            getChild(i).collect_contributors_Stmt_pred();
    }

    protected void contributeTo_Stmt_Stmt_pred(Collection collection) {
    }


    // Declared in DefiniteAssignment.jrag at line 1209
    public boolean unassignedEverywhere(Variable v, TryStmt stmt) {
        boolean unassignedEverywhere_Variable_TryStmt_value = unassignedEverywhere_compute(v, stmt);
        return unassignedEverywhere_Variable_TryStmt_value;
    }

    private boolean unassignedEverywhere_compute(Variable v, TryStmt stmt)  {
    for(int i = 0; i < getNumChild(); i++) {
      if(!getChild(i).unassignedEverywhere(v, stmt))
        return false;
    }
    return true;
  }

    // Declared in ErrorCheck.jrag at line 13
    public int lineNumber() {
        int lineNumber_value = lineNumber_compute();
        return lineNumber_value;
    }

    private int lineNumber_compute()  {
    ASTNode n = this;
    while(n.getParent() != null && n.getStart() == 0) {
      n = n.getParent();
    }
    return getLine(n.getStart());
  }

    // Declared in PrettyPrint.jadd at line 935
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName();  }

    // Declared in ControlFlowGraph.jrag at line 114
    public Set uncaughtThrows() {
        Set uncaughtThrows_value = uncaughtThrows_compute();
        return uncaughtThrows_value;
    }

    private Set uncaughtThrows_compute()  {
		Set uncaughtThrows = Set.empty();
		for(int i = 0; i < getNumChild(); i++)
			uncaughtThrows = uncaughtThrows.union(getChild(i).uncaughtThrows());
		return uncaughtThrows;
	}

    protected java.util.Map mayDef_Variable_values;
    // Declared in VarDefUse.jrag at line 3
    public boolean mayDef(Variable v) {
        Object _parameters = v;
if(mayDef_Variable_values == null) mayDef_Variable_values = new java.util.HashMap(4);
        if(mayDef_Variable_values.containsKey(_parameters))
            return ((Boolean)mayDef_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayDef_Variable_value = mayDef_compute(v);
        if(isFinal && num == boundariesCrossed)
            mayDef_Variable_values.put(_parameters, Boolean.valueOf(mayDef_Variable_value));
        return mayDef_Variable_value;
    }

    private boolean mayDef_compute(Variable v)  {
		for(int i=0;i<getNumChild();++i)
			if(getChild(i).mayDef(v)) return true;
		return false;
	}

    protected java.util.Map mayUse_Variable_values;
    // Declared in VarDefUse.jrag at line 15
    public boolean mayUse(Variable v) {
        Object _parameters = v;
if(mayUse_Variable_values == null) mayUse_Variable_values = new java.util.HashMap(4);
        if(mayUse_Variable_values.containsKey(_parameters))
            return ((Boolean)mayUse_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayUse_Variable_value = mayUse_compute(v);
        if(isFinal && num == boundariesCrossed)
            mayUse_Variable_values.put(_parameters, Boolean.valueOf(mayUse_Variable_value));
        return mayUse_Variable_value;
    }

    private boolean mayUse_compute(Variable v)  {
		for(int i=0;i<getNumChild();++i)
			if(getChild(i).mayUse(v)) return true;
		return false;
	}

    protected java.util.Map mayAccess_Variable_values;
    // Declared in VarDefUse.jrag at line 23
    public boolean mayAccess(Variable v) {
        Object _parameters = v;
if(mayAccess_Variable_values == null) mayAccess_Variable_values = new java.util.HashMap(4);
        if(mayAccess_Variable_values.containsKey(_parameters))
            return ((Boolean)mayAccess_Variable_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean mayAccess_Variable_value = mayAccess_compute(v);
        if(isFinal && num == boundariesCrossed)
            mayAccess_Variable_values.put(_parameters, Boolean.valueOf(mayAccess_Variable_value));
        return mayAccess_Variable_value;
    }

    private boolean mayAccess_compute(Variable v) {  return  mayDef(v) || mayUse(v);  }

public ASTNode rewriteTo() {
    if(state.peek() == ASTNode.REWRITE_CHANGE) {
        state.pop();
        state.push(ASTNode.REWRITE_NOCHANGE);
    }
    return this;
}

    public boolean Define_boolean_hasEnclosingTryStmt(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_hasEnclosingTryStmt(this, caller);
    }
    public boolean Define_boolean_isFinallyBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isFinallyBlock(this, caller);
    }
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }
    public TypeDecl Define_TypeDecl_typeNullPointerException(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeNullPointerException(this, caller);
    }
    public Collection Define_Collection_lookupConstructor(ASTNode caller, ASTNode child) {
        return getParent().Define_Collection_lookupConstructor(this, caller);
    }
    public boolean Define_boolean_mayBeFinal(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeFinal(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeThrowable(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeThrowable(this, caller);
    }
    public boolean Define_boolean_insideLoop(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_insideLoop(this, caller);
    }
    public LabeledStmt Define_LabeledStmt_lookupLabel(ASTNode caller, ASTNode child, String name) {
        return getParent().Define_LabeledStmt_lookupLabel(this, caller, name);
    }
    public boolean Define_boolean_mayBePublic(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBePublic(this, caller);
    }
    public boolean Define_boolean_isMethodParameter(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isMethodParameter(this, caller);
    }
    public Collection Define_Collection_lookupSuperConstructor(ASTNode caller, ASTNode child) {
        return getParent().Define_Collection_lookupSuperConstructor(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeRuntimeException(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeRuntimeException(this, caller);
    }
    public boolean Define_boolean_mayBeProtected(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeProtected(this, caller);
    }
    public boolean Define_boolean_isMemberType(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isMemberType(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeBoolean(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeBoolean(this, caller);
    }
    public TypeDecl Define_TypeDecl_componentType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_componentType(this, caller);
    }
    public boolean Define_boolean_isDUbefore(ASTNode caller, ASTNode child, Variable v) {
        return getParent().Define_boolean_isDUbefore(this, caller, v);
    }
    public boolean Define_boolean_mayBeStrictfp(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeStrictfp(this, caller);
    }
    public TypeDecl Define_TypeDecl_switchType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_switchType(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeObject(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeObject(this, caller);
    }
    public boolean Define_boolean_inExplicitConstructorInvocation(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_inExplicitConstructorInvocation(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeShort(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeShort(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeCloneable(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeCloneable(this, caller);
    }
    public Program Define_Program_programRoot(ASTNode caller, ASTNode child) {
        return getParent().Define_Program_programRoot(this, caller);
    }
    public boolean Define_boolean_isLocalClass(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isLocalClass(this, caller);
    }
    public boolean Define_boolean_isNestedType(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isNestedType(this, caller);
    }
    public String Define_String_methodHost(ASTNode caller, ASTNode child) {
        return getParent().Define_String_methodHost(this, caller);
    }
    public boolean Define_boolean_withInCatchClause(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_withInCatchClause(this, caller);
    }
    public boolean Define_boolean_isDest(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isDest(this, caller);
    }
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isSource(this, caller);
    }
    public SimpleSet Define_SimpleSet_lookupImport(ASTNode caller, ASTNode child, String name) {
        return getParent().Define_SimpleSet_lookupImport(this, caller, name);
    }
    public ConstructorDecl Define_ConstructorDecl_unknownConstructor(ASTNode caller, ASTNode child) {
        return getParent().Define_ConstructorDecl_unknownConstructor(this, caller);
    }
    public ConstructorDecl Define_ConstructorDecl_constructorDecl(ASTNode caller, ASTNode child) {
        return getParent().Define_ConstructorDecl_constructorDecl(this, caller);
    }
    public ConstCase Define_ConstCase_followingConstCase(ASTNode caller, ASTNode child) {
        return getParent().Define_ConstCase_followingConstCase(this, caller);
    }
    public TypeDecl Define_TypeDecl_declType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_declType(this, caller);
    }
    public Collection Define_Collection_visibleLocalDecls(ASTNode caller, ASTNode child) {
        return getParent().Define_Collection_visibleLocalDecls(this, caller);
    }
    public boolean Define_boolean_isConstructorParameter(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isConstructorParameter(this, caller);
    }
    public SimpleSet Define_SimpleSet_lookupVariable(ASTNode caller, ASTNode child, String name) {
        return getParent().Define_SimpleSet_lookupVariable(this, caller, name);
    }
    public DefaultCase Define_DefaultCase_followingDefaultCase(ASTNode caller, ASTNode child) {
        return getParent().Define_DefaultCase_followingDefaultCase(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeChar(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeChar(this, caller);
    }
    public TypeDecl Define_TypeDecl_lookupType(ASTNode caller, ASTNode child, String packageName, String typeName) {
        return getParent().Define_TypeDecl_lookupType(this, caller, packageName, typeName);
    }
    public boolean Define_boolean_mayBeAbstract(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeAbstract(this, caller);
    }
    public boolean Define_boolean_isAnonymous(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isAnonymous(this, caller);
    }
    public Set Define_Set_enclosingFinally(ASTNode caller, ASTNode child) {
        return getParent().Define_Set_enclosingFinally(this, caller);
    }
    public ASTNode Define_ASTNode_enclosingBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_ASTNode_enclosingBlock(this, caller);
    }
    public boolean Define_boolean_mayBeSynchronized(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeSynchronized(this, caller);
    }
    public boolean Define_boolean_reportUnreachable(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_reportUnreachable(this, caller);
    }
    public TypeDecl Define_TypeDecl_enclosingInstance(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_enclosingInstance(this, caller);
    }
    public VariableScope Define_VariableScope_outerScope(ASTNode caller, ASTNode child) {
        return getParent().Define_VariableScope_outerScope(this, caller);
    }
    public CompilationUnit Define_CompilationUnit_compilationUnit(ASTNode caller, ASTNode child) {
        return getParent().Define_CompilationUnit_compilationUnit(this, caller);
    }
    public boolean Define_boolean_mayBePrivate(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBePrivate(this, caller);
    }
    public TypeDecl Define_TypeDecl_unknownType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_unknownType(this, caller);
    }
    public boolean Define_boolean_between(ASTNode caller, ASTNode child, Block blk, int start, int end) {
        return getParent().Define_boolean_between(this, caller, blk, start, end);
    }
    public TypeDecl Define_TypeDecl_typeSerializable(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeSerializable(this, caller);
    }
    public TryStmt Define_TryStmt_enclosingTryStmt(ASTNode caller, ASTNode child) {
        return getParent().Define_TryStmt_enclosingTryStmt(this, caller);
    }
    public BodyDecl Define_BodyDecl_hostBodyDecl(ASTNode caller, ASTNode child) {
        return getParent().Define_BodyDecl_hostBodyDecl(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeLong(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeLong(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeError(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeError(this, caller);
    }
    public boolean Define_boolean_mayBeTransient(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeTransient(this, caller);
    }
    public String Define_String_packageName(ASTNode caller, ASTNode child) {
        return getParent().Define_String_packageName(this, caller);
    }
    public boolean Define_boolean_mayBeNative(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeNative(this, caller);
    }
    public MethodDecl Define_MethodDecl_unknownMethod(ASTNode caller, ASTNode child) {
        return getParent().Define_MethodDecl_unknownMethod(this, caller);
    }
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        return getParent().Define_NameType_nameType(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeNull(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeNull(this, caller);
    }
    public boolean Define_boolean_inStaticContext(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_inStaticContext(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeString(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeString(this, caller);
    }
    public Variable Define_Variable_unknownField(ASTNode caller, ASTNode child) {
        return getParent().Define_Variable_unknownField(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeInt(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeInt(this, caller);
    }
    public String Define_String_hostPackage(ASTNode caller, ASTNode child) {
        return getParent().Define_String_hostPackage(this, caller);
    }
    public boolean Define_boolean_reachable(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_reachable(this, caller);
    }
    public boolean Define_boolean_isDAbefore(ASTNode caller, ASTNode child, Variable v) {
        return getParent().Define_boolean_isDAbefore(this, caller, v);
    }
    public boolean Define_boolean_insideSwitch(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_insideSwitch(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeByte(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeByte(this, caller);
    }
    public TypeDecl Define_TypeDecl_returnType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_returnType(this, caller);
    }
    public TypeDecl Define_TypeDecl_superType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_superType(this, caller);
    }
    public Stmt Define_Stmt_entryBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_Stmt_entryBlock(this, caller);
    }
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }
    public boolean Define_boolean_mayBeStatic(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeStatic(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeFloat(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeFloat(this, caller);
    }
    public Collection Define_Collection_lookupMethod(ASTNode caller, ASTNode child, String name) {
        return getParent().Define_Collection_lookupMethod(this, caller, name);
    }
    public TypeDecl Define_TypeDecl_hostType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_hostType(this, caller);
    }
    public boolean Define_boolean_reachableCatchClause(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_reachableCatchClause(this, caller);
    }
    public Set Define_Set_following(ASTNode caller, ASTNode child) {
        return getParent().Define_Set_following(this, caller);
    }
    public Expr Define_Expr_nestedScope(ASTNode caller, ASTNode child) {
        return getParent().Define_Expr_nestedScope(this, caller);
    }
    public Block Define_Block_hostBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_Block_hostBlock(this, caller);
    }
    public boolean Define_boolean_hasPackage(ASTNode caller, ASTNode child, String packageName) {
        return getParent().Define_boolean_hasPackage(this, caller, packageName);
    }
    public boolean Define_boolean_isExceptionHandlerParameter(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isExceptionHandlerParameter(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeVoid(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeVoid(this, caller);
    }
    public Block Define_Block_getBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_Block_getBlock(this, caller);
    }
    public TypeDecl Define_TypeDecl_enclosingType(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_enclosingType(this, caller);
    }
    public Case Define_Case_bind(ASTNode caller, ASTNode child, Case c) {
        return getParent().Define_Case_bind(this, caller, c);
    }
    public Stmt Define_Stmt_exitBlock(ASTNode caller, ASTNode child) {
        return getParent().Define_Stmt_exitBlock(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeException(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeException(this, caller);
    }
    public boolean Define_boolean_mayBeVolatile(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_mayBeVolatile(this, caller);
    }
    public BodyDecl Define_BodyDecl_enclosingBodyDecl(ASTNode caller, ASTNode child) {
        return getParent().Define_BodyDecl_enclosingBodyDecl(this, caller);
    }
    public boolean Define_boolean_isIncOrDec(ASTNode caller, ASTNode child) {
        return getParent().Define_boolean_isIncOrDec(this, caller);
    }
    public TypeDecl Define_TypeDecl_typeDouble(ASTNode caller, ASTNode child) {
        return getParent().Define_TypeDecl_typeDouble(this, caller);
    }
}
