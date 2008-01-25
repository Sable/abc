// RenameType/test15/in/A.java p A B
package p;
class A{
   static void s(){};
}
class AA{
   AA(){ 
     A.s();
   };   
}