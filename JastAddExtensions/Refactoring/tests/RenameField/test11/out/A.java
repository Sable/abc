package p;
class A {
  int g;
  A(){
    super();
  }
}

class B extends A {
  p.A a;
  void m() {
    int g = a.g;
  }
  B(){
    super();
  }
}
