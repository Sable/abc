public class A {
  static final int j = 23;
  public static final int i = j;
  public A() {
    super();
  }
}

class B {
  int k = A.j;
  B() {
    super();
  }
}
