
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;
public class List extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        List node = (List)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          List node = (List)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        List res = (List)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in ASTUtil.jrag at line 3

	
	public Collection toCollection() {
		Collection children = new ArrayList();
		for(int i=0;i<getNumChild();++i)
			children.add(getChild(i));
		return children;
	}

    // Declared in ASTUtil.jrag at line 10

	
	public static List ofCollection(Collection c) {
		List l = new List();
		for(Iterator i=c.iterator();i.hasNext();)
			l.add((ASTNode)i.next());
		return l;
	}

    // Declared in List.ast at line 3
    // Declared in List.ast line 0

    public List() {
        super();


    }

    // Declared in List.ast at line 9


     public List add(ASTNode node) {
          addChild(node);
          return this;
     }

    // Declared in List.ast at line 14


     public void insertChild(ASTNode node, int i) {
          list$touched = true;
          super.insertChild(node, i);
     }

    // Declared in List.ast at line 18

     public void addChild(ASTNode node) {
          list$touched = true;
          super.addChild(node);
     }

    // Declared in List.ast at line 22

     public void removeChild(int i) {
          list$touched = true;
          super.removeChild(i);
     }

    // Declared in List.ast at line 26

     public int getNumChild() {
          if(list$touched) {
              for(int i = 0; i < numChildren(); i++)
                  getChild(i);
              list$touched = false;
          }
          return numChildren();
     }

    // Declared in List.ast at line 34

     private boolean list$touched = true;

    // Declared in List.ast at line 35

  public boolean mayHaveRewrite() { return false; }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
