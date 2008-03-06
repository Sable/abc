// RenameTypeITD/test3/in/A.java p A B
package p;

aspect X {
  class B { }
  public void A.m() { 
    B a;
  }
}

class A{
}