// RenameType/test106/in/A.java p A.B.C F
package p;

public class A {
  static class B {
    static class C {
      static class D { }
    }
  }
}

class E extends A.B.C.D { }