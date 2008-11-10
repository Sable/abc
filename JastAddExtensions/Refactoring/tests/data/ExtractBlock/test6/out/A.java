class A {
  void m() {
    int i;
    {
      System.out.println(42);
      i = 23;
    }
    System.out.println(i);
  }
  A() {
    super();
  }
}