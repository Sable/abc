class A {
  void m() {
    int i;
    // here
    {
      i = 23;
      i = 42;
    }
    System.out.println(i);
  }
}