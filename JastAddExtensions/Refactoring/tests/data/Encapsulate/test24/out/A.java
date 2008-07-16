package p;

public class A {
  int x;
  private A a;
  void m() {
    getA().x = 3;
  }
  public A() {
    super();
  }
  A getA() {
    return a;
  }
  A setA(A a) {
    return this.a = a;
  }
}
