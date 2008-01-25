package p;
class Z {
  static void n() {
  }
  Z(){
    super();
  }
}

public class A extends Z {
  static void n() {
  }
  public A(){
    super();
  }
}

class B {
  void m() {
    p.A a = new p.A();
    ((p.Z)a).n();
  }
  B(){
    super();
  }
}
