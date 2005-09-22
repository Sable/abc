package test;

public aspect Test44 {
  ast A ::= B;
  ast B;

  syn boolean A.a() = a();
  inh boolean B.b();
  eq A.getB().b() = getB().b();

  public static void main(String[] args) {
    System.out.println("Attributes: check that revisited attributes throws an exception");
    B b = new B();
    A a = new A(b);
    try {
      a.a();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    try {
      b.b();
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
