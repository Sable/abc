import java.io.FileNotFoundException;

class A {
  void m() throws java.io.FileNotFoundException {
    int i;
    i = n();
    for(int j; j < i; ++j) {
      if(j == 4) throw new java.io.FileNotFoundException("");
      ++i;
    }
    int j = ++i;
  }
  A(){
    super();
  }
  private int n() {
    int i;
    i = 2;
    return i;
  }
}
