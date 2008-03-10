package p;
aspect X {
  private int A.i;
  X() {
    super();
  }
  public int setI(int i) {
    return this.i = i;
  }
  public int getI() {
    return i;
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
