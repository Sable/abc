package p;
aspect X {
  
  static class B {
    B() {
      super();
    }
  }
  public void p.B.m() {
    X.B a;
  }
  X() {
    super();
  }
}

class B {
  B() {
    super();
  }
}
