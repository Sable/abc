package p;

class Super {
  int x;
  Super() {
    super();
  }
}

class B extends Super {
  String x;
  
  class Inner {
    void m() {
      B.super.x++;
    }
    Inner() {
      super();
    }
  }
  B() {
    super();
  }
}
