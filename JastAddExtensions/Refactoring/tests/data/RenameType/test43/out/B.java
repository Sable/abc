package p;
public class B {
  static p.B A;
  public B(){
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
