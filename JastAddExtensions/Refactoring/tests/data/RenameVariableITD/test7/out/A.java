package p;
aspect X {
  static int a;
  int A.a = X.a;

  X() {
    super();
  }
}

class A {
  int b;
  A() {
    super();
  }
}
