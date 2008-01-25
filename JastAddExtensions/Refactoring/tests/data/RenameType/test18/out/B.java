package p;
class B extends Exception {
  B(){
    super();
  }
}

class AA {
  void m() {
    try {
      throw new p.B();
    }
    catch (p.B a) {
    }
  }
  AA(){
    super();
  }
}
