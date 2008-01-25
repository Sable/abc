package p;
public class A {
  class XYZ {
    XYZ(p.A.XYZ X){
      super();
      new p.A.XYZ(null);
    }
  }
  A(){
    super();
  }
  A(p.A A){
    super();
  }
  p.A m() {
    new p.A.XYZ(null);
    return (p.A)new p.A();
  }
}

class B {
  p.A.XYZ ax = new p.A().new p.A.XYZ(null);
  B(){
    super();
  }
}
