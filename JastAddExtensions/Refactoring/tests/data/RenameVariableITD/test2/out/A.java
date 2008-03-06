package p;
aspect X {
  static int a;
  void A.m() {
    int i = X.a;
  }
  X() {
    super();
  }
}

class A {
  int a;
  A() {
    super();
  }
}
