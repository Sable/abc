package p;
public class B {
  public B(){
    super();
  }
}

class C {
  void m() {
    class A {
      A(){
        super();
      }
    }
    new p.C.A();
  }
  C(){
    super();
  }
}
