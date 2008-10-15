// PushDownMethod/test9/in/A.java p A m()
package p;

public class A {
  void m() { }
}

class B extends A {
}

class C {
  { new A().m(); }
}