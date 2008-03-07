aspect X {
  static int x;
  int A.m(int y) {
    y = n(y);
    System.out.println(y);
    return y;
  }
  X() {
    super();
  }
  private int n(int y) {
    y += x;
    return y;
  }
}

class A {
  A() {
    super();
  }
}
