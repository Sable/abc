class A {
  A() {
    this(extracted());
  }
  protected static int extracted() {
    return 23;
  }
  A(int i) {
    super();
  }
}
