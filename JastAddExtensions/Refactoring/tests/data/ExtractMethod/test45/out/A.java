class A {
  int m() {
    return extracted();
  }
  protected int extracted() {
    do {
      return 42;
    }while(false);
  }
  A() {
    super();
  }
}
