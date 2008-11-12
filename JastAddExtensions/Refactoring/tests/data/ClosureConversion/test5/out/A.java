class A {
  void m() {
    int i;
    (λ (| int i) : void {
      i = 2;
    })@(| i);
    System.out.println(i);
  }
  A() {
    super();
  }
}