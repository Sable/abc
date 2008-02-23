package p;
public class A<T extends java.lang.Object> {
  T g;
  String m() {
    return ((A<String>)new B()).g;
  }
  public A() {
    super();
  }
}

class B extends A<String> {
  String g;
  B() {
    super();
  }
}
