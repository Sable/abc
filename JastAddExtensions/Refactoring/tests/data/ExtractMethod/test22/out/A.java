import java.io.FileNotFoundException;

class A {
  int m() throws FileNotFoundException {
    int i;
    int j;
    j = n();
    return j;
  }
  private int n() throws FileNotFoundException {
    int j;
    int i;
    i = 2;
    if(i == 2) 
      throw new FileNotFoundException("");
    j = ++i;
    return j;
  }
  A() {
    super();
  }
}
