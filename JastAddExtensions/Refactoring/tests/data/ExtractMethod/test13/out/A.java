class A {
  class MyExn extends Throwable {
    MyExn(){
      super();
    }
  }
  void m(int k) throws java.lang.Throwable {
    int i = k + 1;
    i = 2;
    i = n(i);
    int j = ++i;
  }
  A(){
    super();
  }
  private int n(int i) throws A.MyExn {
    for(int j; j < i; ++j) {
      if(j == 4) throw new A.MyExn();
      ++i;
    }
    return i;
  }
}
