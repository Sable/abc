
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;

public class PrimitiveTypeAccess extends TypeAccess implements Cloneable {
    public void flushCache() {
        super.flushCache();
        decls_computed = false;
        decls_value = null;
        getPackage_computed = false;
        getPackage_value = null;
        getID_computed = false;
        getID_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        PrimitiveTypeAccess node = (PrimitiveTypeAccess)super.clone();
        node.decls_computed = false;
        node.decls_value = null;
        node.getPackage_computed = false;
        node.getPackage_value = null;
        node.getID_computed = false;
        node.getID_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          PrimitiveTypeAccess node = (PrimitiveTypeAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        PrimitiveTypeAccess res = (PrimitiveTypeAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 21

    public PrimitiveTypeAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 21
    public PrimitiveTypeAccess(String p0) {
        setName(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 21
    private String tokenString_Name;

    // Declared in java.ast at line 3

    public void setName(String value) {
        tokenString_Name = value;
    }

    // Declared in java.ast at line 6

    public String getName() {
        return tokenString_Name != null ? tokenString_Name : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 21
    private String tokenString_Package;

    // Declared in java.ast at line 3

    public void setPackage(String value) {
        tokenString_Package = value;
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 21
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in LookupType.jrag at line 136
    public SimpleSet decls() {
        if(decls_computed)
            return decls_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decls_value = decls_compute();
        if(isFinal && num == boundariesCrossed)
            decls_computed = true;
        return decls_value;
    }

    private SimpleSet decls_compute() {  return  lookupType(PRIMITIVE_PACKAGE_NAME, name());  }

    protected boolean getPackage_computed = false;
    protected String getPackage_value;
    // Declared in LookupType.jrag at line 137
    public String getPackage() {
        if(getPackage_computed)
            return getPackage_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getPackage_value = getPackage_compute();
            setPackage(getPackage_value);
        if(isFinal && num == boundariesCrossed)
            getPackage_computed = true;
        return getPackage_value;
    }

    private String getPackage_compute() {  return  PRIMITIVE_PACKAGE_NAME;  }

    protected boolean getID_computed = false;
    protected String getID_value;
    // Declared in LookupType.jrag at line 138
    public String getID() {
        if(getID_computed)
            return getID_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getID_value = getID_compute();
            setID(getID_value);
        if(isFinal && num == boundariesCrossed)
            getID_computed = true;
        return getID_value;
    }

    private String getID_compute() {  return  getName();  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
