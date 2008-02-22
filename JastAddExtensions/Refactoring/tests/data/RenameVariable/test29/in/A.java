// RenameVariable/test29/in/A.java p A f g
package p;

public class A<T> {
  T f;
  String m() {
    return new B().f;
  }
}
class B extends A<String> {
  String g;
}
