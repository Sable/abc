package p;
public aspect A {
  static void n() {
  }
  void X.r() {
    A.n();
  }
  public A() {
    super();
  }
}

class X {
  void n() {
  }
  X() {
    super();
  }
}
