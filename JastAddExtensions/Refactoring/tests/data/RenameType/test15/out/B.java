package p;

class B {
  static void s() {
  }
  B() {
    super();
  }
}

class AA {
  AA() {
    super();
    B.s();
  }
}
