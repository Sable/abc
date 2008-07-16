package p;

public class B {
  
  class C {
    public  <T extends java.lang.Object> C() {
    
      super();
    }
  }
  public B() {
    super();
  }
}

class D {
  Object o = new B().<B>new C();
  D() {
    super();
  }
}
