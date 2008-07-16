package p;

class Z {
  static void n() {
  }
  Z() {
    super();
  }
}

public class A extends Z {
  static void n() {
  }
  static {
    Z.n();
  }
  public A() {
    super();
  }
}
