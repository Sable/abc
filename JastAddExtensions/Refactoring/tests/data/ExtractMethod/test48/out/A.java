class A {
  void m(String ... args) {
    extracted(args);
  }
  protected void extracted(String ... args) {
    System.out.println(args[0]);
  }
  A() {
    super();
  }
}
