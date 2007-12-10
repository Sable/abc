
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;


// 7.3 Compilation Units
public class CompilationUnit extends ASTNode implements Cloneable,  Named {
    public void flushCache() {
        super.flushCache();
        localLookupType_String_values = null;
        packageName_computed = false;
        packageName_value = null;
        lookupType_String_values = null;
        accessType_TypeDecl_boolean_values = null;
    }
    public Object clone() throws CloneNotSupportedException {
        CompilationUnit node = (CompilationUnit)super.clone();
        node.localLookupType_String_values = null;
        node.packageName_computed = false;
        node.packageName_value = null;
        node.lookupType_String_values = null;
        node.accessType_TypeDecl_boolean_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          CompilationUnit node = (CompilationUnit)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        CompilationUnit res = (CompilationUnit)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in ClassPath.jrag at line 136


  private String relativeName;

    // Declared in ClassPath.jrag at line 137

  private String pathName;

    // Declared in ClassPath.jrag at line 138

  private boolean fromSource;

    // Declared in ClassPath.jrag at line 140


  public void setRelativeName(String name) {
    relativeName = name;
  }

    // Declared in ClassPath.jrag at line 143

  public void setPathName(String name) {
    pathName = name;
  }

    // Declared in ClassPath.jrag at line 146

  public void setFromSource(boolean value) {
    fromSource = value;
  }

    // Declared in ErrorCheck.jrag at line 63


  protected java.util.ArrayList errors = new java.util.ArrayList();

    // Declared in ErrorCheck.jrag at line 64

  protected java.util.ArrayList warnings = new java.util.ArrayList();

    // Declared in ErrorCheck.jrag at line 66


  public Collection parseErrors() { return parseErrors; }

    // Declared in ErrorCheck.jrag at line 67

  public void addParseError(Problem msg) { parseErrors.add(msg); }

    // Declared in ErrorCheck.jrag at line 68

  protected Collection parseErrors = new ArrayList();

    // Declared in ErrorCheck.jrag at line 226


  public void errorCheck(Collection collection) {
    collectErrors();
    collection.addAll(errors);
  }

    // Declared in ErrorCheck.jrag at line 230

  public void errorCheck(Collection err, Collection warn) {
    collectErrors();
    err.addAll(errors);
    warn.addAll(warnings);
  }

    // Declared in NameCheck.jrag at line 23


  public void nameCheck() {
    for(int i = 0; i < getNumImportDecl(); i++) {
      ImportDecl decl = getImportDecl(i);
      if(decl instanceof SingleTypeImportDecl) {
        if(!localLookupType(decl.getAccess().type().name()).contains(decl.getAccess().type()))
          error("" + decl + " is conflicting with visible type");
      }
    }
  }

    // Declared in PrettyPrint.jadd at line 34

        
  public void toString(StringBuffer s) {
    try {
      if(!getPackageDecl().equals("")) {
        s.append("package " + getPackageDecl() + ";\n");
      }
      for(int i = 0; i < getNumImportDecl(); i++) {
        getImportDecl(i).toString(s);
        s.append("\n");
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

    // Declared in ASTUtil.jrag at line 126

	
	public String getID() {
	    char pathsep = File.separatorChar;
	    String path = pathName();
	    int i = path.lastIndexOf(pathsep);
		String relname_tail = i == -1 ? path : path.substring(i+1);
	    int j = relname_tail.lastIndexOf(".");
	    return relname_tail.substring(0, j);	 		
	}

    // Declared in ExtractBlock.jrag at line 3

	
	public void extractBlock(Stmt begin, Stmt end) 
			throws RefactoringException {
		check_block_extraction_preconds(begin, end);
		Block begin_host = begin.hostBlock();
		int begin_idx = begin.indexInHostBlock();
		int end_idx = end.indexInHostBlock();
		begin_host.encapsulate(begin_idx, end_idx);
	}

    // Declared in ExtractBlock.jrag at line 12

	
	private void check_block_extraction_preconds(Stmt begin, Stmt end)
			throws RefactoringException {
		if(begin.isInitOrUpdateStmt() || end.isInitOrUpdateStmt())
			throw new RefactoringException("selection cannot start or end at init or update statements");
		if(!begin.dominates(end))
			throw new RefactoringException("begin must dominate end");
		Block begin_host = begin.hostBlock();
		Block end_host = end.hostBlock();
		if(begin_host == null || end_host == null)
			throw new RefactoringException("invalid statement for extraction");
		if(begin_host != end_host)
			throw new RefactoringException("selection straddles block borders");
		int begin_idx = begin.indexInHostBlock();
		int end_idx = end.indexInHostBlock();
		for(int i=begin_idx;i<=end_idx;++i)
			if(begin_host.getStmt(i) instanceof Case)
				throw new RefactoringException("selection cannot contain case labels");
	}

    // Declared in MakeMethod.jrag at line 4

	
	public void makeMethod(String name, String vis, Block blk) 
			throws RefactoringException {
		if(blk.getNumStmt() > 1) {
			Stmt fst = blk.getStmt(0);
			Stmt lst = blk.getStmt(blk.getNumStmt()-1);
			if(!lst.post_dominates(fst))
				throw new RefactoringException("end must post-dominate begin");
		}
		Block host = blk.hostBlock();
		if(host == null)
			throw new RefactoringException("block to extract must be inside some other block");
		BodyDecl bd = blk.hostBodyDecl();
		boolean static_ctxt = false;
		if(bd instanceof StaticInitializer)
			static_ctxt = true;
		if(bd instanceof MethodDecl && ((MethodDecl)bd).isStatic())
			static_ctxt = true;
		host.createMethod(name, vis, blk.indexInHostBlock(), blk, static_ctxt);
	}

    // Declared in Names.jadd at line 33

	
	public void refined_Names_changeID(String id) { 
		// we also need to rename the file the compilation unit is in
		setID(id); 
	}

    // Declared in Names.jadd at line 38

	
	public void setID(String id) {
        setRelativeName(patch_name(relativeName(), id));
        setPathName(patch_name(pathName(), id));
	}

    // Declared in Names.jadd at line 43

	
    private static String patch_name(String path, String name) {
        char pathsep = File.separatorChar;
        int i = path.lastIndexOf(pathsep);
        String relname_head = i == -1 ? "" : path.substring(0, i+1);
        String relname_tail = i == -1 ? path : path.substring(i+1);
        int j = relname_tail.lastIndexOf(".");
        return relname_head + name + relname_tail.substring(j);
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
    public CompilationUnit(String p0, List p1, List p2) {
        setPackageDecl(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 18


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 21

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    private String tokenString_PackageDecl;

    // Declared in java.ast at line 3

    public void setPackageDecl(String value) {
        tokenString_PackageDecl = value;
    }

    // Declared in java.ast at line 6

    public String getPackageDecl() {
        return tokenString_PackageDecl != null ? tokenString_PackageDecl : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    public void setImportDeclList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumImportDecl = 0;

    // Declared in java.ast at line 7

    public int getNumImportDecl() {
        return getImportDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


    public ImportDecl getImportDecl(int i) {
        return (ImportDecl)getImportDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addImportDecl(ImportDecl node) {
        List list = getImportDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setImportDecl(ImportDecl node, int i) {
        List list = getImportDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getImportDeclList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getImportDeclListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 4
    public void setTypeDeclList(List list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumTypeDecl = 0;

    // Declared in java.ast at line 7

    public int getNumTypeDecl() {
        return getTypeDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


    public TypeDecl getTypeDecl(int i) {
        return (TypeDecl)getTypeDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addTypeDecl(TypeDecl node) {
        List list = getTypeDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setTypeDecl(TypeDecl node, int i) {
        List list = getTypeDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getTypeDeclList() {
        return (List)getChild(1);
    }

    // Declared in java.ast at line 28


    public List getTypeDeclListNoTransform() {
        return (List)getChildNoTransform(1);
    }

    // Declared in Undo.jadd at line 36

	  public void changeID(String id) {
		programRoot().pushUndo(new Rename(this, id));
		refined_Names_changeID(id);
	}

    // Declared in ClassPath.jrag at line 18
    public String relativeName() {
        String relativeName_value = relativeName_compute();
        return relativeName_value;
    }

    private String relativeName_compute() {  return  relativeName;  }

    // Declared in ClassPath.jrag at line 19
    public String pathName() {
        String pathName_value = pathName_compute();
        return pathName_value;
    }

    private String pathName_compute() {  return  pathName;  }

    // Declared in ClassPath.jrag at line 20
    public boolean fromSource() {
        boolean fromSource_value = fromSource_compute();
        return fromSource_value;
    }

    private boolean fromSource_compute() {  return  fromSource;  }

    protected java.util.Map localLookupType_String_values;
    // Declared in LookupType.jrag at line 389
    public SimpleSet localLookupType(String name) {
        Object _parameters = name;
if(localLookupType_String_values == null) localLookupType_String_values = new java.util.HashMap(4);
        if(localLookupType_String_values.containsKey(_parameters))
            return (SimpleSet)localLookupType_String_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        SimpleSet localLookupType_String_value = localLookupType_compute(name);
        if(isFinal && num == boundariesCrossed)
            localLookupType_String_values.put(_parameters, localLookupType_String_value);
        return localLookupType_String_value;
    }

    private SimpleSet localLookupType_compute(String name)  {
    for(int i = 0; i < getNumTypeDecl(); i++) {
      if(getTypeDecl(i).name().equals(name))
        return SimpleSet.emptySet.add(getTypeDecl(i));
    }
  
    SimpleSet set = SimpleSet.emptySet;
    // The scope of a type imported by a single-type-import declaration
    for(int i = 0; i < getNumImportDecl(); i++) {
      if(!getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          set = set.add(iter.next());
    }
    if(!set.isEmpty()) return set;
    
    TypeDecl result = lookupType(packageName(), name);
    if(result != null)
      return SimpleSet.emptySet.add(result);
    
    // The scope of a type imported by a type-import-on-demand declaration
    for(int i = 0; i < getNumImportDecl(); i++) {
      if(getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          set = set.add(iter.next());
    }
    if(!set.isEmpty()) return set;
    
    result = lookupType(PRIMITIVE_PACKAGE_NAME, name);
    if(result != null) {
      set = set.add(result);
      //throw new Error("Found an unexpected lookup for primitive type " + name);
      return set;
    }
    
    // 7.5.5 Automatic Imports
    result = lookupType("java.lang", name);
    if(result != null) {
      set = set.add(result);
      return set;
    }
    return set;
  }

    // Declared in PrettyPrint.jadd at line 936
    public String dumpString() {
        String dumpString_value = dumpString_compute();
        return dumpString_value;
    }

    private String dumpString_compute() {  return  getClass().getName() + " [" + getPackageDecl() + "]";  }

    protected boolean packageName_computed = false;
    protected String packageName_value;
    // Declared in QualifiedNames.jrag at line 83
    public String packageName() {
        if(packageName_computed)
            return packageName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        packageName_value = packageName_compute();
        if(isFinal && num == boundariesCrossed)
            packageName_computed = true;
        return packageName_value;
    }

    private String packageName_compute() {  return  getPackageDecl();  }

    // Declared in LookupType.jrag at line 89
    public TypeDecl lookupType(String packageName, String typeName) {
        TypeDecl lookupType_String_String_value = getParent().Define_TypeDecl_lookupType(this, null, packageName, typeName);
        return lookupType_String_String_value;
    }

    protected java.util.Map lookupType_String_values;
    // Declared in LookupType.jrag at line 173
    public SimpleSet lookupType(String name) {
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

    // Declared in AccessPackage.jrag at line 9
    public boolean hasPackage(String packageName) {
        boolean hasPackage_String_value = getParent().Define_boolean_hasPackage(this, null, packageName);
        return hasPackage_String_value;
    }

    protected java.util.Map accessType_TypeDecl_boolean_values;
    // Declared in AccessType.jrag at line 4
    public Access accessType(TypeDecl td, boolean ambiguous) {
        java.util.List _parameters = new java.util.ArrayList(2);
        _parameters.add(td);
        _parameters.add(Boolean.valueOf(ambiguous));
if(accessType_TypeDecl_boolean_values == null) accessType_TypeDecl_boolean_values = new java.util.HashMap(4);
        if(accessType_TypeDecl_boolean_values.containsKey(_parameters))
            return (Access)accessType_TypeDecl_boolean_values.get(_parameters);
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        Access accessType_TypeDecl_boolean_value = getParent().Define_Access_accessType(this, null, td, ambiguous);
        if(isFinal && num == boundariesCrossed)
            accessType_TypeDecl_boolean_values.put(_parameters, accessType_TypeDecl_boolean_value);
        return accessType_TypeDecl_boolean_value;
    }

    // Declared in AccessType.jrag at line 23
    public Access Define_Access_accessType(ASTNode caller, ASTNode child, TypeDecl td, boolean ambiguous) {
        if(true) { 
   int i = this.getIndexOfChild(caller);
 {
		if(td.isNestedType() && !td.isLocalClass()) {
			TypeDecl enc = td.enclosingType();
			Access encacc = getChild(i).accessType(enc, ambiguous);
			if(encacc == null) return null;
			Access acc = enc.getBodyDecl(0).accessType(td, ambiguous);
			if(acc == null) return null;
			return encacc.qualifiesAccess(acc);
		} else {
			SimpleSet set = localLookupType(td.getID());
			if(set.size() == 1 && (TypeDecl)set.iterator().next() == td) {
				return new TypeAccess(td.getID());
			}
			return accessType(td, ambiguous);
		}
	}
}
        return getParent().Define_Access_accessType(this, caller, td, ambiguous);
    }

    // Declared in TypeAnalysis.jrag at line 492
    public TypeDecl Define_TypeDecl_enclosingType(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  null;
        }
        return getParent().Define_TypeDecl_enclosingType(this, caller);
    }

    // Declared in ExceptionHandling.jrag at line 108
    public boolean Define_boolean_handlesException(ASTNode caller, ASTNode child, TypeDecl exceptionType) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return 
    !exceptionType.isUncheckedException();
        }
        return getParent().Define_boolean_handlesException(this, caller, exceptionType);
    }

    // Declared in TypeAnalysis.jrag at line 557
    public String Define_String_hostPackage(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  packageName();
        }
        return getParent().Define_String_hostPackage(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 573
    public TypeDecl Define_TypeDecl_hostType(ASTNode caller, ASTNode child) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  null;
        }
        return getParent().Define_TypeDecl_hostType(this, caller);
    }

    // Declared in LookupType.jrag at line 204
    public SimpleSet Define_SimpleSet_lookupImport(ASTNode caller, ASTNode child, String name) {
        if(caller == getImportDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    for(int i = 0; i < getNumImportDecl(); i++)
      if(!getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          return SimpleSet.emptySet.add(iter.next());
    return SimpleSet.emptySet;
  }
}
        return getParent().Define_SimpleSet_lookupImport(this, caller, name);
    }

    // Declared in QualifiedNames.jrag at line 81
    public String Define_String_packageName(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  packageName();
        }
        return getParent().Define_String_packageName(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 523
    public boolean Define_boolean_isMemberType(ASTNode caller, ASTNode child) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isMemberType(this, caller);
    }

    // Declared in DefiniteAssignment.jrag at line 40
    public boolean Define_boolean_isIncOrDec(ASTNode caller, ASTNode child) {
        if(caller == getTypeDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  false;
        }
        return getParent().Define_boolean_isIncOrDec(this, caller);
    }

    // Declared in TypeAnalysis.jrag at line 535
    public boolean Define_boolean_isLocalClass(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  false;
        }
        return getParent().Define_boolean_isLocalClass(this, caller);
    }

    // Declared in AccessPackage.jrag at line 15
    public Access Define_Access_accessPackage(ASTNode caller, ASTNode child, String pkg) {
        if(true) { 
   int i = this.getIndexOfChild(caller);
 {
		String[] path = pkg.split("\\.");
		if(lookupType(path[0]).isEmpty() && hasPackage(pkg))
			return new PackageAccess(pkg);
		return null;
	}
}
        return getParent().Define_Access_accessPackage(this, caller, pkg);
    }

    // Declared in TypeAnalysis.jrag at line 513
    public boolean Define_boolean_isNestedType(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  false;
        }
        return getParent().Define_boolean_isNestedType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 59
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getImportDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  NameType.PACKAGE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

    // Declared in LookupType.jrag at line 193
    public SimpleSet Define_SimpleSet_lookupType(ASTNode caller, ASTNode child, String name) {
        if(caller == getImportDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    /*
    for(int i = 0; i < getNumImportDecl(); i++)
      if(!getImportDecl(i).isOnDemand())
        for(Iterator iter = getImportDecl(i).importedTypes(name).iterator(); iter.hasNext(); )
          return SimpleSet.emptySet.add(iter.next());
    */
    return lookupType(name);
  }
}
        if(caller == getTypeDeclListNoTransform()) { 
   int childIndex = caller.getIndexOfChild(child);
 {
    SimpleSet set = SimpleSet.emptySet;
    for(Iterator iter = localLookupType(name).iterator(); iter.hasNext(); ) {
      TypeDecl t = (TypeDecl)iter.next();
      if(t.accessibleFromPackage(packageName()))
        set = set.add(t);
    }
    return set;
  }
}
        return getParent().Define_SimpleSet_lookupType(this, caller, name);
    }

    // Declared in ASTUtil.jrag at line 8
    public CompilationUnit Define_CompilationUnit_compilationUnit(ASTNode caller, ASTNode child) {
        if(true) {
      int childIndex = this.getIndexOfChild(caller);
            return  this;
        }
        return getParent().Define_CompilationUnit_compilationUnit(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
