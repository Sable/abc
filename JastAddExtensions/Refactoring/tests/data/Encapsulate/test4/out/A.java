package p;

public class A {
  private char c;
  public A() {
    super();
  }
  char getC() {
    return c;
  }
  char setC(char c) {
    return this.c = c;
  }
}

class B extends A {
  void m() {
    setC('A');
  }
  B() {
    super();
  }
}
