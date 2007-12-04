// RenameType/test19/in/A.java p A B
package p;
class A extends Exception{
  void m(){
    try {
      throw new A();
    }
    catch(A A){}
  }
}
