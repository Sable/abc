package p;

public class B {
  public static void main(String[] args) {
    C<B> a = new D<String>();
  }
  public B() {
    super();
  }
}
class C<T extends java.lang.Object> {
  C() {
    super();
  }
}
class D<B extends java.lang.Object> extends C<p.B> {
  D() {
    super();
  }
}
