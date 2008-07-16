package p;

public class B {
  public B() {
    super();
  }
}
class C<T extends java.lang.Object> {
  C() {
    super();
  }
}

class D {
  C<B> b;
  D() {
    super();
  }
}
