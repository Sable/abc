package test;

public aspect Test28 {
  abstract ast Expr;
  ast Decl : Expr ::= <Type> <Name>;
  ast Use : Expr ::= <Name>;
  ast Program ::= Expr*;

  syn Decl Use.decl() = lookup(getName());
    
  inh Decl Use.lookup(String name);
  eq Program.getExpr(int index).lookup(String name) {
    for(int i = index; i >=0; i--) {
      if(getExpr(i).isDecl(name))
        return (Decl)getExpr(i);
    }
    return null;
  }

  syn boolean Expr.isDecl(String name);
  eq Expr.isDecl(String name) = false;
  eq Decl.isDecl(String name) {
    return name.equals(getName());
  }
  
  public static void main(String[] args) {
    System.out.println("Larger example using the following features:");
    System.out.println("  syn paramterized attributes with/without initializer + overriding + declarative/imparative equations");
    System.out.println("  inh paramterized attributes using list child index");
    Decl d1 = new Decl("int", "d");
    Use u1 = new Use("d");
    Decl d2 = new Decl("string", "d");
    Use u2 = new Use("d");
    List list = new List();
    list.add(d1);
    list.add(u1);
    list.add(d2);
    list.add(u2);
    Program p = new Program(list);
    
    System.out.println(u1.decl().getType());
    System.out.println(u2.decl().getType());
  }
}
