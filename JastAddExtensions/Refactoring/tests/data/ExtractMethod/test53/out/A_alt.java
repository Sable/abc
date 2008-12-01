class A {
  int f(int n) throws Exception {
    int i = 0;
    while(i < n){
      i = extracted(n, i);
    }
    return n;
  }
  protected int extracted(int n, int i) throws Exception {
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
