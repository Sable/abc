import java.io.FileNotFoundException;

class A {
  void m() throws java.io.FileNotFoundException {
    int i;
    i = n();
    int j = ++i;
  }
  A(){
    super();
  }
  private int n() throws java.io.FileNotFoundException {
    int i;
    i = 2;
    if(i == 2) throw new java.io.FileNotFoundException("");
    return i;
  }
}
