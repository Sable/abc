
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import java.util.HashMap;import java.util.Iterator;


public class ArrayDecl extends ClassDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
        accessibleFrom_TypeDecl_values = null;
        dimension_computed = false;
        elementType_computed = false;
        elementType_value = null;
        fullName_computed = false;
        fullName_value = null;
        typeName_computed = false;
        typeName_value = null;
        castingConversionTo_TypeDecl_values = null;
        instanceOf_TypeDecl_values = null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public ArrayDecl clone() throws CloneNotSupportedException {
        ArrayDecl node = (ArrayDecl)super.clone();
        node.accessibleFrom_TypeDecl_values = null;
        node.dimension_computed = false;
        node.elementType_computed = false;
        node.elementType_value = null;
        node.fullName_computed = false;
        node.fullName_value = null;
        node.typeName_computed = false;
        node.typeName_value = null;
        node.castingConversionTo_TypeDecl_values = null;
        node.instanceOf_TypeDecl_values = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
     @SuppressWarnings({"unchecked", "cast"})  public ArrayDecl copy() {
      try {
          ArrayDecl node = (ArrayDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
     @SuppressWarnings({"unchecked", "cast"})  public ArrayDecl fullCopy() {
        ArrayDecl res = (ArrayDecl)copy();
        for(int i = 0; i < getNumChildNoTransform(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in Arrays.jrag at line 59


  public Access createQualifiedAccess() {
    return new ArrayTypeAccess(componentType().createQualifiedAccess());
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 65

    public ArrayDecl() {
        super();

        setChild(new Opt(), 1);
        setChild(new List(), 2);
        setChild(new List(), 3);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 65
    public ArrayDecl(Modifiers p0, String p1, Opt<Access> p2, List<Access> p3, List<BodyDecl> p4) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
        setChild(p3, 2);
        setChild(p4, 3);
    }

    // Declared in java.ast at line 22


    // Declared in java.ast line 65
    public ArrayDecl(Modifiers p0, beaver.Symbol p1, Opt<Access> p2, List<Access> p3, List<BodyDecl> p4) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
        setChild(p3, 2);
        setChild(p4, 3);
    }

    // Declared in java.ast at line 30


  protected int numChildren() {
    return 4;
  }

    // Declared in java.ast at line 33

  public boolean mayHaveRewrite() { return false; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 63
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
    // Declared in java.ast line 63
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
    // Declared in java.ast line 63
    public void setSuperClassAccessOpt(Opt<Access> opt) {
        setChild(opt, 1);
    }

    // Declared in java.ast at line 6


    public boolean hasSuperClassAccess() {
        return getSuperClassAccessOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


     @SuppressWarnings({"unchecked", "cast"})  public Access getSuperClassAccess() {
        return (Access)getSuperClassAccessOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setSuperClassAccess(Access node) {
        getSuperClassAccessOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

     @SuppressWarnings({"unchecked", "cast"})  public Opt<Access> getSuperClassAccessOpt() {
        return (Opt<Access>)getChild(1);
    }

    // Declared in java.ast at line 21


     @SuppressWarnings({"unchecked", "cast"})  public Opt<Access> getSuperClassAccessOptNoTransform() {
        return (Opt<Access>)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 63
    public void setImplementsList(List<Access> list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumImplements = 0;

    // Declared in java.ast at line 7

    public int getNumImplements() {
        return getImplementsList().getNumChild();
    }

    // Declared in java.ast at line 11


     @SuppressWarnings({"unchecked", "cast"})  public Access getImplements(int i) {
        return (Access)getImplementsList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addImplements(Access node) {
        List<Access> list = getImplementsList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setImplements(Access node, int i) {
        List<Access> list = getImplementsList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List<Access> getImplementss() {
        return getImplementsList();
    }

    // Declared in java.ast at line 27

    public List<Access> getImplementssNoTransform() {
        return getImplementsListNoTransform();
    }

    // Declared in java.ast at line 31


     @SuppressWarnings({"unchecked", "cast"})  public List<Access> getImplementsList() {
        return (List<Access>)getChild(2);
    }

    // Declared in java.ast at line 35


     @SuppressWarnings({"unchecked", "cast"})  public List<Access> getImplementsListNoTransform() {
        return (List<Access>)getChildNoTransform(2);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 63
    public void setBodyDeclList(List<BodyDecl> list) {
        setChild(list, 3);
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
        return (List<BodyDecl>)getChild(3);
    }

    // Declared in java.ast at line 35


     @SuppressWarnings({"unchecked", "cast"})  public List<BodyDecl> getBodyDeclListNoTransform() {
        return (List<BodyDecl>)getChildNoTransform(3);
    }

    // Declared in AccessControl.jrag at line 13
 @SuppressWarnings({"unchecked", "cast"})     public boolean accessibleFrom(TypeDecl type) {
        Object _parameters = type;
if(accessibleFrom_TypeDecl_values == null) accessibleFrom_TypeDecl_values = new java.util.HashMap(4);
        if(accessibleFrom_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)accessibleFrom_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean accessibleFrom_TypeDecl_value = accessibleFrom_compute(type);
        if(isFinal && num == boundariesCrossed)
            accessibleFrom_TypeDecl_values.put(_parameters, Boolean.valueOf(accessibleFrom_TypeDecl_value));
        return accessibleFrom_TypeDecl_value;
    }

    private boolean accessibleFrom_compute(TypeDecl type) {  return elementType().accessibleFrom(type);  }

    // Declared in Arrays.jrag at line 12
 @SuppressWarnings({"unchecked", "cast"})     public int dimension() {
        if(dimension_computed)
            return dimension_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        dimension_value = dimension_compute();
        if(isFinal && num == boundariesCrossed)
            dimension_computed = true;
        return dimension_value;
    }

    private int dimension_compute() {  return componentType().dimension() + 1;  }

    // Declared in Arrays.jrag at line 16
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl elementType() {
        if(elementType_computed)
            return elementType_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        elementType_value = elementType_compute();
        if(isFinal && num == boundariesCrossed)
            elementType_computed = true;
        return elementType_value;
    }

    private TypeDecl elementType_compute() {  return componentType().elementType();  }

    // Declared in Arrays.jrag at line 53
 @SuppressWarnings({"unchecked", "cast"})     public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return fullName();  }

    // Declared in Arrays.jrag at line 54
 @SuppressWarnings({"unchecked", "cast"})     public String fullName() {
        if(fullName_computed)
            return fullName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        fullName_value = fullName_compute();
        if(isFinal && num == boundariesCrossed)
            fullName_computed = true;
        return fullName_value;
    }

    private String fullName_compute() {  return getID();  }

    // Declared in QualifiedNames.jrag at line 87
 @SuppressWarnings({"unchecked", "cast"})     public String typeName() {
        if(typeName_computed)
            return typeName_value;
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        typeName_value = typeName_compute();
        if(isFinal && num == boundariesCrossed)
            typeName_computed = true;
        return typeName_value;
    }

    private String typeName_compute() {  return componentType().typeName() + "[]";  }

    // Declared in TypeAnalysis.jrag at line 120
 @SuppressWarnings({"unchecked", "cast"})     public boolean castingConversionTo(TypeDecl type) {
        Object _parameters = type;
if(castingConversionTo_TypeDecl_values == null) castingConversionTo_TypeDecl_values = new java.util.HashMap(4);
        if(castingConversionTo_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)castingConversionTo_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean castingConversionTo_TypeDecl_value = castingConversionTo_compute(type);
        if(isFinal && num == boundariesCrossed)
            castingConversionTo_TypeDecl_values.put(_parameters, Boolean.valueOf(castingConversionTo_TypeDecl_value));
        return castingConversionTo_TypeDecl_value;
    }

    private boolean castingConversionTo_compute(TypeDecl type) {
    if(type.isArrayDecl()) {
      TypeDecl SC = componentType();
      TypeDecl TC = type.componentType();
      if(SC.isPrimitiveType() && TC.isPrimitiveType() && SC == TC)
        return true;
      if(SC.isReferenceType() && TC.isReferenceType()) {
        return SC.castingConversionTo(TC);
      }
      return false;
    }
    else if(type.isClassDecl()) {
      return type.isObject();
    }
    else if(type.isInterfaceDecl()) {
      return type == typeSerializable() || type == typeCloneable();
    }
    else return super.castingConversionTo(type);
  }

    // Declared in TypeAnalysis.jrag at line 214
 @SuppressWarnings({"unchecked", "cast"})     public boolean isArrayDecl() {
        boolean isArrayDecl_value = isArrayDecl_compute();
        return isArrayDecl_value;
    }

    private boolean isArrayDecl_compute() {  return true;  }

    // Declared in TypeAnalysis.jrag at line 411
 @SuppressWarnings({"unchecked", "cast"})     public boolean instanceOf(TypeDecl type) {
        Object _parameters = type;
if(instanceOf_TypeDecl_values == null) instanceOf_TypeDecl_values = new java.util.HashMap(4);
        if(instanceOf_TypeDecl_values.containsKey(_parameters))
            return ((Boolean)instanceOf_TypeDecl_values.get(_parameters)).booleanValue();
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        boolean instanceOf_TypeDecl_value = instanceOf_compute(type);
        if(isFinal && num == boundariesCrossed)
            instanceOf_TypeDecl_values.put(_parameters, Boolean.valueOf(instanceOf_TypeDecl_value));
        return instanceOf_TypeDecl_value;
    }

    private boolean instanceOf_compute(TypeDecl type) {  return type.isSupertypeOfArrayDecl(this);  }

    // Declared in TypeAnalysis.jrag at line 469
 @SuppressWarnings({"unchecked", "cast"})     public boolean isSupertypeOfArrayDecl(ArrayDecl type) {
        boolean isSupertypeOfArrayDecl_ArrayDecl_value = isSupertypeOfArrayDecl_compute(type);
        return isSupertypeOfArrayDecl_ArrayDecl_value;
    }

    private boolean isSupertypeOfArrayDecl_compute(ArrayDecl type) {
    if(type.elementType().isPrimitive() && elementType().isPrimitive())
      return type.dimension() == dimension() && type.elementType() == elementType();
    return type.componentType().instanceOf(componentType());
  }

    // Declared in TypeAnalysis.jrag at line 140
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl typeSerializable() {
        TypeDecl typeSerializable_value = getParent().Define_TypeDecl_typeSerializable(this, null);
        return typeSerializable_value;
    }

    // Declared in TypeAnalysis.jrag at line 141
 @SuppressWarnings({"unchecked", "cast"})     public TypeDecl typeCloneable() {
        TypeDecl typeCloneable_value = getParent().Define_TypeDecl_typeCloneable(this, null);
        return typeCloneable_value;
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
