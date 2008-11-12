class A {
  void m() {
    int i;
    (λ () : void {
      int i;
      i = 2;
    })@();
  }
  A() {
    super();
  }
}