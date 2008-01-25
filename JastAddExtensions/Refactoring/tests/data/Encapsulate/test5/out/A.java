package p;
public class A {
  private java.lang.String s;
  class B {
    void m() {
      setS("foo");
    }
    B(){
      super();
    }
  }
  public A(){
    super();
  }
  java.lang.String getS() {
    return s;
  }
  java.lang.String setS(java.lang.String s) {
    return this.s = s;
  }
}
