class A {
  void m() {
    int i;
    {
      return i = 42;
    }
  }
  int n(int i) {
    return 42;
  }
  A() {
    super();
  }
}