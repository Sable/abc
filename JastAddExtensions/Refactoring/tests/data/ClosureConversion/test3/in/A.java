class A {
  void m() {
    int i = 23;
    // here
    {
      i = 42;
    }
    System.out.println(i);
  }
}