// PushDownMethod/test16/in/A.java p A m()
package p;

class A {
  protected void k() { System.out.println(23); }
  void m() { k(); }
}

class B extends A {
  protected void k() { System.out.println(42); }
  public static void main(String[] args) {
    new B().m();
  }
}
