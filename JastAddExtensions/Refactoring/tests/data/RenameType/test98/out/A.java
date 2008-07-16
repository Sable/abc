package p;

public class A {
   <T extends java.lang.Object> int m() {
    T s;
    return 23;
  }
  public A() {
    super();
  }
}

class B {
  int x = new A().<B>m();
  B() {
    super();
  }
}
