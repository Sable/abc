
import java.util.Stack;
import java.util.*;

public class ASTNode extends beaver.Symbol  implements Cloneable {
  public ASTNode() {
    super();
    init$children();
  }

  protected void init$children() { }

  static public boolean IN_CIRCLE = false;
  static public boolean CHANGE = false;
  static public boolean LAST_CYCLE = false;
  static public Set circularEvalSet = new HashSet();
  static public Stack circularEvalStack = new Stack();
  
  public static class CircularEvalEntry {
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
  
  
  
  public static class CircularStackEntry {
  	Set circularEvalSet;
  	boolean changeValue;
  	
  	public CircularStackEntry(Set set, boolean change) {
  		circularEvalSet = set;
  		changeValue = change;
  	}
  }
  
  public void pushEvalStack() {
  	circularEvalStack.push(new CircularStackEntry(circularEvalSet, CHANGE));
  	circularEvalSet = new HashSet();
  	CHANGE = false;
  }
  
  public void popEvalStack() {
  	CircularStackEntry c = (CircularStackEntry) circularEvalStack.pop();
  	circularEvalSet = c.circularEvalSet;
  	CHANGE = c.changeValue;
  }
 
  
  public static int boundariesCrossed = 0;

  public static class State {
    private int[] stack;
    private int pos;
    public State() {
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
  }
  protected static State state = new State();
  public boolean inCircle = false;
  public boolean is$Final = false;
  protected static final int REWRITE_CHANGE = 1;
  protected static final int REWRITE_NOCHANGE = 2;
  protected static final int REWRITE_INTERRUPT = 3;
  public ASTNode getChild(int i) {
    return ASTNode.getChild(this, i);
  }
  public static ASTNode getChild(ASTNode that, int i) {
    ASTNode node = that.getChildNoTransform(i);
    if(node.is$Final) return node;
    if(!node.mayHaveRewrite()) {
      node.is$Final = that.is$Final;
      return node;
    }
    if(!node.inCircle) {
      int rewriteState;
      int num = ASTNode.boundariesCrossed;
      do {
        ASTNode.state.push(ASTNode.REWRITE_CHANGE);
        ASTNode oldNode = node;
        oldNode.inCircle = true;
        node = node.rewriteTo();
        oldNode.inCircle = false;
        that.setChild(node, i);
        rewriteState = state.pop();
      } while(rewriteState == ASTNode.REWRITE_CHANGE);
      if(rewriteState == ASTNode.REWRITE_NOCHANGE && that.is$Final) {
        node.is$Final = true;
        ASTNode.boundariesCrossed = num;
      }
    }
    else if(that.is$Final != node.is$Final) boundariesCrossed++;
    return node;
  }

  private int childIndex;
  public int getIndexOfChild(ASTNode node) {
    if(node.childIndex < getNumChild() && node == getChildNoTransform(node.childIndex))
      return node.childIndex;
    for(int i = 0; i < getNumChild(); i++)
      if(getChildNoTransform(i) == node) {
        node.childIndex = i;
        return i;
      }
    return -1;
  }

  public void addChild(ASTNode node) {
    setChild(node, getNumChild());
  }
  public ASTNode getChildNoTransform(int i) {
    return children[i];
  }
  protected ASTNode parent;
  protected ASTNode[] children;
  protected int numChildren;
  public int getNumChild() {
    return numChildren;
  }
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
  public void insertChild(ASTNode node, int i) {
    if(i > numChildren)
      throw new Error("insertChild error: can not insert child at position outside list of elements");
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

  public ASTNode insertList(List newList, int i) {
    // insert list newlist at position i and return first element in newlist
    setChild(newList.getChildNoTransform(0), i);
    for(int j = 1; j < newList.getNumChild(); j++)
      insertChild(newList.getChildNoTransform(j), ++i);
    return newList.getChildNoTransform(0);
  }
  
  public ASTNode getParent() {
    if(parent != null && parent.is$Final != is$Final) {
      boundariesCrossed++;
    }
    return parent;
  }
  public void setParent(ASTNode node) {
    parent = node;
  }
  public ASTNode rewriteTo() {
    if(state.peek() == ASTNode.REWRITE_CHANGE) {
      state.pop();
      state.push(ASTNode.REWRITE_NOCHANGE);
    }
    return this;
  }

  public boolean mayHaveRewrite() {
    return false;
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
    ASTNode res = copy();
    for(int i = 0; i < getNumChild(); i++) {
      ASTNode node = getChildNoTransform(i);
      if(node != null) node = node.fullCopy();
      res.setChild(node, i);
    }
    return res;
  }


}
