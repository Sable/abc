import java.io.FileNotFoundException;

class A {
  void m(int k) throws FileNotFoundException {
    int i = k + 1;
    n();
  }
  A() {
    super();
  }
  private void n() throws FileNotFoundException {
    int i;
    i = 2;
    for(int j = 0; j < i; ++j) {
      if(j == 4) 
        throw new FileNotFoundException("");
      ++i;
    }
    int j = ++i;
  }
}
