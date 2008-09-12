package test;

public class ASTNode$State {
  // circular enabled begin
  public boolean IN_CIRCLE = false;
  public int CIRCLE_INDEX;
  public boolean CHANGE = false;
  public boolean LAST_CYCLE = false; // cache cycle
  public boolean RESET_CYCLE = false;

  // component check begin
  public java.util.Set circularEvalSet = new java.util.HashSet();
  public java.util.Stack circularEvalStack = new java.util.Stack();

  static class CircularEvalEntry {
  	ASTNode node;
  	String attrName;
  	Object parameters;
  	
  	public CircularEvalEntry(ASTNode node, String attrName, Object parameters) {
  		this.node = node;
  		this.attrName = attrName;
  		this.parameters = parameters;
  	}
  	
  	public boolean equals(Object rhs) {
  		CircularEvalEntry s = (CircularEvalEntry) rhs;
  		if (parameters == null && s.parameters == null)
  			return node == s.node && attrName.equals(s.attrName);
  		else if (parameters != null && s.parameters != null)
  			return node == s.node && attrName.equals(s.attrName) && parameters.equals(s.parameters);
  		else
  			return false;
  	} 
  
  	
  	public int hashCode() {
  		return node.hashCode();
  	}
  }

  public void addEvalEntry(ASTNode node, String attrName, Object parameters) {
  	circularEvalSet.add(new CircularEvalEntry(node,attrName,parameters));
  }
  
  public boolean containsEvalEntry(ASTNode node, String attrName, Object parameters) {
  	return circularEvalSet.contains(new CircularEvalEntry(node,attrName,parameters));
  }
  
  static class CircularStackEntry {
  	java.util.Set circularEvalSet;
  	boolean changeValue;
  	
  	public CircularStackEntry(java.util.Set set, boolean change) {
  		circularEvalSet = set;
  		changeValue = change;
  	}
  }
  
  public void pushEvalStack() {
  	circularEvalStack.push(new CircularStackEntry(circularEvalSet, CHANGE));
  	circularEvalSet = new java.util.HashSet();
  	CHANGE = false;
  }
  
  public void popEvalStack() {
  	CircularStackEntry c = (CircularStackEntry) circularEvalStack.pop();
  	circularEvalSet = c.circularEvalSet;
  	CHANGE = c.changeValue;
  }
  // component check end
  // circular end
 
  // rewrite begin
  public static final int REWRITE_CHANGE = 1;
  public static final int REWRITE_NOCHANGE = 2;
  public static final int REWRITE_INTERRUPT = 3;

  public java.util.HashMap debugRewrite = new java.util.HashMap(); // rewriteLimit > 0

  public int boundariesCrossed = 0;

  private int[] stack;
  private int pos;
  public ASTNode$State() {
    stack = new int[64];
    pos = 0;
  }
  private void ensureSize(int size) {
    if(size < stack.length)
      return;
    int[] newStack = new int[stack.length * 2];
    System.arraycopy(stack, 0, newStack, 0, stack.length);
    stack = newStack;
  }
  public void push(int i) {
    ensureSize(pos+1);
    stack[pos++] = i;
  }
  public int pop() {
    return stack[--pos];
  }
  public int peek() {
    return stack[pos-1];
  }
  // rewrite end

  public void reset() {
      IN_CIRCLE = false;
      CIRCLE_INDEX = 0;
      CHANGE = false;
      boundariesCrossed = 0;
  }
}
