package test;

public aspect Test32 {
  ast Program ::= A;
  abstract ast A;
  ast B : A;
  ast C : A;

  syn int A.value();
  eq B.value() = 1;
  eq C.value() = 2;

  rewrite B {
    when(value() == 1)
    to A new C();
  }
  
  public static void main(String[] args) {
    System.out.println("Rewrite: rewrite node B into new C using conditional rewrite short form");
    Program p = 
      new Program(
        new B()
      );
    System.out.println(p.getA().value());
  }
}
