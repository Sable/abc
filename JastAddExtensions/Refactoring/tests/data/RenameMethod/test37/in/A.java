// RenameMethod/test37/in/A.java p A.D g() f
package p;

class A {
  private static class C {
    static int f() { return 23; }
  }
  static class D extends C {
    static int g() { return 42; }
  }
}

class B extends A {
  { new D().f(); }
}
