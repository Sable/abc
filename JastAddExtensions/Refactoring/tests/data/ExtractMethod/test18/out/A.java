import java.io.FileNotFoundException;

class A {
  void m() throws FileNotFoundException {
    int i;
    i = n();
    int j = ++i;
  }
  A() {
    super();
  }
  private int n() throws FileNotFoundException {
    int i;
    i = 2;
    if(i == 2) 
      throw new FileNotFoundException("");
    return i;
  }
}
