package test;

public aspect Test24 {
  ast A ::= B;
  ast B ::= Left:C Right:C;
  ast C;

  inh int C.value();
  eq A.getB().value() = 1;
  eq B.getRight().value() = 2;
  
  public static void main(String[] args) {
    System.out.println("Inh: inherited attribute with equations being overridden in one path only");
    C c1 = new C();
    C c2 = new C();
    B b = new B(c1, c2);
    A a = new A(b);
    System.out.println(c1.value());
    System.out.println(c2.value());
  }
}
