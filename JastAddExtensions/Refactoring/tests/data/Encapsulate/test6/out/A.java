package p;

public class A {
  private String s;
  public A() {
    super();
  }
  public String getS() {
    return s;
  }
  public String setS(String s) {
    return this.s = s;
  }
  public String assignPlusS(String s) {
    return this.s += s;
  }
}

class B {
  void m() {
    A a = new A();
    a.assignPlusS("foo");
  }
  B() {
    super();
  }
}
