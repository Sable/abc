class A {
  int x;
  void m() {
    int x;
    {
      this.x = 1;
      x = 2;
    }
    System.out.println(x);
  }
  A() {
    super();
  }
}