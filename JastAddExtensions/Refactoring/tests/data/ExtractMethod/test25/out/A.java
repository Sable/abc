class A {
  void m() {
    int y;
    int z;
    y = 2;
    n();
  }
  private void n() {
    int y;
    int z;
    int x = (0 < 0 ? y = 0 : (y = 1));
    z = y;
  }
  A() {
    super();
  }
}
