
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


// 4.1 The Kinds of Types and Values

public abstract class ReferenceType extends TypeDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
        narrowingConversionTo_TypeDecl_values = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public ReferenceType clone() throws CloneNotSupportedException {
        ReferenceType node = (ReferenceType)super.clone();
        node.narrowingConversionTo_TypeDecl_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 41

    public ReferenceType() {
        super();

        setChild(new List(), 1);

    }

    // Declared in java.ast at line 11


    // Declared in java.ast line 41
    public ReferenceType(Modifiers p0, String p1, List<BodyDecl> p2) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 18


    // Declared in java.ast line 41
    public ReferenceType(Modifiers p0, beaver.Symbol p1, List<BodyDecl> p2) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
    }

    // Declared in java.ast at line 24


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 27

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 38
    public void setModifiers(Modifiers node) {
        setChild(node, 0);
    }

    // Declared in java.ast at line 5

    public Modifiers getModifiers() {
        return (Modifiers)getChild(0);
    }

    // Declared in java.ast at line 9


    public Modifiers getModifiersNoTransform() {
        return (Modifiers)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 38
    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 5

    public int IDstart;

    // Declared in java.ast at line 6

    public int IDend;

    // Declared in java.ast at line 7

    public void setID(beaver.Symbol symbol) {
        if(symbol.value != null && !(symbol.value instanceof String))
          throw new UnsupportedOperationException("setID is only valid for String lexemes");
        tokenString_ID = (String)symbol.value;
        IDstart = symbol.getStart();
        IDend = symbol.getEnd();
    }

    // Declared in java.ast at line 14

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 38
    public void setBodyDeclList(List<BodyDecl> list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumBodyDecl = 0;

    // Declared in java.ast at line 7

    public int getNumBodyDecl() {
        return getBodyDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


     @SuppressWarnings({"unchecked", "cast"})  public BodyDecl getBodyDecl(int i) {
        return (BodyDecl)getBodyDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addBodyDecl(BodyDecl node) {
        List<BodyDecl> list = getBodyDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setBodyDecl(BodyDecl node, int i) {
        List<BodyDecl> list = getBodyDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List<BodyDecl> getBodyDecls() {
        return getBodyDeclList();
    }

    // Declared in java.ast at line 27

    public List<BodyDecl> getBodyDeclsNoTransform() {
        return getBodyDeclListNoTransform();
    }

    // Declared in java.ast at line 31


     @SuppressWarnings({"unchecked", "cast"})  public List<BodyDecl> getBodyDeclList() {
        return (List<BodyDecl>)getChild(1);
    }

    // Declared in java.ast at line 35


     @SuppressWarnings({"unchecked", "cast"})  public List<BodyDecl> getBodyDeclListNoTransform() {
        return (List<BodyDecl>)getChildNoTransform(1);
    }

    // Declared in TypeAnalysis.jrag at line 33
 @SuppressWarnings({"unchecked", "cast"})     public boolean wideningConversionTo(TypeDecl type) {
        boolean wideningConversionTo_TypeDecl_value = wideningConversionTo_compute(type);
        return wideningConversionTo_TypeDecl_value;
    }

    private boolean wideningConversionTo_compute(TypeDecl type) {  return instanceOf(type);  }

    // Declared in TypeAnalysis.jrag at line 36
 @SuppressWarnings({"unchecked", "cast"})     public boolean narrowingConversionTo(TypeDecl type) {
        Object _parameters = type;
if(narrowingConversionTo_TypeDecl_values == null) narrowingConversionTo_TypeDecl_values = new java.util.HashMap(4);
        if(narrowingConversionTo_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)narrowingConversionTo_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean narrowingConversionTo_TypeDecl_value = narrowingConversionTo_compute(type);
        if(isFinal && num == boundariesCrossed)
            narrowingConversionTo_TypeDecl_values.put(_parameters, Boolean.valueOf(narrowingConversionTo_TypeDecl_value));
        return narrowingConversionTo_TypeDecl_value;
    }

    private boolean narrowingConversionTo_compute(TypeDecl type) {
    if(type.instanceOf(this))
      return true;
    if(isClassDecl() && !getModifiers().isFinal() && type.isInterfaceDecl())
      return true;
    if(isInterfaceDecl() && type.isClassDecl() && !type.getModifiers().isFinal())
      return true;
    if(isInterfaceDecl() && type.instanceOf(this))
      return true;
    if(fullName().equals("java.lang.Object") && type.isInterfaceDecl())
      return true;
    // Dragons
    // TODO: Check if both are interfaces with compatible methods
    if(isArrayDecl() && type.isArrayDecl() && elementType().instanceOf(type.elementType()))
      return true;
    return false;
  }

    // Declared in TypeAnalysis.jrag at line 166
 @SuppressWarnings({"unchecked", "cast"})     public boolean isReferenceType() {
        boolean isReferenceType_value = isReferenceType_compute();
        return isReferenceType_value;
    }

    private boolean isReferenceType_compute() {  return true;  }

    // Declared in TypeAnalysis.jrag at line 483
 @SuppressWarnings({"unchecked", "cast"})     public boolean isSupertypeOfNullType(NullType type) {
        boolean isSupertypeOfNullType_NullType_value = isSupertypeOfNullType_compute(type);
        return isSupertypeOfNullType_NullType_value;
    }

    private boolean isSupertypeOfNullType_compute(NullType type) {  return true;  }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
