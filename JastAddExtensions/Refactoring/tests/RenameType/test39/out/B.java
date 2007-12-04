package p;
public class B {
  public B(){
    super();
  }
}

class C {
  static {
    p.B a;
  }
  C(){
    super();
  }
}
