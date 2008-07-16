package p;

class A {
  
  class C {
    C() {
      super();
    }
  }
  A() {
    super();
  }
}

class C {
  
  static class D extends A {
    int D;
    static int m() {
      return 23;
    }
    int i = p.C.D.m();
    D() {
      super();
    }
  }
  C() {
    super();
  }
}
