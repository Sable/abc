package p;
aspect A {
  static void n() {
  }
  public B.B() {
    super();
    n();
  }
  A() {
    super();
  }
}

class B {
  B() {
    super();
  }
}
