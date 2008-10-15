package p;

public class A {
  public A() {
    super();
  }
}

class B extends A {
  B() {
    super();
  }
  void m() {
  }
}

class C {
  {
    new B().m();
  }
  C() {
    super();
  }
}
