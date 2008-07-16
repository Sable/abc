package p;

public class A {
  private int i;
  public void m() {
    assignPlusI(preDecI());
  }
  public A() {
    super();
  }
  public int getI() {
    return i;
  }
  public int setI(int i) {
    return this.i = i;
  }
  public int preDecI() {
    return --i;
  }
  public int assignPlusI(int i) {
    return this.i += i;
  }
}
