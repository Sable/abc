package p;
class Super {
  int x;
  Super(){
    super();
  }
}

class B extends Super {
  java.lang.String x;
  class Inner {
    void m() {
      p.B.super.x++;
    }
    Inner(){
      super();
    }
  }
  B(){
    super();
  }
}
