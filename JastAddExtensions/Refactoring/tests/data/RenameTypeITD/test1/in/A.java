// RenameTypeITD/test1/in/A.java p A B
package p;

aspect X {
  static class B { }
  public void A.m() { 
    B a;
  }
}

class A{
}