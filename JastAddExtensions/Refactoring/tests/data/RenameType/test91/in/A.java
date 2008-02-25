// RenameType/test91/in/A.java p A B
package p;

public class A<T> {
  T f;
  T m(int g) {
    String s = new A<String>().f;
    return null;
  }
}
