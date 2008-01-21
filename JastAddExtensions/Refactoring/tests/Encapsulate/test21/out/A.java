package p;
public class A {
  private int i;
  public void m() {
    setI(getI() + setI(getI() - 1));
  }
  public A(){
    super();
  }
  public int getI() {
    return i;
  }
  public int setI(int i) {
    return this.i = i;
  }
}
