class A {
  int f() {
    try {
      return extracted();
    }
    finally {
    }
  }
  protected int extracted() {
    int k = 0;
    return k;
  }
  A() {
    super();
  }
}
