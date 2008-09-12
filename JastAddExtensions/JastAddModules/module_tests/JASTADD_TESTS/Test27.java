package test;

public aspect Test27 {
  ast A ::= B*;
  ast B;

  inh int B.value();
  eq A.getB(int index).value() = index;
  
  public static void main(String[] args) {
    System.out.println("Inh: inherited attribute with equation for lists using index in equations");
    B b1 = new B();
    B b2 = new B();
    List list = new List().add(b1).add(b2);
    A a = new A(list);
    System.out.println(b1.value());
    System.out.println(b2.value());
  }
}
