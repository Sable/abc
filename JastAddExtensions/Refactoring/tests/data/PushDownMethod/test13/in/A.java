// PushDownMethod/test13/in/A.java p B m()
package p;

class A {
  void k() { System.out.println(23); }
}

class B extends A {
  void m() { super.k(); }
  void k() { System.out.println(42); } 
}

class C extends B {
  public static void main(String[] args) {
    new C().m();
  }
}
