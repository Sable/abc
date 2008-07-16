package p;

class A {
  B f;
  
  class B extends A {
    int f;
    {
      B b = ((A)((A)B.this).f).f;
    }
    B() {
      super();
    }
  }
  A() {
    super();
  }
}
