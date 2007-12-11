
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;
public class PackageDecl extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
        PackageDecl_prefixUses_visited = false;
        PackageDecl_prefixUses_computed = false;
        PackageDecl_prefixUses_value = null;
    PackageDecl_prefixUses_contributors = new java.util.HashSet();
    }
    public Object clone() throws CloneNotSupportedException {
        PackageDecl node = (PackageDecl)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          PackageDecl node = (PackageDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        PackageDecl res = (PackageDecl)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PackageName.ast at line 3
    // Declared in PackageName.ast line 1

    public PackageDecl() {
        super();

        is$Final(true);

    }

    // Declared in PackageName.ast at line 11


    // Declared in PackageName.ast line 1
    public PackageDecl(String p0) {
        setName(p0);
        is$Final(true);
    }

    // Declared in PackageName.ast at line 16


  protected int numChildren() {
    return 0;
  }

    // Declared in PackageName.ast at line 19

  public boolean mayHaveRewrite() { return false; }

    // Declared in PackageName.ast at line 2
    // Declared in PackageName.ast line 1
    private String tokenString_Name;

    // Declared in PackageName.ast at line 3

    public void setName(String value) {
        tokenString_Name = value;
    }

    // Declared in PackageName.ast at line 6

    public String getName() {
        return tokenString_Name != null ? tokenString_Name : "";
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected boolean PackageDecl_prefixUses_visited = false;
    protected boolean PackageDecl_prefixUses_computed = false;
    protected HashSet PackageDecl_prefixUses_value;
    // Declared in Uses.jrag at line 89
    public HashSet prefixUses() {
        if(PackageDecl_prefixUses_computed)
            return PackageDecl_prefixUses_value;
        if(PackageDecl_prefixUses_visited)
            throw new RuntimeException("Circular definition of attr: prefixUses in class: ");
        PackageDecl_prefixUses_visited = true;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        PackageDecl_prefixUses_value = prefixUses_compute();
        if(isFinal && num == boundariesCrossed)
            PackageDecl_prefixUses_computed = true;
        PackageDecl_prefixUses_visited = false;
        return PackageDecl_prefixUses_value;
    }

    java.util.HashSet PackageDecl_prefixUses_contributors = new java.util.HashSet();
    private HashSet prefixUses_compute() {
        ASTNode node = this;
        while(node.getParent() != null)
            node = node.getParent();
        Program root = (Program)node;
        root.collect_contributors_PackageDecl_prefixUses();
        PackageDecl_prefixUses_value = new HashSet();
        for(java.util.Iterator iter = PackageDecl_prefixUses_contributors.iterator(); iter.hasNext(); ) {
            ASTNode contributor = (ASTNode)iter.next();
            contributor.contributeTo_PackageDecl_PackageDecl_prefixUses(PackageDecl_prefixUses_value);
        }
        return PackageDecl_prefixUses_value;
    }

}
