package p;

class B {
  
  class D {
    D() {
      super();
    }
  }
  B() {
    super();
  }
}

class A {
  
  class D {
    D() {
      super();
    }
  }
  
  class E extends B {
    A.D d;
    E() {
      super();
    }
  }
  A() {
    super();
  }
}
