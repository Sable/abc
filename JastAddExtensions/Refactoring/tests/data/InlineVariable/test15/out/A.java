class C {
  static final int j = 23;
  C() {
    super();
  }
}

public class A {
  public static final int i = C.j;
  public A() {
    super();
  }
}

class B {
  int k = C.j;
  B() {
    super();
  }
}
