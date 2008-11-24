class A {
  void m() {
    int j;
    {
      int i = 23;
      System.out.println("here");
      j = i = 42;
    }
    System.out.println("back");
  }
  int n(int i) {
    System.out.println("here");
    return i = 42;
  }
  A() {
    super();
  }
}