package p;

class B {
  static B A;
  B() {
    super();
  }
}

class X extends B {
  void x() {
    B.A = A.A;
  }
  X() {
    super();
  }
}
