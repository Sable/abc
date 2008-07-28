package test;

public aspect Test64 {
  ast A ::= B;
  ast B;

  eq A.getChild(int i).value() = 1;
  inh int B.value();

  public static void main(String[] args) {
	  System.out.println("Inh: inherited attribute defined using getChild()");
	  B b = new B();
	  A a = new A(b);
	  System.out.println(b.value());
  }
}
