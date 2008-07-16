package p;

public class A {
  int y;
  void m() {
    for(int y = 0; true; ++y) 
      y = this.y;
  }
  public A() {
    super();
  }
}
