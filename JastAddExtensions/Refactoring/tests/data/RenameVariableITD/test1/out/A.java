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
aspect X {
  int A.B.a;

  X() {
    super();
  }
}
