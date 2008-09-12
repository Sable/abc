package test;

public aspect Test33 {
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
    System.out.println("Nodecopy: check that node is copied including is$Final attribute");
    Program p = 
      new Program(
        new B()
      );
    A a = p.getA();
    A c = (A)a.fullCopy();
    System.out.println("fullCopy is instanceof C: " + (c instanceof C));
    System.out.println("a is$Final: " + a.is$Final);
    System.out.println("c is$Final: " + c.is$Final);
  }
}
