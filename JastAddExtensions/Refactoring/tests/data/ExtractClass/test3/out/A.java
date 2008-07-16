package p;

public class A {
  public A() {
    super();
  }
  
  static class Data {
    A() {
      super();
    }
    private p.Data x;
    p.Data getX() {
      return x;
    }
    p.Data setX(p.Data x) {
      return this.x = x;
    }
  }
  private Data data = new Data();
  public Data getData() {
    return data;
  }
  public Data setData(Data data) {
    return this.data = data;
  }
}

class Data {
  Data() {
    super();
  }
}
