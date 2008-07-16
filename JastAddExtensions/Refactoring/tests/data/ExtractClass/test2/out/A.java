package p;

class B {
  int d = 0;
  B() {
    super();
  }
}

public class A extends B {
  public int init() {
    return 4 + super.d;
  }
  public A() {
    super();
  }
  
  static class Data {
    A(int x) {
      super();
      this.setX(x);
    }
    private int x;
    int getX() {
      return x;
    }
    int setX(int x) {
      return this.x = x;
    }
  }
  private Data d = new Data(init());
  public Data getD() {
    return d;
  }
  public Data setD(Data d) {
    return this.d = d;
  }
}
