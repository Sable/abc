package p;
class B {
  B(){
    super();
  }
  {
  }
  static int fgT;
}

class C {
  void s() {
    new p.B();
    p.B.fgT = 6;
  }
  C(){
    super();
  }
}
