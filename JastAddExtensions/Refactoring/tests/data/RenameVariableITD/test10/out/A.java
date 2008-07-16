package p;
aspect X {
  int A.g;

  X() {
    super();
  }
}

class A {
  A() {
    super();
  }
}

class B {
  int m(A a) {
    return a.g;
  }
  B() {
    super();
  }
}
