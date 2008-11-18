class A {
  Object m() {
    return n();
  }
  private Object n() {
      class B {
        B() {
          super();
        }
      }
    return new B();
  }
  A() {
    super();
  }
}
