package p;

class B extends Exception {
  B() {
    super();
  }
}

class C {
  C() throws B {
    super();
  }
}
