class A {
  void m() {
    int[] a = { 23 } ;
    a = extracted();
    System.out.println(a[0]);
  }
  protected int[] extracted() {
    int[] a;
    a = new int[]{ 42 } ;
    return a;
  }
  A() {
    super();
  }
}
