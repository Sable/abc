import java.io.FileNotFoundException;

class A {
  int m() throws FileNotFoundException {
    int i;
    int j;
    j = n();
    return j;
  }
  A() {
    super();
  }
  private int n() throws FileNotFoundException {
    int i;
    int j;
    i = 2;
    if(i == 2) 
      throw new FileNotFoundException("");
    j = ++i;
    return j;
  }
}
