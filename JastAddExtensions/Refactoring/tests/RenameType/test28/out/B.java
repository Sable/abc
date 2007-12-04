package p;
class Super {
  void m1() {
  }
  {
  }
  Super(){
    super();
  }
}

class B extends Super {
  class Inner {
    void m() {
      p.B.super.m1();
    }
    Inner(){
      super();
    }
  }
  B(){
    super();
  }
}
