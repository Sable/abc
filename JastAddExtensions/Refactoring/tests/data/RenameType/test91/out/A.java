package p;
public class A<T extends java.lang.Object> {
  T g;
  T m(int g) {
    String s = new A<String>().g;
    return null;
  }
  public A() {
    super();
  }
}
