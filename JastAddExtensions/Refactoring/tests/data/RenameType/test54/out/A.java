package p;

public class A {
  
  class XYZ {
    XYZ(XYZ X) {
      super();
      new XYZ(null);
    }
  }
  A() {
    super();
  }
  A(A A) {
    super();
  }
  A m() {
    new XYZ(null);
    return (A)new A();
  }
}

class B {
  A.XYZ ax = new A().new XYZ(null);
  B() {
    super();
  }
}
