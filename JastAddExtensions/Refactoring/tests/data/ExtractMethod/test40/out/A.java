class A {
  void m() {
    int i = 23;
    extracted(i);
  }
  protected void extracted(int i) {
    i = i + 19;
  }
  A() {
    super();
  }
}
