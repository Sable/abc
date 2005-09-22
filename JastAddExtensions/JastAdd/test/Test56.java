package test;

public aspect Test56 {
  ast A ::= B;
  ast B;

  syn boolean A.x(String name) circular [false] {
    System.out.println("Eval x(" + name + ")");
    return getB().y(name);
  }
  inh boolean B.y(String name) circular [false];
  eq A.getB().y(String name) {
    System.out.println("Eval y(" + name + ")");
    return x(name) || true;
  }
  
  public static void main(String[] args) {
    System.out.println("Circular: two mutually circular paramterized attributes");
    B b = new B();
    A a = new A(b);
    System.out.println("x(a) == true: " + (a.x("a") == true));
    System.out.println("y(a) == true: " + (b.y("a") == true));
    System.out.println("x(b) == true: " + (a.x("b") == true));
    System.out.println("y(b) == true: " + (b.y("b") == true));
  }
}
