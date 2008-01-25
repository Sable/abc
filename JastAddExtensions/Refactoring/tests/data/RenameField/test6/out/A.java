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

class AA extends A {
  protected int f;
  AA(){
    super();
  }
}

class B {
  p.A a;
  p.AA b;
  p.A ab = new p.AA();
  void m() {
    a.g = 0;
    b.f = 0;
    ab.g = 0;
  }
  B(){
    super();
  }
}
