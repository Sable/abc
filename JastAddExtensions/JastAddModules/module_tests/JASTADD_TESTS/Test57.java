package test;

public aspect Test57 {
  ast A;

  syn boolean A.a() circular [false] {
    System.out.println("Eval a()");
    return b();
  }
  syn boolean A.b() circular [false] {
    System.out.println("Eval b()");
    return c();
  }
  syn boolean A.c() circular [false] {
    System.out.println("Eval c()");
    return a() || true;
  }

  public static void main(String[] args) {
    System.out.println("Circular: avoid recomputation of potentially circular attributes");
    A node = new A();
    System.out.println("a() == true: " + (node.a() == true));
    System.out.println("b() == true: " + (node.b() == true));
    System.out.println("c() == true: " + (node.c() == true));
  }
}
