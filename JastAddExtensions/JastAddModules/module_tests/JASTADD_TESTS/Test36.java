package test;

public aspect Test36 {
  ast A ::= B /NTAB:B/;
  ast B;

  syn lazy B A.getNTAB() = new B();

  inh int B.value();
  eq A.getB().value() = 1;
  eq A.getNTAB().value() = 2;

  public static void main(String[] args) {
    System.out.println("NTA: non terminal attribute implemented using syn eq");
    A a = new A(new B());
    System.out.println(a.getB().value());
    System.out.println(a.getNTAB().value());
  }
}
