package p;
class B {
  int x;
  class Inner {
    void m() {
      p.B.this.x++;
    }
    Inner(){
      super();
    }
  }
  B(){
    super();
  }
}
