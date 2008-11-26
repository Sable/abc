class A {
  void m() {
    boolean b = false;
    int i = 23;
    i = extracted(i, b);
    System.out.println(i);
  }
  protected int extracted(int i, boolean b) {
    if(b) 
      i = 42;
    return i;
  }
  A() {
    super();
  }
}
