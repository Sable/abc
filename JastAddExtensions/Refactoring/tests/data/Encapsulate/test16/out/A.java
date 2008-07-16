package p;

class A {
  private int i;
  void m() {
    setI(getI());
  }
  A() {
    super();
  }
  public int getI() {
    return i;
  }
  public int setI(int i) {
    return this.i = i;
  }
}
