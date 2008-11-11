class A {
  void m() {
    int i;
    λ (| int i) : void {
      i = 23;
      i = 42;
    }(| i);
    System.out.println(i);
  }
  A() {
    super();
  }
}