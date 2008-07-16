package p;
aspect X {
  private int A.i;

  X() {
    super();
  }
  public int A.getI() {
    return i;
  }
  public int A.setI(int i) {
    return this.i = i;
  }
}

public class A {
  public int f(int j) {
    return getI() + j;
  }
  public A() {
    super();
  }
}
