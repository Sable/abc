class A {
  Object m() {
    return n();
  }
  A() {
    super();
  }
  private Object n() {
      class B {
        B() {
          super();
        }
      }
    return new B();
  }
}
