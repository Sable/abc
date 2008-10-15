// PushDownMethod/test2/in/A.java p A m()
package p;

class B {
  void m() { }
}

public class A extends B {
  void m() { }
}

class C extends A {
}