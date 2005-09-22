package test;

public aspect Test25 {
  ast A ::= B [MayB:B];
  ast B;

  inh int B.value();
  eq A.getB().value() = 1;
  eq A.getMayB().value() = 2;
  
  public static void main(String[] args) {
    System.out.println("Inh: inherited attribute with equation for optionals");
    B b = new B();
    B mayb = new B();
    A a = new A(b, new Opt(mayb));
    System.out.println(b.value());
    System.out.println(mayb.value());
  }
}
