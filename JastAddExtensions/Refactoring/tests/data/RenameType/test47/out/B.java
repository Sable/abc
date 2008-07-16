package p;

class B {
  B() {
    super();
  }
}

class C {
  void s() {
    new B();
  }
  C() {
    super();
  }
}
