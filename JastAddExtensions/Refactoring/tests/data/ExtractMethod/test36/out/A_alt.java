class A {
  void test(int x, int y) {
    while(x < 0){
      y = extracted(x, y);
      x = y - 1;
    }
  }
  protected int extracted(int y, int x) {
    doStuff(--x);
    y++;
    return y;
  }
  void doStuff(int x) {
  }
  A() {
    super();
  }
}
