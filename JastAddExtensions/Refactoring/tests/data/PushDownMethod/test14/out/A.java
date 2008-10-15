package p;

class A {
  
  class B {
    public B() {
      super();
      System.out.println(23);
    }
  }
  A() {
    super();
  }
}

class C extends A {
  
  class B {
    public B() {
      super();
      System.out.println(42);
    }
  }
  public static void main(String[] args) {
    new C().m();
  }
  C() {
    super();
  }
  A.B m() {
    return new A.B();
  }
}
