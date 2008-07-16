package p;

class A {
  private int i;
  A[] a;
  void m() {
    a[preIncI()].preIncI();
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
  public int preIncI() {
    return ++i;
  }
}
