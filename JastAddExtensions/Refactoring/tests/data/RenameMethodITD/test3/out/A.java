package p;
aspect X {
  static int n() {
    return 23;
  }
  int A.x = X.n();

  X() {
    super();
  }
}

public class A {
  int n() {
    return 42;
  }
  {
    System.out.println(x);
  }
  public A() {
    super();
  }
}
