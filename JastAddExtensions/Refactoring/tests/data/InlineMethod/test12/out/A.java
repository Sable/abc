class A {
  void m() {
    int j;
    int i = 23;
    {
      int i0 = i;
      System.out.println("here");
      j = i0 = 42;
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