package p;
class A {
  private int i;
  A(){
    super();
  }
  public int getI() {
    return i;
  }
  public int setI(int i) {
    return this.i = i;
  }
}

class B {
  void m() {
    p.A a = new p.A();
    a.setI(a.getI() + a.getI());
  }
  B(){
    super();
  }
}
