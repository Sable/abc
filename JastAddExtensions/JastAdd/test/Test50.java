package test;

public aspect Test50 {
  ast A ::= B;
  ast B;

  syn boolean A.a() circular [false] = true;
  syn lazy boolean A.b() circular [false] = true;
  syn boolean A.c() circular [false] { return true; };
  syn lazy boolean A.d() circular [false] { return true; };
  syn boolean A.e() circular [false];
  eq A.e() = true;
  syn lazy boolean A.f() circular [false];
  eq A.f() { return true; };


  eq A.getB().g() = true;
  inh boolean B.g() circular [false];
  eq A.getB().h() = true;
  inh lazy boolean B.h() circular [false];

  public static void main(String[] args) {
    System.out.println("Circular: test syntax for syn/inh circular declarations");
  }
}
