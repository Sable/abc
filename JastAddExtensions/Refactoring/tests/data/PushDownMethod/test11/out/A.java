package p;

public class A {
  int x = 23;
  public A() {
    super();
  }
}

class B extends A {
  B() {
    super();
  }
  void m() {
    System.out.println(x);
  }
}
