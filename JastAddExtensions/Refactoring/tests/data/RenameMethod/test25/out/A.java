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

class B {
  void m() {
    A a = new A();
    ((Z)a).n();
  }
  B() {
    super();
  }
}
