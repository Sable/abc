class A {
  void m() {
    int x = 23;
    System.out.println(this.x);
  }
  int x = 42;
  void n() {
    System.out.println(x);
  }
  A() {
    super();
  }
}