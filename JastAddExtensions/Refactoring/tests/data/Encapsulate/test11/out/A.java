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
  public int assignPlusI(int i) {
    return this.i += i;
  }
}

class B {
  void m() {
    p.A a = new p.A();
    a.assignPlusI(a.getI());
  }
  B(){
    super();
  }
}
