
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import changes.*;


public class AnonymousDecl extends ClassDecl implements Cloneable {
    public void flushCache() {
        super.flushCache();
        isCircular_computed = false;
        getSuperClassAccessOpt_computed = false;
        getSuperClassAccessOpt_value = null;
        getImplementsList_computed = false;
        getImplementsList_value = null;
    }
    public Object clone() throws CloneNotSupportedException {
        AnonymousDecl node = (AnonymousDecl)super.clone();
        node.isCircular_computed = false;
        node.getSuperClassAccessOpt_computed = false;
        node.getSuperClassAccessOpt_value = null;
        node.getImplementsList_computed = false;
        node.getImplementsList_value = null;
        node.in$Circle(false);
        node.is$Final(false);
    return node;
    }
    public ASTNode copy() {
      try {
          AnonymousDecl node = (AnonymousDecl)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AnonymousDecl res = (AnonymousDecl)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    // Declared in java.ast at line 3
    // Declared in java.ast line 66

    public AnonymousDecl() {
        super();
        setChild(new Opt(), 0);
        setChild(null, 1);

        setChild(null, 0);
        setChild(new List(), 1);
        setChild(new Opt(), 2);
        setChild(new List(), 3);

    }

    // Declared in java.ast at line 16


    // Declared in java.ast line 66
    public AnonymousDecl(Modifiers p0, String p1, List p2) {
        setChild(p0, 0);
        setID(p1);
        setChild(p2, 1);
        setChild(new Opt(), 2);
        setChild(new List(), 3);
    }

    // Declared in java.ast at line 24


  protected int numChildren() {
    return 2;
  }

    // Declared in java.ast at line 27

  public boolean mayHaveRewrite() { return true; }

    // Declared in java.ast at line 2
    // Declared in java.ast line 66
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
    // Declared in java.ast line 66
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
    // Declared in java.ast line 66
    public void setBodyDeclList(List list) {
        setChild(list, 1);
    }

    // Declared in java.ast at line 6


    private int getNumBodyDecl = 0;

    // Declared in java.ast at line 7

    public int getNumBodyDecl() {
        return getBodyDeclList().getNumChild();
    }

    // Declared in java.ast at line 11


    public BodyDecl getBodyDecl(int i) {
        return (BodyDecl)getBodyDeclList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addBodyDecl(BodyDecl node) {
        List list = getBodyDeclList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setBodyDecl(BodyDecl node, int i) {
        List list = getBodyDeclList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getBodyDeclList() {
        return (List)getChild(1);
    }

    // Declared in java.ast at line 28


    public List getBodyDeclListNoTransform() {
        return (List)getChildNoTransform(1);
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 66
    public void setSuperClassAccessOpt(Opt opt) {
        setChild(opt, 2);
    }

    // Declared in java.ast at line 6


    public boolean hasSuperClassAccess() {
        return getSuperClassAccessOpt().getNumChild() != 0;
    }

    // Declared in java.ast at line 10


    public Access getSuperClassAccess() {
        return (Access)getSuperClassAccessOpt().getChild(0);
    }

    // Declared in java.ast at line 14


    public void setSuperClassAccess(Access node) {
        getSuperClassAccessOpt().setChild(node, 0);
    }

    // Declared in java.ast at line 17

    public Opt getSuperClassAccessOptNoTransform() {
        return (Opt)getChildNoTransform(2);
    }

    // Declared in java.ast at line 21


    protected int getSuperClassAccessOptChildPosition() {
        return 2;
    }

    // Declared in java.ast at line 2
    // Declared in java.ast line 66
    public void setImplementsList(List list) {
        setChild(list, 3);
    }

    // Declared in java.ast at line 6


    private int getNumImplements = 0;

    // Declared in java.ast at line 7

    public int getNumImplements() {
        return getImplementsList().getNumChild();
    }

    // Declared in java.ast at line 11


    public Access getImplements(int i) {
        return (Access)getImplementsList().getChild(i);
    }

    // Declared in java.ast at line 15


    public void addImplements(Access node) {
        List list = getImplementsList();
        list.addChild(node);
    }

    // Declared in java.ast at line 20


    public void setImplements(Access node, int i) {
        List list = getImplementsList();
        list.setChild(node, i);
    }

    // Declared in java.ast at line 24

    public List getImplementsListNoTransform() {
        return (List)getChildNoTransform(3);
    }

    // Declared in java.ast at line 28


    protected int getImplementsListChildPosition() {
        return 3;
    }

    protected boolean isCircular_visited = false;
    protected boolean isCircular_computed = false;
    protected boolean isCircular_initialized = false;
    protected boolean isCircular_value;
    public boolean isCircular() {
        if(isCircular_computed)
            return isCircular_value;
        if (!isCircular_initialized) {
            isCircular_initialized = true;
            isCircular_value = true;
        }
        if (!IN_CIRCLE) {
            IN_CIRCLE = true;
            isCircular_visited = true;
            int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
            do {
                CHANGE = false;
                boolean new_isCircular_value = isCircular_compute();
                if (new_isCircular_value!=isCircular_value)
                    CHANGE = true;
                isCircular_value = new_isCircular_value; 
            } while (CHANGE);
            isCircular_visited = false;
            if(isFinal && num == boundariesCrossed)
{
            isCircular_computed = true;
            }
            else {
            RESET_CYCLE = true;
            isCircular_compute();
            RESET_CYCLE = false;
              isCircular_computed = false;
              isCircular_initialized = false;
            }
            IN_CIRCLE = false; 
            return isCircular_value;
        }
        if(!isCircular_visited) {
            if (RESET_CYCLE) {
                isCircular_computed = false;
                isCircular_initialized = false;
                return isCircular_value;
            }
            isCircular_visited = true;
            boolean new_isCircular_value = isCircular_compute();
            if (new_isCircular_value!=isCircular_value)
                CHANGE = true;
            isCircular_value = new_isCircular_value; 
            isCircular_visited = false;
            return isCircular_value;
        }
        return isCircular_value;
    }

    private boolean isCircular_compute() {  return  false;  }

    protected boolean getSuperClassAccessOpt_computed = false;
    protected Opt getSuperClassAccessOpt_value;
    // Declared in AnonymousClasses.jrag at line 23
    public Opt getSuperClassAccessOpt() {
        if(getSuperClassAccessOpt_computed)
            return (Opt)ASTNode.getChild(this, getSuperClassAccessOptChildPosition());
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getSuperClassAccessOpt_value = getSuperClassAccessOpt_compute();
        setSuperClassAccessOpt(getSuperClassAccessOpt_value);
        if(isFinal && num == boundariesCrossed)
            getSuperClassAccessOpt_computed = true;
        return (Opt)ASTNode.getChild(this, getSuperClassAccessOptChildPosition());
    }

    private Opt getSuperClassAccessOpt_compute()  {
    if(superType().isInterfaceDecl())
      return new Opt(typeObject().createQualifiedAccess());
    else
      return new Opt(superType().createBoundAccess());
  }

    protected boolean getImplementsList_computed = false;
    protected List getImplementsList_value;
    // Declared in AnonymousClasses.jrag at line 29
    public List getImplementsList() {
        if(getImplementsList_computed)
            return (List)ASTNode.getChild(this, getImplementsListChildPosition());
        int num = boundariesCrossed;
        boolean isFinal = this.is$Final();
        getImplementsList_value = getImplementsList_compute();
        setImplementsList(getImplementsList_value);
        if(isFinal && num == boundariesCrossed)
            getImplementsList_computed = true;
        return (List)ASTNode.getChild(this, getImplementsListChildPosition());
    }

    private List getImplementsList_compute()  {
    if(superType().isInterfaceDecl())
      return new List().add(superType().createBoundAccess());
    else
      return new List();
  }

    // Declared in AnonymousClasses.jrag at line 5
    public TypeDecl superType() {
        TypeDecl superType_value = getParent().Define_TypeDecl_superType(this, null);
        return superType_value;
    }

    // Declared in AnonymousClasses.jrag at line 9
    public ConstructorDecl constructorDecl() {
        ConstructorDecl constructorDecl_value = getParent().Define_ConstructorDecl_constructorDecl(this, null);
        return constructorDecl_value;
    }

    // Declared in AnonymousClasses.jrag at line 100
    public TypeDecl typeNullPointerException() {
        TypeDecl typeNullPointerException_value = getParent().Define_TypeDecl_typeNullPointerException(this, null);
        return typeNullPointerException_value;
    }

public ASTNode rewriteTo() {
    // Declared in AnonymousClasses.jrag at line 43
    if(noConstructor()) {
        duringAnonymousClasses++;
        ASTNode result = rewriteRule0();
        duringAnonymousClasses--;
        return result;
    }

    return super.rewriteTo();
}

    // Declared in AnonymousClasses.jrag at line 43
    private AnonymousDecl rewriteRule0() {
      setModifiers(new Modifiers(new List().add(new Modifier("final"))));
      
      ConstructorDecl constructor = new ConstructorDecl();
      addBodyDecl(constructor);

      constructor.setModifiers((Modifiers)constructorDecl().getModifiers().fullCopy());
      String name = "Anonymous" + nextAnonymousIndex();
      setID(name);
      constructor.setID(name);

      List parameterList = new List();
      for(int i = 0; i < constructorDecl().getNumParameter(); i++) {
        parameterList.add(
          new ParameterDeclaration(
            constructorDecl().getParameter(i).type().createQualifiedAccess(),
            constructorDecl().getParameter(i).name()
          )
        );
      }
      constructor.setParameterList(parameterList);
      
      List argList = new List();
      for(int i = 0; i < constructor.getNumParameter(); i++)
        argList.add(new VarAccess(constructor.getParameter(i).name()));
      constructor.setConstructorInvocation(
        new ExprStmt(
          new SuperConstructorAccess("super", argList)
        )
      );
      constructor.setBlock(new Block());

      HashSet set = new HashSet();
      for(int i = 0; i < getNumBodyDecl(); i++) {
        if(getBodyDecl(i) instanceof InstanceInitializer) {
          InstanceInitializer init = (InstanceInitializer)getBodyDecl(i);
          set.addAll(init.exceptions());
        }
        else if(getBodyDecl(i) instanceof FieldDeclaration) {
          FieldDeclaration f = (FieldDeclaration)getBodyDecl(i);
          if(f.isInstanceVariable()) {
            set.addAll(f.exceptions());
          }
        }
      }
      List exceptionList = new List();
      for(Iterator iter = set.iterator(); iter.hasNext(); ) {
        TypeDecl exceptionType = (TypeDecl)iter.next();
        if(exceptionType.isNull())
          exceptionType = typeNullPointerException();
        exceptionList.add(exceptionType.createQualifiedAccess());
      }
      constructor.setExceptionList(exceptionList);
      return this;
    }
}
