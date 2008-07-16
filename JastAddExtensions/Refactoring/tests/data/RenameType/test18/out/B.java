package p;

class B extends Exception {
  B() {
    super();
  }
}

class AA {
  void m() {
    try {
      throw new B();
    }
    catch (B a) {
    }
  }
  AA() {
    super();
  }
}
