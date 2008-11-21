class A {
  void m() {
    int i = 23;
    (λ (out int i) : void {
      i = 42;
    })@(i);
    System.out.println(i);
  }
  A() {
    super();
  }
}