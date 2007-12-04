// RenameType/test26/in/A.java p A B
package p;
class A{
  int x;
  class Inner{
    void m(){
      A.this.x++;
    }
  }
}