// RenameType/test28/in/A.java p A B
package p;
class Super{
  void m1(){};
}
class A extends Super{
  class Inner{
    void m(){
      A.super.m1();
    }
  }
}
