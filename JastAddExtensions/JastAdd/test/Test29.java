package test;

public aspect Test29 {
  ast Program ::= A;
  abstract ast A;
  ast B : A;
  ast C : A;

  syn int A.value();
  eq B.value() = 1;
  eq C.value() = 2;

  rewrite B {
    to A {
      return new C();
    }
  }
  
  public static void main(String[] args) {
    System.out.println("Rewrite: always rewrite node B into new C with common supertype");
    Program p = 
      new Program(
        new B()
      );
    System.out.println(p.getA().value());
  }
}
