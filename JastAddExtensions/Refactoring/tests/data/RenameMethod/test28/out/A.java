package p;

class Z {
  void n() {
  }
  Z() {
    super();
  }
}

public class A {
  boolean[] n(int i) {
    return null;
  }
  
  class B extends Z {
    void p() {
      A.this.n(42);
    }
    B() {
      super();
    }
  }
  public A() {
    super();
  }
}
