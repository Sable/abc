package p;
public class A {
  private java.lang.String s;
  public A(){
    super();
  }
  public java.lang.String getS() {
    return s;
  }
  public java.lang.String setS(java.lang.String s) {
    return this.s = s;
  }
  public java.lang.String assignPlusS(java.lang.String s) {
    return this.s += s;
  }
}

class B {
  void m() {
    p.A a = new p.A();
    a.assignPlusS("foo");
  }
  B(){
    super();
  }
}
