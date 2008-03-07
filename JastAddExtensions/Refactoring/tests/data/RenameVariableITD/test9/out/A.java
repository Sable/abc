package p;
aspect X {
  int A.m() {
    int a;
    return this.a;
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
