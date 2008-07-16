import java.io.FileNotFoundException;

class A {
  void m() throws FileNotFoundException {
    int i;
    i = n();
    for(int j = 0; j < i; ++j) {
      if(j == 4) 
        throw new FileNotFoundException("");
      ++i;
    }
    int j = ++i;
  }
  A() {
    super();
  }
  private int n() {
    int i;
    i = 2;
    return i;
  }
}
