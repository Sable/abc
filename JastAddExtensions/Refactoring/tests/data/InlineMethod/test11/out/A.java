class A {
  void m() {
    int j;
    l:{
      int i = 23;
      System.out.println("here");
      j = i = 42;
      break l;
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