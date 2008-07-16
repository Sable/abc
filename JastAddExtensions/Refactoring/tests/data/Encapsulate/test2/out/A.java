package p;

class A {
  void getI(String s) {
  }
  
  class C {
    
    class B {
      void m() {
        A.this.getI("");
      }
      B() {
        super();
      }
    }
    private int i;
    C() {
      super();
    }
    private int getI() {
      return i;
    }
    private int setI(int i) {
      return this.i = i;
    }
  }
  A() {
    super();
  }
}
