package p;

class A {
  protected void k() {
    System.out.println(23);
  }
  A() {
    super();
  }
}

class B extends A {
  protected void k() {
    System.out.println(42);
  }
  public static void main(String[] args) {
    new B().m();
  }
  B() {
    super();
  }
  void m() {
    super.k();
  }
}
