// PushDownMethod/test3/in/A.java p A m()
package p;

interface B {
  void m();
}

public class A implements B {
  public void m() { }
}

class C extends A {
}