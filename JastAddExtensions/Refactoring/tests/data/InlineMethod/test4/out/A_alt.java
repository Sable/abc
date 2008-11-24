class A {
  void m() {
    int i;
    {
      int i1 = 23;
      i = 42;
    }
  }
  int n(int i) {
    return 42;
  }
  A() {
    super();
  }
}