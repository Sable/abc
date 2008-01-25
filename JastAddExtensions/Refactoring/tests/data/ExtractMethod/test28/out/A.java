class A {
  void m() {
    int y;
    int z;
    y = 2;
    n(y);
  }
  A(){
    super();
  }
  private void n(int y) {
    int z;
    try {
      if(3 == 3) y = 1;
      else throw new java.lang.Exception("boo");
    }
    catch (java.lang.Throwable t) {
    }
    z = y;
  }
}
