import java.io.FileNotFoundException;
class A {
  void m() throws FileNotFoundException {
    int i;
    i = 2;
    n(i);
  }
  A() {
    super();
  }
  private void n(int i) throws FileNotFoundException {
    for(int j = 0; j < i; ++j) {
      if(j == 4) throw new FileNotFoundException("");
      ++i;
    }
    int j = ++i;
  }
}
