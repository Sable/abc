class A {
  void m() {
    int y;
    int z;
    y = 2;
    n(y);
  }
  private void n(int y) {
    int z;
    try {
      if(3 == 3) 
        y = 1;
      else 
        throw new Exception("boo");
    }
    catch (Throwable t) {
    }
    z = y;
  }
  A() {
    super();
  }
}
