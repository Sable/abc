class A {
  Object m() {
    return extracted();
  }
  protected Class<A> extracted() {
    return A.class;
  }
  A() {
    super();
  }
}
