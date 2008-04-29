// RenameMethod/test36/in/A.java p A m() n
package p;

public class A {
  static String m() { return "hello"; }
}

class C {
  static class B extends A {
    static String n() { return "world!"; }
  }
  static { System.out.println(C.B.m().length()); }
}
