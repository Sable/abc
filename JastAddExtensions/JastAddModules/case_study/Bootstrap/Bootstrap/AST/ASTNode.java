module jastadd$framework;

import java.util.Stack;
import java.util.*;

public class ASTNode<T extends ASTNode> extends beaver.Symbol  implements Cloneable, Iterable<T> {
  public ASTNode() {
    super();
    init$children();
  }

  protected void init$children() { }

  public void flushCache() { }

  /* Alternative implementation for no static
  private ASTNode$State state = null;
  public final ASTNode$State state() {
    if(state == null)
      state = parent == null ? new ASTNode$State() : parent.state();
    return state;
  }
  */
  protected static ASTNode$State state = new ASTNode$State();
  public final ASTNode$State state() {
    return state;
  };

  // begin rewriteEnabled
  // begin rewriteLimit > 0
  public void debugRewrite(String info) {
    if(!parent.is$Final()) return;
    java.util.ArrayList key = new java.util.ArrayList(2);
    key.add(getParent());
    key.add(new Integer(getParent().getIndexOfChild(this)));
    java.util.ArrayList list;
    if(state().debugRewrite.containsKey(key))
      list = (java.util.ArrayList)state().debugRewrite.get(key);
    else {
      list = new java.util.ArrayList();
      state().debugRewrite.put(key, list);
    }
    list.add(info);
    if(list.size() > 100) { // Set rewrite limit here
      StringBuffer buf = new StringBuffer("Iteration count exceeded for rewrite:");
      for(java.util.Iterator iter = list.iterator(); iter.hasNext(); ) buf.append("\n" + iter.next());
      throw new RuntimeException(buf.toString());
    }
  }
  public void debugRewriteRemove() {
    java.util.ArrayList key = new java.util.ArrayList(2);
    key.add(getParent());
    key.add(new Integer(getParent().getIndexOfChild(this)));
    state().debugRewrite.remove(key);
  }
  // end rewriteLimit > 0
  public boolean in$Circle = false;
  public boolean in$Circle() { return in$Circle; }
  public void in$Circle(boolean b) { in$Circle = b; }
  public boolean is$Final = false;
  public boolean is$Final() { return is$Final; }
  public void is$Final(boolean b) { is$Final = b; }
  /*@SuppressWarnings("cast")*/ public T getChild(int i) {
    return (T)ASTNode.getChild(this, i);
  }

  public static ASTNode getChild(ASTNode that, int i) {
    ASTNode node = that.getChildNoTransform(i);
    if(node.is$Final()) return node;
    if(!node.mayHaveRewrite()) {
      node.is$Final(that.is$Final());
      return node;
    }
    if(!node.in$Circle()) {
      int rewriteState;
      int num = that.state().boundariesCrossed;
      do {
        that.state().push(ASTNode$State.REWRITE_CHANGE);
        ASTNode oldNode = node;
        oldNode.in$Circle(true);
        node = node.rewriteTo();
        if(node != oldNode)
          that.setChild(node, i);
        oldNode.in$Circle(false);
        rewriteState = that.state().pop();
      } while(rewriteState == ASTNode$State.REWRITE_CHANGE);
      if(rewriteState == ASTNode$State.REWRITE_NOCHANGE && that.is$Final()) {
        node.is$Final(true);
        that.state().boundariesCrossed = num;
        // node.debugRewriteRemove()
      }
    }
    else if(that.is$Final() != node.is$Final()) that.state().boundariesCrossed++;
    return node;
  }
  // end rewriteEnabled

  private int childIndex;
  public int getIndexOfChild(ASTNode node) {
    if(node != null && node.childIndex < getNumChildNoTransform() && node == getChildNoTransform(node.childIndex))
      return node.childIndex;
    for(int i = 0; i < getNumChildNoTransform(); i++)
      if(getChildNoTransform(i) == node) {
        node.childIndex = i;
        return i;
      }
    return -1;
  }

  public void addChild(T node) {
    setChild(node, getNumChildNoTransform());
  }
  /*@SuppressWarnings("cast")*/ public final T getChildNoTransform(int i) {
    return (T)children[i];
  }

  protected ASTNode parent;
  protected ASTNode[] children;
  protected int numChildren;
  protected int numChildren() {
    return numChildren;
  }

  public int getNumChild() {
    return numChildren();
  }
  public final int getNumChildNoTransform() {
    return numChildren();
  }

  public void setChild(T node, int i) {
    // debugNodeAttachment(node);
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

  public void insertChild(T node, int i) {
    if(i > numChildren)
      throw new Error("insertChild error: can not insert child at position outside list of elements");
    // debugNodeAttachment(node);
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

  public ASTNode getParent() {
    if(parent != null && ((ASTNode)parent).is$Final() != is$Final()) {
      state().boundariesCrossed++;
    }
    return (ASTNode)parent;
  }

  public void setParent(ASTNode node) {
    parent = node;
  }
  protected boolean debugNodeAttachmentIsRoot() { return false; }
  private static void debugNodeAttachment(ASTNode node) {
      if(node == null) throw new RuntimeException("Trying to assign null to a tree child node");
      while(node != null && !node.debugNodeAttachmentIsRoot()) {
          if(node.in$Circle()) return;
          ASTNode parent = node.parent;
          if(parent != null && parent.getIndexOfChild(node) == -1) return;
          node = parent;
      }
      if(node == null) return;
      throw new RuntimeException("Trying to insert the same tree at multiple tree locations");
  }
  

  public ASTNode insertList(List newList, int i) {
    // insert list newlist at position i and return first element in newlist
    setChild((T)newList.getChildNoTransform(0), i);
    for(int j = 1; j < newList.getNumChild(); j++)
      insertChild((T)newList.getChildNoTransform(j), ++i);
    return newList.getChildNoTransform(0);
  }

  public java.util.Iterator<T> iterator() {
    return new java.util.Iterator<T>() {
      private int counter = 0;
      public boolean hasNext() {
        return counter < getNumChild();
      }
      /*@SuppressWarnings("unchecked")*/ public T next() {
         if(hasNext())
           return (T)getChild(counter++);
         else
           return null;
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public ASTNode rewriteTo() {
    if(state().peek() == ASTNode$State.REWRITE_CHANGE) {
      state().pop();
      state().push(ASTNode$State.REWRITE_NOCHANGE);
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
