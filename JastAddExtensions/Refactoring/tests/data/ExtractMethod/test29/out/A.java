class A {
   <T extends java.lang.Object> int m() {
    n();
    return 42;
  }
  A() {
    super();
  }
  private  <T extends java.lang.Object> void n() {
    T t;
  }
}
