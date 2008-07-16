package p;

public class A {
  public void n() {
    System.out.println(42);
  }
  public A() {
    super();
  }
}

class B {
  static void n() {
    System.out.println(23);
  }
  
  class C extends A {
    {
      B.n();
    }
    C() {
      super();
    }
  }
  B() {
    super();
  }
}
