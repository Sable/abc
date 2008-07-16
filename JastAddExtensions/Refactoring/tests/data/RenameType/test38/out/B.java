package p;

public class B {
  public B() {
    super();
  }
}

class C {
  {
    B a;
  }
  C() {
    super();
  }
}
