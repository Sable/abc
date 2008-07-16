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
  public A() {
    super();
  }
}

class B extends A {
  {
    Z.n();
  }
  B() {
    super();
  }
}
