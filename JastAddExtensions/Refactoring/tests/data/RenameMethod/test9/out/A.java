package p;

class Z {
  void n() {
  }
  Z() {
    super();
  }
}

public class A extends Z {
  void n() {
    super.n();
  }
  public A() {
    super();
  }
}
