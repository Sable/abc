package p;

public class A {
  private String s;
  
  class B {
    void m() {
      setS("foo");
    }
    B() {
      super();
    }
  }
  public A() {
    super();
  }
  String getS() {
    return s;
  }
  String setS(String s) {
    return this.s = s;
  }
}
