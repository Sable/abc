// RenameType/test65/in/A.java p A.X XYZ
package p;
public class A{
  class X{ }
}
class B{
  A a = new A();
  A.X ax = a.new X();
}