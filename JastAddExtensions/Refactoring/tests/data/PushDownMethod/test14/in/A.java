// PushDownMethod/test14/in/A.java p A m()
package p;

class A {
  class B { public B() { System.out.println(23); } }
  B m() { return new B(); }
}

class C extends A {
  class B { public B() { System.out.println(42); } }
  public static void main(String[] args) {
    new C().m();
  }
}
