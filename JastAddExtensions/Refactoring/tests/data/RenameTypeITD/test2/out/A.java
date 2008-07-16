package p;
aspect X {
  
  static class B {
    B() {
      super();
    }
  }
  public void A.m() {
    X.B a;
  }
  X() {
    super();
  }
}

class C {
  
  class B {
    B() {
      super();
    }
  }
  C() {
    super();
  }
}

class A extends C {
  A() {
    super();
  }
}
