import java.io.FileNotFoundException;

class A {
  static void m() throws java.io.FileNotFoundException {
    int i;
    i = n();
    int j = ++i;
  }
  A(){
    super();
  }
  private static int n() throws java.io.FileNotFoundException {
    int i;
    i = 2;
    for(int j; j < i; ++j) {
      if(j == 4) throw new java.io.FileNotFoundException("");
      ++i;
    }
    return i;
  }
}
