package test;

public aspect Test55 {
  ast A ::= B;
  ast B;

  syn boolean A.x() circular [false] = getB().y();
  inh boolean B.y() circular [false];
  eq A.getB().y() = x() || true;
  
  public static void main(String[] args) {
    System.out.println("Circular: two mutually circular attributes that may require an iteration before termination");
    B b = new B();
    A a = new A(b);
    System.out.println("x() == true: " + (a.x() == true));
    System.out.println("y() == true: " + (b.y() == true));
  }
}
