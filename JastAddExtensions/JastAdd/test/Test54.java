package test;

public aspect Test54 {
  ast A ::= B;
  ast B;

  inh boolean B.t() circular [false];
  eq A.getB().t() = getB().t();
  
  public static void main(String[] args) {
    System.out.println("Circular: a self-circular inherited attribute");
    B b = new B();
    A a = new A(b);
    System.out.println("t() == false: " + (b.t() == false));
  }
}
