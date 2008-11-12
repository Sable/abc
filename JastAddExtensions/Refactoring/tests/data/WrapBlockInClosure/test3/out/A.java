class A {
  void m() {
    (λ () : void {
      System.out.println("Hello!");
      return ;
    })@();
  }
  A() {
    super();
  }
}