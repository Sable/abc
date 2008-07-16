package p;

public class B {
  static B A;
  public B() {
    super();
  }
}

class X extends B {
  void x() {
    B.A = A.A;
  }
  X() {
    super();
  }
}
