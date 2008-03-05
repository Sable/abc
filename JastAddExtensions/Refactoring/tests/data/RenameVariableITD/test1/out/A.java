package p;
class A {
  int a;
  class B {
    void v() {
      int x = A.this.a;
    }
    B() {
      super();
    }
  }
  A() {
    super();
  }
}

class X {
  int a;
  X() {
    super();
  }
}
