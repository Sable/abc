
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

public class ArrayTypeAccess extends TypeAccess implements Cloneable {
    public void flushCache() {
        super.flushCache();
        getPackage_computed = false;
        getPackage_value = null;
        getID_computed = false;
        getID_value = null;
        decl_computed = false;
        decl_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        ArrayTypeAccess node = (ArrayTypeAccess)super.clone();
        node.getPackage_computed = false;
        node.getPackage_value = null;
        node.getID_computed = false;
        node.getID_value = null;
        node.decl_computed = false;
        node.decl_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          ArrayTypeAccess node = (ArrayTypeAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ArrayTypeAccess res = (ArrayTypeAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NameCheck.jrag at line 138

    
  
  public void nameCheck() {
    if(decl().elementType().isUnknown())
      error("no type named " + decl().elementType().typeName());
  }

    // Declared in PrettyPrint.jadd at line 621

  
  public void toString(StringBuffer s) {
    super.toString(s);
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 22

    public ArrayTypeAccess() {
        super();

        setChild(null, 0);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 22
    public ArrayTypeAccess(Access p0, int p1) {
        setChild(p0, 0);
        setDimension(p1);
    }

    // Declared in java.ast at line 16


  protected int numChildren() {
    return 1;
  }

    // Declared in java.ast at line 19

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 22
    public void setAccess(Access node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Access getAccess() {
        return (Access)getChild(0);
    }

    // Declared in java.ast at line 9


    public Access getAccessNoTransform() {
        return (Access)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 22
    private int tokenint_Dimension;

    // Declared in java.ast at line 3

    public void setDimension(int value) {
        tokenint_Dimension = value;
    }

    // Declared in java.ast at line 6

    public int getDimension() {
        return tokenint_Dimension;
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 22
    private String tokenString_Package;

    // Declared in java.ast at line 3

    public void setPackage(String value) {
        tokenString_Package = value;
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 22
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    protected boolean getPackage_computed = false;
    protected String getPackage_value;
    // Declared in Arrays.jrag at line 49
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

    private String getPackage_compute() {  return  getAccess().type().packageName();  }

    protected boolean getID_computed = false;
    protected String getID_value;
    // Declared in Arrays.jrag at line 50
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

    private String getID_compute() {  return  getAccess().type().name();  }

    // Declared in LookupType.jrag at line 148
    public TypeDecl decl() {
        if(decl_computed)
            return decl_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        decl_value = decl_compute();
        if(isFinal && num == boundariesCrossed)
            decl_computed = true;
        return decl_value;
    }

    private TypeDecl decl_compute()  {
    TypeDecl typeDecl = getAccess().type();
    for(int i = 0; i < getDimension(); i++)
      typeDecl = typeDecl.arrayType();
    return typeDecl;
  }

    // Declared in PrettyPrint.jadd at line 940
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getDimension() + "]";  }

    // Declared in SyntacticClassification.jrag at line 119
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.AMBIGUOUS_NAME;  }

    // Declared in TypeHierarchyCheck.jrag at line 146
    public boolean staticContextQualifier() {
        boolean staticContextQualifier_value = staticContextQualifier_compute();
        return staticContextQualifier_value;
    }

    private boolean staticContextQualifier_compute() {  return  true;  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
