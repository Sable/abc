package test;

public aspect Test21 {
  ast A ::= B;
  ast B;

  inh int B.value();
  eq A.getB().value() = 1;
  
  public static void main(String[] args) {
    System.out.println("Inh: inherited attribute with single equation");
    B b = new B();
    A a = new A(b);
    System.out.println(b.value());
  }
}
