class A {
  void m() {
    String msg = "Hello, ";
    {
      System.out.println(msg);
    }
    System.out.println("world!");
  }
  void n(String msg) {
    System.out.println(msg);
  }
  A() {
    super();
  }
}