
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;

public class PackageAccess extends Access implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        PackageAccess node = (PackageAccess)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          PackageAccess node = (PackageAccess)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        PackageAccess res = (PackageAccess)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in NameCheck.jrag at line 33


  public void nameCheck() {
    if(!hasPackage(packageName())) {
      error(packageName() + " not found");
    }
  }

    // Declared in NodeConstructors.jrag at line 13


  public PackageAccess(String name, int start, int end) {
    this(name);
    this.start = start;
    this.end = end;
  }

    // Declared in PrettyPrint.jadd at line 617


  public void toString(StringBuffer s) {
    s.append(getPackage());
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 25

    public PackageAccess() {
        super();


    }

    // Declared in java.ast at line 10


    // Declared in java.ast line 25
    public PackageAccess(String p0) {
        setPackage(p0);
    }

    // Declared in java.ast at line 14


  protected int numChildren() {
    return 0;
  }

    // Declared in java.ast at line 17

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 25
    private String tokenString_Package;

    // Declared in java.ast at line 3

    public void setPackage(String value) {
        tokenString_Package = value;
    }

    // Declared in java.ast at line 6

    public String getPackage() {
        return tokenString_Package != null ? tokenString_Package : "";
    }

    // Declared in LookupType.jrag at line 75
    public boolean hasQualifiedPackage(String packageName) {
        boolean hasQualifiedPackage_String_value = hasQualifiedPackage_compute(packageName);
        return hasQualifiedPackage_String_value;
    }

    private boolean hasQualifiedPackage_compute(String packageName) {  return 
    hasPackage(packageName() + "." + packageName);  }

    // Declared in LookupType.jrag at line 296
    public SimpleSet qualifiedLookupType(String name) {
        SimpleSet qualifiedLookupType_String_value = qualifiedLookupType_compute(name);
        return qualifiedLookupType_String_value;
    }

    private SimpleSet qualifiedLookupType_compute(String name)  {
    SimpleSet c = SimpleSet.emptySet;
    TypeDecl typeDecl = lookupType(packageName(), name);
    if(nextAccess() instanceof ClassInstanceExpr) {
      if(typeDecl != null && typeDecl.accessibleFrom(hostType()))
        c = c.add(typeDecl);
      return c;
    }
    else {
      if(typeDecl != null) {
        if(hostType() != null && typeDecl.accessibleFrom(hostType()))
          c = c.add(typeDecl);
        else if(hostType() == null && typeDecl.accessibleFromPackage(hostPackage()))
          c = c.add(typeDecl);
      }
      return c;
    }
  }

    // Declared in LookupVariable.jrag at line 153
    public SimpleSet qualifiedLookupVariable(String name) {
        SimpleSet qualifiedLookupVariable_String_value = qualifiedLookupVariable_compute(name);
        return qualifiedLookupVariable_String_value;
    }

    private SimpleSet qualifiedLookupVariable_compute(String name) {  return  SimpleSet.emptySet;  }

    // Declared in PrettyPrint.jadd at line 941
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getPackage() + "]";  }

    // Declared in QualifiedNames.jrag at line 14
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getPackage();  }

    // Declared in QualifiedNames.jrag at line 19
    public String packageName() {
        String packageName_value = packageName_compute();
        return packageName_value;
    }

    private String packageName_compute()  {
    StringBuffer s = new StringBuffer();
    if(hasPrevExpr()) {
      s.append(prevExpr().packageName());
      s.append(".");
    }
    s.append(getPackage());
    return s.toString();
  }

    // Declared in ResolveAmbiguousNames.jrag at line 30
    public boolean isPackageAccess() {
        boolean isPackageAccess_value = isPackageAccess_compute();
        return isPackageAccess_value;
    }

    private boolean isPackageAccess_compute() {  return  true;  }

    // Declared in SyntacticClassification.jrag at line 58
    public NameType predNameType() {
        NameType predNameType_value = predNameType_compute();
        return predNameType_value;
    }

    private NameType predNameType_compute() {  return  NameType.PACKAGE_NAME;  }

    // Declared in TypeHierarchyCheck.jrag at line 12
    public boolean isUnknown() {
        boolean isUnknown_value = isUnknown_compute();
        return isUnknown_value;
    }

    private boolean isUnknown_compute() {  return  !hasPackage(packageName());  }

    // Declared in AccessField.jrag at line 213
    public Access qualifiedAccessField(FieldDeclaration fd) {
        Access qualifiedAccessField_FieldDeclaration_value = qualifiedAccessField_compute(fd);
        return qualifiedAccessField_FieldDeclaration_value;
    }

    private Access qualifiedAccessField_compute(FieldDeclaration fd) {  return  null;  }

    // Declared in AccessMethod.jrag at line 51
    public Access qualifiedAccessMethod(MethodDecl md, List args) {
        Access qualifiedAccessMethod_MethodDecl_List_value = qualifiedAccessMethod_compute(md, args);
        return qualifiedAccessMethod_MethodDecl_List_value;
    }

    private Access qualifiedAccessMethod_compute(MethodDecl md, List args) {  return  null;  }

    // Declared in AccessType.jrag at line 157
    public Access qualifiedAccessType(TypeDecl td, boolean ambiguous) {
        Access qualifiedAccessType_TypeDecl_boolean_value = qualifiedAccessType_compute(td, ambiguous);
        return qualifiedAccessType_TypeDecl_boolean_value;
    }

    private Access qualifiedAccessType_compute(TypeDecl td, boolean ambiguous)  {
		if(lookupType(packageName(), td.getID()) == td)
			return new TypeAccess(td.getID());
		else
			return null;
	}

    // Declared in NameCheck.jrag at line 226
    public boolean hasPackage(String packageName) {
        boolean hasPackage_String_value = getParent().Define_boolean_hasPackage(this, null, packageName);
        return hasPackage_String_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

    protected void collect_contributors_PackageDecl_prefixUses() {
        // Declared in Uses.jrag at line 75
        {
            PackageDecl ref = (PackageDecl)(findPackageDecl(name().split("\\.")[0]));
            if(ref != null)
                ref.PackageDecl_prefixUses_contributors.add(this);
        }
        super.collect_contributors_PackageDecl_prefixUses();
    }
    protected void contributeTo_PackageDecl_PackageDecl_prefixUses(HashSet collection) {
        collection.add(this);
    }

}
