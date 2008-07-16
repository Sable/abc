package p;

class A {
  B f;
  
  class B extends A {
    int f;
    {
      B b = ((A)super.f).f;
    }
    B() {
      super();
    }
  }
  A() {
    super();
  }
}
