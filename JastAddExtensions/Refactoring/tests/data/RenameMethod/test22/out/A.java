package p;

public class A {
  static int n() {
    return 23;
  }
  
  static class B {
    static void n(int i) {
    }
    int k = A.n();
    B() {
      super();
    }
  }
  public A() {
    super();
  }
}
