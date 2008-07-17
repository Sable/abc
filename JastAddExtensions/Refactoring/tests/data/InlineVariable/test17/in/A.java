class A {
  int k;
  int incK() { ++k; return 0; }
  int m() {
    int i = incK();
    return i;
  }
}
