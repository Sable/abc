class B {
  
  class C {
    C() {
      super();
    }
  }
  B() {
    super();
  }
}

class A extends B {
  void m() {
    C c = new C();
    {
        class C {
          C() {
            super();
          }
        }
      System.out.println((Object)(B.C)c);
    }
  }
  A() {
    super();
  }
}
