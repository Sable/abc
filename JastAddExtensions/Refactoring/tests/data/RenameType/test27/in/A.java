// RenameType/test27/in/A.java p A B
package p;
class Super{
  int x;
}
class A extends Super{
  String x;
  class Inner{
    void m(){
      A.super.x++;
    }
  }
}
