package p;
public class B {
  public B(){
    super();
  }
}

class C {
  {
    p.B a;
  }
  C(){
    super();
  }
}
