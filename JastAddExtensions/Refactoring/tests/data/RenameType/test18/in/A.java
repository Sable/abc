// RenameType/test18/in/A.java p A B
package p;
class A extends Exception{
}
class AA{
  void m(){
    try {
      throw new A();
    }
    catch(A a){}
  }
}