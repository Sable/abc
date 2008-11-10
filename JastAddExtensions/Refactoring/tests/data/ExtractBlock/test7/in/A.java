class A {
  void m() {
    // from
    System.out.println();
    System.out.println();
    int j;
    j = 42;
    System.out.println(j);
    int i = 23;
    // to
    System.out.println(i+j);
  }
}