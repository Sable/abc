class A {
  int m() {
    int i;
    i = n();
    return ++i;
  }
  void n() {
    System.out.println("Hello, world!");
  }
  A() {
    super();
  }
  private int n() {
    int i;
    i = 2;
    return i;
  }
}
