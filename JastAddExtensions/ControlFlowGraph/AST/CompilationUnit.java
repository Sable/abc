
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


// 7.3 Compilation Units

public class CompilationUnit extends ASTNode<ASTNode> implements Cloneable {
    public void flushCache() {
        super.flushCache();
        packageName_computed = false;
        packageName_value = null;
        lookupType_String_values = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public CompilationUnit clone() throws CloneNotSupportedException {
        CompilationUnit node = (CompilationUnit)super.clone();
        node.packageName_computed = false;
        node.packageName_value = null;
        node.lookupType_String_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
     @SuppressWarnings({"unchecked", "cast"})  public CompilationUnit copy() {
      try {
          CompilationUnit node = (CompilationUnit)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public CompilationUnit fullCopy() {
        CompilationUnit res = (CompilationUnit)copy();
        for(int i = 0; i < getNumChildNoTransform(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in ClassPath.jrag at line 148


  private String relativeName;

    // Declared in ClassPath.jrag at line 149

  private String pathName;

    // Declared in ClassPath.jrag at line 150

  private boolean fromSource;

    // Declared in ClassPath.jrag at line 152


  public void setRelativeName(String name) {
    relativeName = name;
  }

    // Declared in ClassPath.jrag at line 155

  public void setPathName(String name) {
    pathName = name;
  }

    // Declared in ClassPath.jrag at line 158

  public void setFromSource(boolean value) {
    fromSource = value;
  }

    // Declared in ErrorCheck.jrag at line 67


  protected java.util.ArrayList errors = new java.util.ArrayList();

    // Declared in ErrorCheck.jrag at line 68

  protected java.util.ArrayList warnings = new java.util.ArrayList();

    // Declared in ErrorCheck.jrag at line 70


  public Collection parseErrors() { return parseErrors; }

    // Declared in ErrorCheck.jrag at line 71

  public void addParseError(Problem msg) { parseErrors.add(msg); }

    // Declared in ErrorCheck.jrag at line 72

  protected Collection parseErrors = new ArrayList();

    // Declared in ErrorCheck.jrag at line 230


  public void errorCheck(Collection collection) {
    collectErrors();
    collection.addAll(errors);
  }

    // Declared in ErrorCheck.jrag at line 234

  public void errorCheck(Collection err, Collection warn) {
    collectErrors();
    err.addAll(errors);
    warn.addAll(warnings);
  }

    // Declared in NameCheck.jrag at line 35


  public void nameCheck() {
    for(int i = 0; i < getNumImportDecl(); i++) {
      ImportDecl decl = getImportDecl(i);
      if(decl instanceof SingleTypeImportDecl) {
        if(localLookupType(decl.getAccess().type().name()).contains(decl.getAccess().type()))
          error("" + decl + " is conflicting with visible type");
      }
    }
  }

    // Declared in PrettyPrint.jadd at line 44

        
  public void toString(StringBuffer s) {
    try {
      if(!getPackageDecl().equals("")) {
        s.append("package " + getPackageDecl() + ";\n");
      }
      for(int i = 0; i < getNumImportDecl(); i++) {
        getImportDecl(i).toString(s);
      }
      for(int i = 0; i < getNumTypeDecl(); i++) {
        getTypeDecl(i).toString(s);
        s.append("\n");
      }
    } catch (NullPointerException e) {
      System.out.print("Error in compilation unit hosting " + getTypeDecl(0).typeName());
      throw e;
    }
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 4

    public CompilationUnit() {
        super();

        setChild(new List(), 0);
        setChild(new List(), 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 4
    public CompilationUnit(java.lang.String p0, List<ImportDecl> p1, List<TypeDecl> p2) {
        setPackageDecl(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 19


    // Declared in java.ast line 4
    public CompilationUnit(beaver.Symbol p0, List<ImportDecl> p1, List<TypeDecl> p2) {
        setPackageDecl(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 25


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 28

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    protected java.lang.String tokenjava_lang_String_PackageDecl;

    // Declared in java.ast at line 3

    public void setPackageDecl(java.lang.String value) {
        tokenjava_lang_String_PackageDecl = value;
    }

    // Declared in java.ast at line 6

    public int PackageDeclstart;

    // Declared in java.ast at line 7

    public int PackageDeclend;

    // Declared in java.ast at line 8

    public void setPackageDecl(beaver.Symbol symbol) {
        if(symbol.value != null && !(symbol.value instanceof String))
          throw new UnsupportedOperationException("setPackageDecl is only valid for String lexemes");
        tokenjava_lang_String_PackageDecl = (String)symbol.value;
        PackageDeclstart = symbol.getStart();
        PackageDeclend = symbol.getEnd();
    }

    // Declared in java.ast at line 15

    public java.lang.String getPackageDecl() {
        return tokenjava_lang_String_PackageDecl != null ? tokenjava_lang_String_PackageDecl : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    public void setImportDeclList(List<ImportDecl> list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumImportDecl = 0;

    // Declared in java.ast at line 7

    public int getNumImportDecl() {
        return getImportDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


     @SuppressWarnings({"unchecked", "cast"})  public ImportDecl getImportDecl(int i) {
        return (ImportDecl)getImportDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addImportDecl(ImportDecl node) {
        List<ImportDecl> list = getImportDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setImportDecl(ImportDecl node, int i) {
        List<ImportDecl> list = getImportDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List<ImportDecl> getImportDecls() {
        return getImportDeclList();
    }

    // Declared in java.ast at line 27

    public List<ImportDecl> getImportDeclsNoTransform() {
        return getImportDeclListNoTransform();
    }

    // Declared in java.ast at line 31


     @SuppressWarnings({"unchecked", "cast"})  public List<ImportDecl> getImportDeclList() {
        return (List<ImportDecl>)getChild(0);
    }

    // Declared in java.ast at line 35


     @SuppressWarnings({"unchecked", "cast"})  public List<ImportDecl> getImportDeclListNoTransform() {
        return (List<ImportDecl>)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    public void setTypeDeclList(List<TypeDecl> list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumTypeDecl = 0;

    // Declared in java.ast at line 7

    public int getNumTypeDecl() {
        return getTypeDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


     @SuppressWarnings({"unchecked", "cast"})  public TypeDecl getTypeDecl(int i) {
        return (TypeDecl)getTypeDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addTypeDecl(TypeDecl node) {
        List<TypeDecl> list = getTypeDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setTypeDecl(TypeDecl node, int i) {
        List<TypeDecl> list = getTypeDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List<TypeDecl> getTypeDecls() {
        return getTypeDeclList();
    }

    // Declared in java.ast at line 27

    public List<TypeDecl> getTypeDeclsNoTransform() {
        return getTypeDeclListNoTransform();
    }

    // Declared in java.ast at line 31


     @SuppressWarnings({"unchecked", "cast"})  public List<TypeDecl> getTypeDeclList() {
        return (List<TypeDecl>)getChild(1);
    }

    // Declared in java.ast at line 35


     @SuppressWarnings({"unchecked", "cast"})  public List<TypeDecl> getTypeDeclListNoTransform() {
        return (List<TypeDecl>)getChildNoTransform(1);
    }

    // Declared in ClassPath.jrag at line 27
 @SuppressWarnings({"unchecked", "cast"})     public String relativeName() {
        String relativeName_value = relativeName_compute();
        return relativeName_value;
    }

    private String relativeName_compute() {  return relativeName;  }

    // Declared in ClassPath.jrag at line 28
 @SuppressWarnings({"unchecked", "cast"})     public String pathName() {
        String pathName_value = pathName_compute();
        return pathName_value;
    }

    private String pathName_compute() {  return pathName;  }

    // Declared in ClassPath.jrag at line 29
 @SuppressWarnings({"unchecked", "cast"})     public boolean fromSource() {
        boolean fromSource_value = fromSource_compute();
        return fromSource_value;
    }

    private boolean fromSource_compute() {  return fromSource;  }

    // Declared in LookupType.jrag at line 211
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet localLookupType(String name) {
        SimpleSet localLookupType_String_value = localLookupType_compute(name);
        return localLookupType_String_value;
    }

    private SimpleSet localLookupType_compute(String name) {
    for(int i = 0; i < getNumTypeDecl(); i++)
      if(getTypeDecl(i).name().equals(name))
        return SimpleSet.emptySet.add(getTypeDecl(i));
    return SimpleSet.emptySet;
  }

    // Declared in LookupType.jrag at line 218
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet importedTypes(String name) {
        SimpleSet importedTypes_String_value = importedTypes_compute(name);
        return importedTypes_String_value;
    }

    private SimpleSet importedTypes_compute(String name) {
    SimpleSet set = SimpleSet.emptySet;
    for(int i = 0; i < getNumImportDecl(); i++)
      if(!getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          set = set.add(iter.next());
    return set;
  }

    // Declared in LookupType.jrag at line 226
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet importedTypesOnDemand(String name) {
        SimpleSet importedTypesOnDemand_String_value = importedTypesOnDemand_compute(name);
        return importedTypesOnDemand_String_value;
    }

    private SimpleSet importedTypesOnDemand_compute(String name) {
    SimpleSet set = SimpleSet.emptySet;
    for(int i = 0; i < getNumImportDecl(); i++)
      if(getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          set = set.add(iter.next());
    return set;
  }

    // Declared in PrettyPrint.jadd at line 780
 @SuppressWarnings({"unchecked", "cast"})     public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return getClass().getName() + " [" + getPackageDecl() + "]";  }

    protected boolean packageName_computed = false;
    protected String packageName_value;
    // Declared in QualifiedNames.jrag at line 92
 @SuppressWarnings({"unchecked", "cast"})     public String packageName() {
        if(packageName_computed)
            return packageName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        packageName_value = packageName_compute();
        if(isFinal && num == boundariesCrossed)
            packageName_computed = true;
        return packageName_value;
    }

    private String packageName_compute() {  return getPackageDecl();  }

    // Declared in LookupType.jrag at line 99
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    protected java.util.Map lookupType_String_values;
    // Declared in LookupType.jrag at line 171
 @SuppressWarnings({"unchecked", "cast"})     public SimpleSet lookupType(String name) {
        Object _parameters = name;
if(lookupType_String_values == null) lookupType_String_values = new java.util.HashMap(4);
        if(lookupType_String_values.containsKey(_parameters))
            return (SimpleSet)lookupType_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet lookupType_String_value = getParent().Define_SimpleSet_lookupType(this, null, name);
        if(isFinal && num == boundariesCrossed)
            lookupType_String_values.put(_parameters, lookupType_String_value);
        return lookupType_String_value;
    }

    // Declared in TypeAnalysis.jrag at line 493
    public TypeDecl Define_TypeDecl_enclosingType(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return null;
        }
        return getParent().Define_TypeDecl_enclosingType(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 117
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return !exceptionType.isUncheckedException();
        }
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in TypeAnalysis.jrag at line 564
    public String Define_String_hostPackage(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return packageName();
        }
        return getParent().Define_String_hostPackage(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 580
    public TypeDecl Define_TypeDecl_hostType(ASTNode caller, ASTNode child) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return null;
        }
        return getParent().Define_TypeDecl_hostType(this, caller);
    }

    // Declared in NameCheck.jrag at line 27
    public SimpleSet Define_SimpleSet_allImportedTypes(ASTNode caller, ASTNode child, String name) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return importedTypes(name);
        }
        return getParent().Define_SimpleSet_allImportedTypes(this, caller, name);
    }

    // Declared in QualifiedNames.jrag at line 90
    public String Define_String_packageName(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return packageName();
        }
        return getParent().Define_String_packageName(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 530
    public boolean Define_boolean_isMemberType(ASTNode caller, ASTNode child) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return false;
        }
        return getParent().Define_boolean_isMemberType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 51
    public boolean Define_boolean_isIncOrDec(ASTNode caller, ASTNode child) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return false;
        }
        return getParent().Define_boolean_isIncOrDec(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 542
    public boolean Define_boolean_isLocalClass(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return false;
        }
        return getParent().Define_boolean_isLocalClass(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 520
    public boolean Define_boolean_isNestedType(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return false;
        }
        return getParent().Define_boolean_isNestedType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 69
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return NameType.PACKAGE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in LookupType.jrag at line 267
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return lookupType(name);
        }
        if(true) { 
   int childIndex = this.getIndexOfChild(caller);
{
    // locally declared types in compilation unit
    SimpleSet set = localLookupType(name);
    if(!set.isEmpty()) return set;

    // imported types
    set = importedTypes(name);
    if(!set.isEmpty()) return set;

    // types in the same package
    TypeDecl result = lookupType(packageName(), name);
    if(result != null && result.accessibleFromPackage(packageName())) 
      return SimpleSet.emptySet.add(result);
    
    // types imported on demand
    set = importedTypesOnDemand(name);
    if(!set.isEmpty()) return set;
    
    // include primitive types
    result = lookupType(PRIMITIVE_PACKAGE_NAME, name);
    if(result != null) return SimpleSet.emptySet.add(result);
    
    // 7.5.5 Automatic Imports
    result = lookupType("java.lang", name);
    if(result != null && result.accessibleFromPackage(packageName()))
      return SimpleSet.emptySet.add(result);
    return lookupType(name);
  }
}
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

    // Declared in ClassPath.jrag at line 32
    public CompilationUnit Define_CompilationUnit_compilationUnit(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return this;
        }
        return getParent().Define_CompilationUnit_compilationUnit(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
