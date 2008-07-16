package p;

public class A {
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
    return a.n();
  }
  B() {
    super();
  }
}
