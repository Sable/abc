class A {
  void m() {
    int i = 2;
    λ (int i) : void {
      System.out.println(i + i);
    }(i);
  }
  A() {
    super();
  }
}