package p;

class B {
  B() {
    super();
  }
  static int fgT;
}

class C {
  void s() {
    new B();
    B.fgT = 6;
  }
  C() {
    super();
  }
}
