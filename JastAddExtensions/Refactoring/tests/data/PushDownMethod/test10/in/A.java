// PushDownMethod/test10/in/A.java p A m()
package p;

public class A {
  void m() { }
}

class B extends A {
}

class C {
  { new B().m(); }
}