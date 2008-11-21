class A {
  void m() {
    String msg = "Hello, ";
    {
      String msg0 = msg;
      System.out.println(msg0);
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