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

class AA extends A {
  protected int f;
  AA() {
    super();
  }
}

class B {
  A a;
  void m() {
    a.g = 0;
  }
  B() {
    super();
  }
}
