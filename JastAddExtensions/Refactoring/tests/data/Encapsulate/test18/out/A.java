package p;

class A {
  private int i;
  void m() {
    postIncI();
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
  public int postIncI() {
    return i++;
  }
}
