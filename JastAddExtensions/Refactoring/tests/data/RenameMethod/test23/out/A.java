package p;

class Z {
  static int n() {
    return 72;
  }
  Z() {
    super();
  }
}

public class A extends Z {
  static int n() {
    return 23;
  }
  public A() {
    super();
  }
}

class B extends A {
  
  class C {
    int k = Z.n();
    C() {
      super();
    }
  }
  B() {
    super();
  }
}
