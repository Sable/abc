package p;
class B {
  static p.B A;
  B(){
    super();
  }
}

class X extends B {
  void x() {
    p.B.A = A.A;
  }
  X(){
    super();
  }
}
