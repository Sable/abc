import java.io.FileNotFoundException;

class A {
  void m() throws java.io.FileNotFoundException {
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
