
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;
public class List<T extends ASTNode> extends ASTNode<T> implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
     @SuppressWarnings({"unchecked", "cast"})  public List<T> clone() throws CloneNotSupportedException {
        List node = (List)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
     @SuppressWarnings({"unchecked", "cast"})  public List<T> copy() {
      try {
          List node = (List)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public List<T> fullCopy() {
        List res = (List)copy();
        for(int i = 0; i < getNumChildNoTransform(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in List.ast at line 3
    // Declared in List.ast line 0

    public List() {
        super();


    }

    // Declared in List.ast at line 9


     public List<T> add(T node) {
          addChild(node);
          return this;
     }

    // Declared in List.ast at line 14


     public void insertChild(T node, int i) {
          list$touched = true;
          super.insertChild(node, i);
     }

    // Declared in List.ast at line 18

     public void addChild(T node) {
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
              for(int i = 0; i < getNumChildNoTransform(); i++)
                  getChild(i);
              list$touched = false;
          }
          return getNumChildNoTransform();
     }

    // Declared in List.ast at line 34

     private boolean list$touched = true;

    // Declared in List.ast at line 35

  public boolean mayHaveRewrite() { return false; }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
