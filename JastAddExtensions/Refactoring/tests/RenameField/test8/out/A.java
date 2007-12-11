package p;
class A {
  public p.A g;
  public int k;
  void m() {
    g.g.g.k = 0;
  }
  A(){
    super();
  }
}
