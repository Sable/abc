package p;
class A {
  protected int g;
  void m() {
    g++;
  }
  A(){
    super();
  }
}

class B {
  p.A a;
  protected int f;
  void m() {
    a.g = 0;
  }
  B(){
    super();
  }
}
