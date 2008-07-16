package p;

public class A {
  
  class B {
    int b;
    B() {
      super();
    }
  }
  
  class C extends B {
    int b;
    C() {
      super();
    }
  }
  
  class D {
    C c;
    int m() {
      return ((B)c).b;
    }
    D() {
      super();
    }
  }
  public A() {
    super();
  }
}
