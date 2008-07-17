public class A {
  static final int j = 23;
  static final int i = j;
  public A() {
    super();
  }
}

class B extends A {
  int j = 42;
  int k = A.j;
  B() {
    super();
  }
}
