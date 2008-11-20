class A {
  void m() {
    n((int)'x');
  }
  void n(int i) {
    System.out.println("here");
  }
  void n(char c) {
    System.out.println("there");
  }
  A() {
    super();
  }
}
