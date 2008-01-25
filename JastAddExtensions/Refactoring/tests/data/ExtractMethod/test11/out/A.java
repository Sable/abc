import java.io.FileNotFoundException;

class A {
  void m(int k) throws java.io.FileNotFoundException {
    int i = k + 1;
    n();
  }
  A(){
    super();
  }
  private void n() throws java.io.FileNotFoundException {
    int i;
    i = 2;
    for(int j; j < i; ++j) {
      if(j == 4) throw new java.io.FileNotFoundException("");
      ++i;
    }
    int j = ++i;
  }
}
