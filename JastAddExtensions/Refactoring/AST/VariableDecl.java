
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;import changes.*;import main.FileRange;
 // Simplified VarDeclStmt

public class VariableDecl extends ASTNode implements Cloneable {
    public void flushCache() {
        super.flushCache();
    }
    public Object clone() throws CloneNotSupportedException {
        VariableDecl node = (VariableDecl)super.clone();
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          VariableDecl node = (VariableDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        VariableDecl res = (VariableDecl)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in PrettyPrint.jadd at line 234


  //
  
  public void toString(StringBuffer s) {
    s.append(name());
    for(int i = 0; i < getNumEmptyBracket(); i++) {
      s.append("[]");
    }
    if(hasInit()) {
      s.append(" = ");
      getInit().toString(s);
    }
  }

    // Declared in VariableDeclaration.jrag at line 155


  public VariableDeclaration createVariableDeclarationFrom(Modifiers modifiers, Access type) {
    VariableDeclaration decl = new VariableDeclaration(
      modifiers,
      getNumEmptyBracket() == 0 ? type : 
        new ArrayTypeAccess(type, getNumEmptyBracket()),
      getID(),
      getInitOpt()
    );
    decl.setStart(start); // copy location information
    decl.setEnd(end); // copy location information
    return decl;
  }

    // Declared in VariableDeclaration.jrag at line 168


  public FieldDeclaration createFieldDeclarationFrom(Modifiers modifiers, Access type) {
    FieldDeclaration decl = new FieldDeclaration(
      modifiers,
      getNumEmptyBracket() == 0 ? type : 
        new ArrayTypeAccess(type, getNumEmptyBracket()),
      getID(),
      getInitOpt()
    );
    decl.setStart(start); // copy location information
    decl.setEnd(end); // copy location information
    return decl;
  }

    // Declared in java.ast at line 3
    // Declared in java.ast line 81

    public VariableDecl() {
        super();

        setChild(new List(), 0);
        setChild(new Opt(), 1);

    }

    // Declared in java.ast at line 12


    // Declared in java.ast line 81
    public VariableDecl(String p0, List p1, Opt p2) {
        setID(p0);
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
    // Declared in java.ast line 81
    private String tokenString_ID;

    // Declared in java.ast at line 3

    public void setID(String value) {
        tokenString_ID = value;
    }

    // Declared in java.ast at line 6

    public String getID() {
        return tokenString_ID != null ? tokenString_ID : "";
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 81
    public void setEmptyBracketList(List list) {
        setChild(list, 0);
    }

    // Declared in java.ast at line 6


    private int getNumEmptyBracket = 0;

    // Declared in java.ast at line 7

    public int getNumEmptyBracket() {
        return getEmptyBracketList().getNumChild();
    }

    // Declared in java.ast at line 11


    public EmptyBracket getEmptyBracket(int i) {
        return (EmptyBracket)getEmptyBracketList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addEmptyBracket(EmptyBracket node) {
        List list = getEmptyBracketList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setEmptyBracket(EmptyBracket node, int i) {
        List list = getEmptyBracketList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getEmptyBracketList() {
        return (List)getChild(0);
    }

    // Declared in java.ast at line 28


    public List getEmptyBracketListNoTransform() {
        return (List)getChildNoTransform(0);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 81
    public void setInitOpt(Opt opt) {
        setChild(opt, 1);
    }

    // Declared in java.ast at line 6


    public boolean hasInit() {
        return getInitOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Expr getInit() {
        return (Expr)getInitOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setInit(Expr node) {
        getInitOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getInitOpt() {
        return (Opt)getChild(1);
    }

    // Declared in java.ast at line 21


    public Opt getInitOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }

    // Declared in VariableDeclaration.jrag at line 88
    public String name() {
        String name_value = name_compute();
        return name_value;
    }

    private String name_compute() {  return  getID();  }

    // Declared in DefiniteAssignment.jrag at line 30
    public boolean Define_boolean_isSource(ASTNode caller, ASTNode child) {
        if(caller == getInitOptNoTransform()) {
            return  true;
        }
        return getParent().Define_boolean_isSource(this, caller);
    }

public ASTNode rewriteTo() {
    return super.rewriteTo();
}

}
