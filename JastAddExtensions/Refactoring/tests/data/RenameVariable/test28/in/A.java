// RenameVariable/test28/in/A.java p A f g
package p;

public class A<T> {
  T f;
  T m(int g) {
    String s = new A<String>().f;
    return null;
  }
}
