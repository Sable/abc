package p;

public class A {
  void n() {
  }
  public A() {
    super();
  }
}

class B extends A {
  void n() {
  }
  
  class C {
    void p() {
      B.super.n();
    }
    C() {
      super();
    }
  }
  B() {
    super();
  }
}
