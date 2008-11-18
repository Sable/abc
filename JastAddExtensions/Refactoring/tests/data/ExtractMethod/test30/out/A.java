aspect X {
  static int x;
  int A.m(int y) {
    y = n(y);
    System.out.println(y);
    return y;
  }
  private int n(int y) {
    y += x;
    return y;
  }
  X() {
    super();
  }
}

class A {
  A() {
    super();
  }
}
