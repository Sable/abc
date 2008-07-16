package p;
aspect X {
  static int a;
  int A.x = X.a;

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
