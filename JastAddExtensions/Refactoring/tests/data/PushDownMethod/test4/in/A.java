// PushDownMethod/test4/in/A.java p A m()
package p;

abstract class B {
  abstract void m();
}

public class A extends B {
  void m() { }
}

class C extends A {
}
