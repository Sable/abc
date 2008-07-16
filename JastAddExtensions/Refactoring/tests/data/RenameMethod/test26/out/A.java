package p;

class Z {
  int n() {
    return 42;
  }
  Z() {
    super();
  }
}

public class A extends Z {
  A a;
  int n() {
    return 23;
  }
  public A() {
    super();
  }
}

class B {
  A a;
  int p() {
    return a.a.n();
  }
  B() {
    super();
  }
}
