package p;
class B extends Exception {
  void m() {
    try {
      throw new p.B();
    }
    catch (p.B A) {
    }
  }
  B(){
    super();
  }
}
