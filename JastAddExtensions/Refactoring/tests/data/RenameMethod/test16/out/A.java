package p;

public class A {
  void n() {
  }
  
  class B {
    void n() {
    }
    void p() {
      A.this.n();
    }
    B() {
      super();
    }
  }
  public A() {
    super();
  }
}
