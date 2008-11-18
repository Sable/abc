class A {
  
  class MyExn extends Throwable {
    MyExn() {
      super();
    }
  }
  void m(int k) throws Throwable {
    int i = k + 1;
    i = 2;
    i = n(i);
    int j = ++i;
  }
  private int n(int i) throws MyExn {
    for(int j = 0; j < i; ++j) {
      if(j == 4) 
        throw new MyExn();
      ++i;
    }
    return i;
  }
  A() {
    super();
  }
}
