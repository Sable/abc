package p;

public class A {
  private int i;
  public A() {
    super();
  }
  protected int getI() {
    return i;
  }
  protected int setI(int i) {
    return this.i = i;
  }
  protected int preIncI() {
    return ++i;
  }
}

class B extends A {
  B() {
    super();
  }
}

class C {
  void m() {
    B b = new B();
    b.preIncI();
  }
  C() {
    super();
  }
}
