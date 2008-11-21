class A {
  void m() {
    int i;
    (λ (out int i) : void {
      i = 2;
    })@(i);
    System.out.println(i);
  }
  A() {
    super();
  }
}