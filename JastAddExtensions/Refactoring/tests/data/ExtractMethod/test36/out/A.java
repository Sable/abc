class A {
  void test(int x, int y) {
    while(x < 0){
      y = extracted(x, y);
      x = y - 1;
    }
  }
  protected int extracted(int x, int y) {
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
