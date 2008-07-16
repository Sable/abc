package p;

class A {
  static int g = 23;
  public A(int g) {
    this(A.g, 0);
  }
  public A(int x, int y) {
    super();
    System.out.println(x);
  }
}
