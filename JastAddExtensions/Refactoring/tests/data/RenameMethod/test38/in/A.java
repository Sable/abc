// RenameMethod/test38/in/A.java p A f(int) g
package p;

class A {
  void g(long x) {
      System.out.println(x);
  }
  void f(int x) {
      System.out.println(x+19);
  }
  { g(23); }
}
