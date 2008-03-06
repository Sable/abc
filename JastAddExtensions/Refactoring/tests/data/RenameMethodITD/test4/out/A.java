package p;
aspect A {
  static int n() {
    return 23;
  }
  public B.B() {
    this(n());
  }
  A() {
    super();
  }
}

class B {
  public B(int i) {
    super();
  }
}
