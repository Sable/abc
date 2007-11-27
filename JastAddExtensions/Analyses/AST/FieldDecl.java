
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;import main.FileRange;


public class FieldDecl extends MemberDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        FieldDecl node = (FieldDecl)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          FieldDecl node = (FieldDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        FieldDecl res = (FieldDecl)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 164


  // Type members 

  public void toString(StringBuffer s) {
    s.append(indent());
    getModifiers().toString(s);
    getTypeAccess().toString(s);
    s.append(" ");
    getVariableDecl(0).toString(s);
    for(int i = 1; i < getNumVariableDecl(); i++) {
      s.append(", ");
      getVariableDecl(i).toString(s);
    }
    s.append(";/* error */\n");
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 75

    public FieldDecl() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(new List(), 2);

    }

    // Declared in java.ast at line 13


    // Declared in java.ast line 75
    public FieldDecl(Modifiers p0, Access p1, List p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
    }

    // Declared in java.ast at line 19


  protected int numChildren() {
    return 3;
  }

    // Declared in java.ast at line 22

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 75
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
    // Declared in java.ast line 75
    public void setTypeAccess(Access node) {
        setChild(node, 1);
    }

    // Declared in java.ast at line 5

    public Access getTypeAccess() {
        return (Access)getChild(1);
    }

    // Declared in java.ast at line 9


    public Access getTypeAccessNoTransform() {
        return (Access)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 75
    public void setVariableDeclList(List list) {
        setChild(list, 2);
    }

    // Declared in java.ast at line 6


    private int getNumVariableDecl = 0;

    // Declared in java.ast at line 7

    public int getNumVariableDecl() {
        return getVariableDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


    public VariableDecl getVariableDecl(int i) {
        return (VariableDecl)getVariableDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addVariableDecl(VariableDecl node) {
        List list = getVariableDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setVariableDecl(VariableDecl node, int i) {
        List list = getVariableDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getVariableDeclList() {
        return (List)getChild(2);
    }

    // Declared in java.ast at line 28


    public List getVariableDeclListNoTransform() {
        return (List)getChildNoTransform(2);
    }

    // Declared in Modifiers.jrag at line 228
    public boolean isStatic() {
        boolean isStatic_value = isStatic_compute();
        return isStatic_value;
    }

    private boolean isStatic_compute() {  return  getModifiers().isStatic();  }

    // Declared in TypeAnalysis.jrag at line 254
    public TypeDecl Define_TypeDecl_declType(ASTNode caller, ASTNode child) {
        if(caller == getVariableDeclListNoTransform()) {
      int childIndex = caller.getIndexOfChild(child);
            return  null;
        }
        return getParent().Define_TypeDecl_declType(this, caller);
    }

    // Declared in SyntacticClassification.jrag at line 67
    public NameType Define_NameType_nameType(ASTNode caller, ASTNode child) {
        if(caller == getTypeAccessNoTransform()) {
            return  NameType.TYPE_NAME;
        }
        return getParent().Define_NameType_nameType(this, caller);
    }

public ASTNode rewriteTo() {
    // Declared in VariableDeclaration.jrag at line 95
    if(getNumVariableDecl() == 1) {
        duringVariableDeclaration++;
        ASTNode result = rewriteRule0();
        duringVariableDeclaration--;
        return result;
    }

    // Declared in VariableDeclaration.jrag at line 106
    if(getParent().getParent() instanceof TypeDecl && 
        ((TypeDecl)getParent().getParent()).getBodyDeclListNoTransform() == getParent() && getNumVariableDecl() > 1) {
        duringVariableDeclaration++;
      List newList = rewriteTypeDecl_getBodyDecl();
      List list = (List)getParent();
      int i = list.getIndexOfChild(this);
      list.setChild(newList.getChildNoTransform(0), i);
      for(int j = 1; j < newList.getNumChild(); j++)
        list.insertChild(newList.getChildNoTransform(j), ++i);
        duringVariableDeclaration--;
      return newList.getChildNoTransform(0);
    }
    return super.rewriteTo();
}

    // Declared in VariableDeclaration.jrag at line 95
    private FieldDeclaration rewriteRule0() {
      FieldDeclaration decl = getVariableDecl(0).createFieldDeclarationFrom(getModifiers(), getTypeAccess());
      decl.setStart(start); // copy location information
      decl.setEnd(end); // copy location information
      return decl;
    }
    // Declared in VariableDeclaration.jrag at line 106
    private List rewriteTypeDecl_getBodyDecl() {
      List varList = new List();
      for(int j = 0; j < getNumVariableDecl(); j++) {
        varList.add(
          getVariableDecl(j).createFieldDeclarationFrom(
            (Modifiers)getModifiers().fullCopy(),
            (Access)getTypeAccess().fullCopy()
          )
        );
      }
      return varList;
    }
}
