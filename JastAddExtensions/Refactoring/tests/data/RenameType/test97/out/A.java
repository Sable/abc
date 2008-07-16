package p;

public class A {
   <T extends java.lang.Object> int m() {
    return 23;
  }
  public A() {
    super();
  }
}

class C {
  int x = new A().<C>m();
  C() {
    super();
  }
}
