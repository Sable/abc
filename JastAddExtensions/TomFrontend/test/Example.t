package test;

public class Example {
  public String toString() {
    return "Example";
  }
  static class JavaType { 
    JavaType(Example e) {
      child = e;
    }
    Example child;
    Example getChild() { return child; };
    public String toString() {
      return "JavaType(" + getChild() + ")";
    }
  }


  public static void main(String[] args) {
    System.out.println("Hello");
    Example t = new Example();
    JavaType b = `A(t);
    Example c = `C();
    %match(TomType b) {
      c -> {
        System.out.println(c); 
        System.out.println(`c); 
      }
    }
  }

  %typeterm TomType { 
    implement { JavaType } 
    is_sort(t) { t instanceof JavaType }
  }
  %typeterm TomType2 { 
    implement { Example } 
    is_sort(t) { t instanceof Example }
  }
<F21>
  %op TomType A(name : TomType2) {
    make(x) { new JavaType(x) }
    is_fsym(t) { t instanceof JavaType }
    get_slot(name, t) { t.getChild() } 
  }
  %op TomType2 C() {
    make() { new Example() }
    is_fsym(t) { t instanceof Example }
  }

  // ClassDecl ::= Modifiers <Name:String> BodyDeclList
  // BodyDeclList ::= BodyDecl*;

  typeterm TomASTNode {
    implement { ASTNode }
    is_sort(t) { t instanceof ASTNode }
  }
  typeterm TomList {
    implement { List }
    is_sort(t) { t instanceof List }
  }
  typeterm TomOpt {
    implement { Opt }
    is_sort(t) { t instanceof Opt }
  }

  %op TomASTNode newClassDecl(m : ASTNode, name : String, superOpt : TomOpt, bodyDeclList : TomList) {
    make(m, name, superOpt, bodyDeclList) { new ClassDecl(m, name, superOpt, bodyDeclList) }
    is_fsym(t) { t instanceof ClassDecl }
    get_slot(m, t) { t.getModifiers() }
    get_slot(name, t) { t.getName() }
    get_slot(superOpt, t) { t.getSuperAccessOpt() }
    get_slot(bodyDeclList, t) { t.getBodyDeclList() }
  }

  %oplist TomList newList(ASTNode*) {
  }

  syn boolean ClassDecl.hasConstructor() {

    %match(this) {

      newClassDecl[bodyDeclList=newList(_*,C@newConstructorDecl[],_*)] -> { return true; }
  }

  syn Access ClassDecl.getSuperAccessInPublic() {
    %match(this) {
      newClassDecl(newModifiers(newList(_*,newModifier("public"),_*)),_,newOpt(v),_) { return v; }
    }
    return null;
  }


