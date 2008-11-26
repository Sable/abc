class A {
  void m() {
    boolean b = true;
    int i = 0;
    while(b){
      i = extracted(i);
    }
  }
  protected int extracted(int i) {
    System.out.println(i++);
    return i;
  }
  A() {
    super();
  }
}
