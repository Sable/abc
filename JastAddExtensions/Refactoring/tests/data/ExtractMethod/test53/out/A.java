class A {
  int f(int n) throws Exception {
    int i = 0;
    while(i < n){
      i = extracted(i, n);
    }
    return n;
  }
  protected int extracted(int i, int n) throws Exception {
    i++;
    if(i == 23) {
      n += 42;
      throw new Exception("" + n);
    }
    return i;
  }
  A() {
    super();
  }
}
