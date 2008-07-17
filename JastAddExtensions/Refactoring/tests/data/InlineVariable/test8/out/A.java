class A {
  static final int j = 23;
  static final int i = j;
  int m() {
    int j = 42;
    return A.j;
  }
  A() {
    super();
  }
}
