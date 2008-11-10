class A {
  void m() {
    int i;
    int j;
    {
      System.out.println();
      System.out.println();
      j = 42;
      System.out.println(j);
      i = 23;
    }
    System.out.println(i + j);
  }
  A() {
    super();
  }
}