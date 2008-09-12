package test;

public aspect Test22 {
  ast A ::= Left:B Right:B ;
  ast B;

  inh int B.value();
  eq A.getLeft().value() = 1;
  eq A.getRight().value() = 2;
  
  public static void main(String[] args) {
    System.out.println("Inh: inherited attribute with multiple equations");
    B b1 = new B();
    B b2 = new B();
    A a = new A(b1, b2);
    System.out.println(b1.value());
    System.out.println(b2.value());
  }
}
