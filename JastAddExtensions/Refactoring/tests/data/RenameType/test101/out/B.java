package p;

public class B {
  public B() {
    super();
  }
}

class X {
  
  class Y {
     <T extends java.lang.Object> Y() {
    
      super();
    }
  }
  X() {
    super();
  }
}

class Z extends X.Y {
  Z() {
    new X().<B>super();
  }
}
