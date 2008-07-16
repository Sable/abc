package p;

public class B {
  public B() {
    super();
  }
}

class C {
  class D<T extends java.lang.Object> {
    D() {
      super();
    }
  }
  C() {
    super();
  }
}

class E {
  C.D<B> b = new C().new D<B>();
  E() {
    super();
  }
}
