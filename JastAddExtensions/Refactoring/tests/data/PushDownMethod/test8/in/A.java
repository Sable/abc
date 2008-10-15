// PushDownMethod/test8/in/A.java p A m()
package p;

public class A {
  void m() { }
  void f() { m(); }
}

class B extends A {
}