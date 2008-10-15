package p;

public class A {
  int x = 23;
  public A() {
    super();
  }
}

class B extends A {
  int x = 42;
  B() {
    super();
  }
  void m() {
    System.out.println(super.x);
  }
}
