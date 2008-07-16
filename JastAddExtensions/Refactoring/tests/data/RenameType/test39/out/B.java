package p;

public class B {
  public B() {
    super();
  }
}

class C {
  static {
    B a;
  }
  C() {
    super();
  }
}
