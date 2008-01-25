import java.io.FileNotFoundException;

class A {
  int m() throws java.io.FileNotFoundException {
    int i;
    int j;
    j = n();
    return j;
  }
  A(){
    super();
  }
  private int n() throws java.io.FileNotFoundException {
    int j;
    int i;
    i = 2;
    if(i == 2) throw new java.io.FileNotFoundException("");
    j = ++i;
    return j;
  }
}
