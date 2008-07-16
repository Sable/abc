package p;

class B {
  int y;
  B() {
    super();
  }
}

public class A {
  int y;
  
  class C extends B {
    int m() {
      return A.this.y;
    }
    C() {
      super();
    }
  }
  public A() {
    super();
  }
}
