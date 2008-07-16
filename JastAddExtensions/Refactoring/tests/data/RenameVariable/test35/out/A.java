package p;

class A {
  int y;
  
  class B {
    int y;
    public B() {
      this(A.this.y);
    }
    public B(int z) {
      super();
      System.out.println(z);
    }
  }
  A() {
    super();
  }
}
