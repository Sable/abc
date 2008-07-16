package p;

class A {
  protected int g;
  void m() {
    g++;
  }
  A() {
    super();
  }
}

class B extends A {
  void m() {
    g = 0;
  }
  B() {
    super();
  }
}
