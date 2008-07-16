package p;

class B {
  int data = 0;
  B() {
    super();
  }
}

public class A extends B {
  public void f() {
    int data;
    this.getData().setX(0);
    this.getData().setY(new p.Data());
    this.getData().setX(2);
    this.getData().getY().z = 3;
  }
  public int init() {
    return 4 + super.data;
  }
  public void g() {
    p.Data data;
  }
  public A() {
    super();
  }
  
  static class Data {
    A(int x, p.Data y) {
      super();
      this.setX(x);
      this.setY(y);
    }
    private int x;
    private p.Data y;
    int getX() {
      return x;
    }
    int setX(int x) {
      return this.x = x;
    }
    p.Data getY() {
      return y;
    }
    p.Data setY(p.Data y) {
      return this.y = y;
    }
  }
  private Data data = new Data(init(), new p.Data());
  public Data getData() {
    return data;
  }
  public Data setData(Data data) {
    return this.data = data;
  }
}

class Data {
  int z;
  Data() {
    super();
  }
}
