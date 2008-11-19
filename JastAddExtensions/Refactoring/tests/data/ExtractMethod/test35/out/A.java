class A {
  int test(int y) {
    int x;
    y = n(y);
    x = y;
    y = y + x;
    return y;
  }
  private int n(int y) {
    int x;
    if(y > 0) {
      x = 1;
      y = y + x;
    }
    return y;
  }
  A() {
    super();
  }
}
