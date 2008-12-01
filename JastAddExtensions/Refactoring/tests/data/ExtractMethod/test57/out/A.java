class A {
  void m(int i) {
    if(extracted(i)) 
      return ;
    System.out.println("Phew!");
  }
  protected boolean extracted(int i) {
    if(i == 23) 
      return true;
    return false;
  }
  A() {
    super();
  }
}
