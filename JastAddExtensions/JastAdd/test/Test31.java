package test;

public aspect Test31 {
  ast Program ::= A;
  abstract ast A;
  ast B : A;
  ast C : A;

  syn int A.value();
  eq B.value() = 1;
  eq C.value() = 2;

  rewrite B {
    when(value() == 1)
    to A {
      return new C();
    }
  }

  rewrite B {
    when(true)
    to A {
      throw new Error("This should not happen");
    }
  }
  
  public static void main(String[] args) {
    System.out.println("Rewrite: rewrite node B into new C using multiple matching rewrite clauses choosing using lexical order");
    Program p = 
      new Program(
        new B()
      );
    System.out.println(p.getA().value());
  }
}
