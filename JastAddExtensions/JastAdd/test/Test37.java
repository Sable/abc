package test;

public aspect Test37 {
  ast A ::= B /NTAB:B/;
  ast B;

  syn lazy B A.getNTAB() = new B();

  inh int B.value();
  eq A.getB().value() = 1;
  eq A.getNTAB().value() = 2;

  public static void main(String[] args) {
    System.out.println("NTA: check that NTA is not included in generic traversal");
    A a = new A(new B());
    for(int i = 0; i < a.getNumChild(); i++)
      System.out.println(((B)a.getChild(i)).value());
    System.out.println(a.getNTAB().value());
  }
}
