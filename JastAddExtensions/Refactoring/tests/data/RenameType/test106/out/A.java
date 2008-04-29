package p;
public class A {
  static class B {
    static class F {
      static class D {
        D() {
          super();
        }
      }
      F() {
        super();
      }
    }
    B() {
      super();
    }
  }
  public A() {
    super();
  }
}

class E extends A.B.F.D {
  E() {
    super();
  }
}
