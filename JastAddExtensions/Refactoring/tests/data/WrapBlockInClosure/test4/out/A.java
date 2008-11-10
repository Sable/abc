class A {
  void m() {
    λ () : void {
      System.out.print("Hello, ");
      {
        System.out.println("world!");
      }
    }();
  }
  A() {
    super();
  }
}