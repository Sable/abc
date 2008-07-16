package p;

public class A {
  private int i;
  public void m() {
    Object o = new Object() {
        int k = A.this.getI();
        int getI() {
          return 42;
        }
    };
  }
  public A() {
    super();
  }
  protected int getI() {
    return i;
  }
  protected int setI(int i) {
    return this.i = i;
  }
}
