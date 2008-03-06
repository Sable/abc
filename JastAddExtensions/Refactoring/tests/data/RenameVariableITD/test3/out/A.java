package p;
aspect X {
  static int a;
  void A.m(int a) {
    int i = X.a;
  }
  X() {
    super();
  }
}

class A {
  A() {
    super();
  }
}
