class A {
  void m() {
    int y;
    int z;
    n();
  }
  A() {
    super();
  }
  private void n() {
    int z;
    int y;
    try {
      if(3 == 3) 
        y = 1;
      else 
        throw new Exception("boo");
    }
    catch (Throwable t) {
      y = 2;
    }
    z = y;
  }
}
