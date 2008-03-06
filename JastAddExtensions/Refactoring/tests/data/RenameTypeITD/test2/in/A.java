// RenameTypeITD/test2/in/A.java p C.A B
package p;

aspect X {
  static class B { }
  public void A.m() { 
    B a;
  }
}

class C {
  class A { }
}

class A extends C {
}